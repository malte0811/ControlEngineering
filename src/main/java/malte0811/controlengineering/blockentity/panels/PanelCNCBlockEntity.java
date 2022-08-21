package malte0811.controlengineering.blockentity.panels;

import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.blockentity.MultiblockBEType;
import malte0811.controlengineering.blockentity.base.CEBlockEntity;
import malte0811.controlengineering.blockentity.base.IExtraDropBE;
import malte0811.controlengineering.blockentity.bus.ParallelPort;
import malte0811.controlengineering.blockentity.tape.TapeDrive;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.panels.PanelCNCBlock;
import malte0811.controlengineering.blocks.shapes.ListShapes;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.IBusInterface;
import malte0811.controlengineering.bus.MarkDirtyHandler;
import malte0811.controlengineering.client.render.utils.PiecewiseAffinePath;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.cnc.CNCInstructionParser;
import malte0811.controlengineering.items.PanelTopItem;
import malte0811.controlengineering.items.PunchedTapeItem;
import malte0811.controlengineering.util.*;
import malte0811.controlengineering.util.math.MatrixUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.DeferredRegister;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static malte0811.controlengineering.util.ShapeUtils.createPixelRelative;

public class PanelCNCBlockEntity extends CEBlockEntity implements SelectionShapeOwner, IExtraDropBE, IBusInterface {
    private static final int ENERGY_CONSUMPTION = 40;

    private State state = State.EMPTY;
    private final TapeDrive tape = new TapeDrive(
            () -> setState(state.addTape()), () -> setState(state.removeTape()), () -> state.canTakeTape()
    );
    private final CachedValue<byte[], CNCJob> currentJob = new CachedValue<>(
            tape::getNullableTapeContent,
            tape -> {
                if (tape != null) {
                    return CNCJob.createFor(CNCInstructionParser.parse(level, BitUtils.toString(tape)));
                } else {
                    return null;
                }
            },
            Arrays::equals,
            b -> b == null ? null : Arrays.copyOf(b, b.length)
    );
    private int currentTicksInJob;
    private final List<PlacedComponent> currentPlacedComponents = new ArrayList<>();
    private final List<CapabilityReference<IItemHandler>> neighborInventories = Util.make(
            new ArrayList<>(),
            list -> {
                for (Direction d : DirectionUtils.BY_HORIZONTAL_INDEX) {
                    list.add(CapabilityReference.forNeighbor(this, ForgeCapabilities.ITEM_HANDLER, d));
                }
            }
    );
    private final EnergyStorage energy = new EnergyStorage(20 * ENERGY_CONSUMPTION);
    private final MarkDirtyHandler markBusDirty = new MarkDirtyHandler();
    private final ParallelPort dataOutput = new ParallelPort();
    // Used and initialized by the renderer
    public CachedValue<CNCJob, PiecewiseAffinePath<Vec3>> headPath;

    private final CachedValue<Direction, SelectionShapes> bottomSelectionShapes = new CachedValue<>(
            () -> getBlockState().getValue(PanelCNCBlock.FACING),
            facing -> new ListShapes(
                    Shapes.block(),
                    MatrixUtils.inverseFacing(facing),
                    ImmutableList.of(
                            new SingleShape(createPixelRelative(1, 14, 1, 15, 16, 15), this::panelClick),
                            new SingleShape(createPixelRelative(2, 4, 14, 14, 12, 16), tape::click),
                            new SingleShape(
                                    createPixelRelative(4, 4, 0, 12, 12, 1),
                                    dataOutput.makeRemapInteraction(this)
                            )
                    ),
                    ctx -> InteractionResult.PASS
            )
    );

    private static final SelectionShapes topSelectionShapes = new SingleShape(
            PanelCNCBlock.UPPER_SHAPE, $ -> InteractionResult.PASS
    );

    public PanelCNCBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private InteractionResult panelClick(UseOnContext ctx) {
        if (level == null) {
            return InteractionResult.PASS;
        }
        if (state.canTakePanel()) {
            if (!level.isClientSide && ctx.getPlayer() != null) {
                ItemStack result = PanelTopItem.createWithComponents(currentPlacedComponents);
                ItemUtil.giveOrDrop(ctx.getPlayer(), result);
                currentPlacedComponents.clear();
                currentTicksInJob = 0;
                setState(state.removePanel());
            }
            return InteractionResult.SUCCESS;
        } else if (!state.hasPanel()) {
            ItemStack heldItem = ctx.getItemInHand();
            if (PanelTopItem.isEmptyPanelTop(heldItem)) {
                if (!level.isClientSide) {
                    setState(state.addPanel());
                    heldItem.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    public void clientTick() {
        if (state == State.RUNNING) {
            ++currentTicksInJob;
        }
    }

    public void tick() {
        if (dataOutput.tickTX()) {
            markBusDirty.run();
        }
        if (state.isInProcess()) {
            if (energy.extractEnergy(ENERGY_CONSUMPTION, true) < ENERGY_CONSUMPTION) {
                setState(State.NO_ENERGY);
                return;
            }
            energy.extractEnergy(ENERGY_CONSUMPTION, false);
            setState(State.RUNNING);
            ++currentTicksInJob;
            int nextComponent = currentPlacedComponents.size();
            CNCJob job = currentJob.get();
            if (nextComponent < job.getTotalComponents()) {
                if (!level.isClientSide && currentTicksInJob >= job.tickPlacingComponent().getInt(nextComponent)) {
                    PlacedComponent componentToPlace = job.components().get(nextComponent);
                    var componentCost = componentToPlace.getComponent().getType().getCost(level);
                    if (!ItemUtil.tryConsumeItemsFrom(componentCost, neighborInventories)) {
                        dataOutput.queueStringWithParity("Unable to consume items for component number " + nextComponent);
                        setState(State.FAILED);
                    } else {
                        currentPlacedComponents.add(componentToPlace);
                        BEUtil.markDirtyAndSync(this);
                    }
                }
            }
            if (currentTicksInJob >= job.totalTicks()) {
                if (job.error() != null) {
                    dataOutput.queueStringWithParity(job.error());
                    setState(State.FAILED);
                } else {
                    setState(State.DONE);
                }
            }
        }
    }

    @Override
    public SelectionShapes getShape() {
        if (getBlockState().getValue(PanelCNCBlock.UPPER)) {
            return topSelectionShapes;
        } else {
            return bottomSelectionShapes.get();
        }
    }

    @Nullable
    public CNCJob getCurrentJob() {
        return currentJob.get();
    }

    public int getTapeLength() {
        return tape.getTapeLength();
    }

    public int getCurrentTicksInJob() {
        return currentTicksInJob;
    }

    public List<PlacedComponent> getCurrentPlacedComponents() {
        return currentPlacedComponents;
    }

    public State getState() {
        return state;
    }


    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        super.saveAdditional(compound);
        writeSyncedData(compound);
        compound.put("energy", energy.serializeNBT());
        compound.put("dataOutput", dataOutput.toNBT());
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        readSyncedData(nbt);
        energy.deserializeNBT(nbt.get("energy"));
        dataOutput.readNBT(nbt.getCompound("dataOutput"));
    }

    @Override
    protected void readSyncedData(CompoundTag compound) {
        tape.loadNBT(compound.get("tape"));
        currentTicksInJob = compound.getInt("currentTick");
        state = State.VALUES[compound.getInt("state")];
        currentPlacedComponents.clear();
        var components = PlacedComponent.LIST_CODEC.fromNBT(compound.get("components"));
        if (components != null) {
            currentPlacedComponents.addAll(components);
        }
    }

    @Override
    protected void writeSyncedData(CompoundTag in) {
        in.put("tape", tape.toNBT());
        in.putInt("currentTick", currentTicksInJob);
        in.putInt("state", state.ordinal());
        in.put("components", PlacedComponent.LIST_CODEC.toNBT(currentPlacedComponents));
    }

    @Override
    public void getExtraDrops(Consumer<ItemStack> dropper) {
        if (tape.hasTape()) {
            dropper.accept(PunchedTapeItem.withBytes(tape.getTapeContent()));
        }
        if (state.hasPanel()) {
            dropper.accept(PanelTopItem.createWithComponents(currentPlacedComponents));
        }
    }

    private final CachedValue<BlockPos, AABB> renderBB = new CachedValue<>(
            () -> worldPosition, pos -> new AABB(
            pos.getX(), pos.getY(), pos.getZ(),
            pos.getX() + 1, pos.getY() + 2, pos.getZ() + 2
    ));

    @Override
    public AABB getRenderBoundingBox() {
        return renderBB.get();
    }

    public static MultiblockBEType<PanelCNCBlockEntity, ?> register(DeferredRegister<BlockEntityType<?>> register) {
        return MultiblockBEType.makeType(
                register, "panel_cnc", PanelCNCBlockEntity::new, Dummy::new, CEBlocks.PANEL_CNC, PanelCNCBlock::isMaster
        );
    }

    private void setState(State state) {
        if (state != this.state) {
            this.state = state;
            BEUtil.markDirtyAndSync(this);
        }
    }


    @Override
    public void onBusUpdated(BusState totalState, BusState otherState) {
        dataOutput.onBusStateChange(otherState);
        setChanged();
    }

    @Override
    public BusState getEmittedState() {
        return dataOutput.getOutputState();
    }

    @Override
    public boolean canConnect(Direction fromSide) {
        return fromSide == getBlockState().getValue(PanelCNCBlock.FACING);
    }

    @Override
    public void addMarkDirtyCallback(Clearable<Runnable> markDirty) {
        this.markBusDirty.addCallback(markDirty);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.markBusDirty.run();
    }

    private static class Dummy extends CEBlockEntity {
        private LazyOptional<IEnergyStorage> energyRef = null;

        public Dummy(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap == ForgeCapabilities.ENERGY && CapabilityUtils.isNullOr(Direction.UP, side)) {
                if (energyRef == null) {
                    if (level.getBlockEntity(worldPosition.below()) instanceof PanelCNCBlockEntity paneCNC) {
                        energyRef = CapabilityUtils.constantOptional(paneCNC.energy);
                    } else {
                        return LazyOptional.empty();
                    }
                }
                return energyRef.cast();
            }
            return super.getCapability(cap, side);
        }

        @Override
        public void invalidateCaps() {
            super.invalidateCaps();
            if (energyRef != null) {
                energyRef.invalidate();
            }
        }
    }

    public enum State {
        EMPTY,
        HAS_TAPE,
        HAS_PANEL,
        RUNNING,
        NO_ENERGY,
        FAILED,
        DONE;

        public static final State[] VALUES = values();

        public boolean canTakePanel() {
            return hasPanel() && !isInProcess();
        }

        public boolean hasPanel() {
            return this == HAS_PANEL || this == RUNNING || this == NO_ENERGY || this == FAILED || this == DONE;
        }

        public boolean isInProcess() {
            return this == RUNNING || this == NO_ENERGY;
        }

        public State removePanel() {
            return switch (this) {
                case HAS_PANEL -> EMPTY;
                case FAILED, DONE -> HAS_TAPE;
                default -> throw new RuntimeException(name());
            };
        }

        public State addPanel() {
            return switch (this) {
                case EMPTY -> HAS_PANEL;
                case HAS_TAPE -> RUNNING;
                default -> throw new RuntimeException(name());
            };
        }

        public State addTape() {
            return switch (this) {
                case EMPTY -> HAS_TAPE;
                case HAS_PANEL -> RUNNING;
                default -> throw new RuntimeException(name());
            };
        }

        public boolean canTakeTape() {
            return hasTape() && !isInProcess();
        }

        public State removeTape() {
            return switch (this) {
                case HAS_TAPE -> EMPTY;
                case FAILED, DONE -> HAS_PANEL;
                default -> throw new RuntimeException(name());
            };
        }

        public boolean hasTape() {
            return this == HAS_TAPE || this == RUNNING || this == NO_ENERGY || this == FAILED || this == DONE;
        }
    }
}
