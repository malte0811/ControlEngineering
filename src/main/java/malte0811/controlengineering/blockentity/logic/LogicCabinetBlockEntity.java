package malte0811.controlengineering.blockentity.logic;

import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.blockentity.base.CEBlockEntity;
import malte0811.controlengineering.blockentity.base.IExtraDropBE;
import malte0811.controlengineering.blockentity.base.IHasMaster;
import malte0811.controlengineering.blocks.logic.LogicCabinetBlock;
import malte0811.controlengineering.blocks.shapes.*;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.IBusInterface;
import malte0811.controlengineering.bus.MarkDirtyHandler;
import malte0811.controlengineering.gui.logic.LogicDesignContainer;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.PCBStackItem;
import malte0811.controlengineering.logic.circuit.BusConnectedCircuit;
import malte0811.controlengineering.logic.clock.ClockGenerator;
import malte0811.controlengineering.logic.clock.ClockGenerator.ClockInstance;
import malte0811.controlengineering.logic.clock.ClockTypes;
import malte0811.controlengineering.logic.model.DynamicLogicModel;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.SchematicCircuitConverter;
import malte0811.controlengineering.util.*;
import malte0811.controlengineering.util.energy.CEEnergyStorage;
import malte0811.controlengineering.util.math.MatrixUtils;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class LogicCabinetBlockEntity extends CEBlockEntity implements SelectionShapeOwner, IBusInterface,
        ISchematicBE, IExtraDropBE, IHasMaster<LogicCabinetBlockEntity> {
    public static final int MAX_NUM_BOARDS = 4;
    public static final int NUM_TUBES_PER_BOARD = 16;

    private final CEEnergyStorage energy = new CEEnergyStorage(2048, 2 * 128, 128);
    @Nullable
    private Pair<Schematic, BusConnectedCircuit> circuit;
    @Nonnull
    private ClockInstance<?> clock = ClockTypes.NEVER.newInstance();
    private final MarkDirtyHandler markBusDirty = new MarkDirtyHandler();
    private int numTubes;
    private BusState currentBusState = BusState.EMPTY;

    public LogicCabinetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static int getNumBoardsFor(int numTubes) {
        return Mth.ceil(numTubes / (double) NUM_TUBES_PER_BOARD);
    }

    public void tick() {
        //TODO less? config?
        if (circuit == null || energy.extractOrTrue(128) || level.getGameTime() % 2 != 0) {
            return;
        }
        final Direction facing = getFacing(getBlockState());
        final Direction clockFace = facing.getCounterClockWise();
        boolean rsIn = level.getSignal(worldPosition.relative(clockFace), clockFace.getOpposite()) > 0;
        if (!clock.tick(rsIn)) {
            return;
        }
        // Inputs are updated in onBusUpdated
        if (circuit.getSecond().tick()) {
            markBusDirty.run();
        }
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        clock = Codecs.readOrNull(ClockInstance.CODEC, nbt.getCompound("clock"));
        if (clock == null) {
            clock = ClockTypes.NEVER.newInstance();
        }
        if (nbt.contains("circuit")) {
            setCircuit(Codecs.readOrNull(Schematic.CODEC, nbt.get("circuit")));
        } else {
            setCircuit(null);
        }
        energy.readNBT(nbt.get("energy"));
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        super.saveAdditional(compound);
        compound.put("clock", Codecs.encode(ClockInstance.CODEC, clock));
        if (circuit != null) {
            compound.put("circuit", Codecs.encode(Schematic.CODEC, circuit.getFirst()));
        }
        compound.put("energy", energy.writeNBT());
    }

    @Override
    protected void writeSyncedData(CompoundTag result) {
        result.putBoolean("hasClock", clock.getType().isActiveClock());
        result.putInt("numTubes", numTubes);
    }

    @Override
    protected void readSyncedData(CompoundTag tag) {
        if (tag.getBoolean("hasClock"))
            clock = ClockTypes.ALWAYS_ON.newInstance();
        else
            clock = ClockTypes.NEVER.newInstance();
        numTubes = tag.getInt("numTubes");
        requestModelDataUpdate();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new SinglePropertyModelData<>(
                new DynamicLogicModel.ModelData(numTubes, clock.getType().isActiveClock()), DynamicLogicModel.DATA
        );
    }

    @Override
    public void onBusUpdated(BusState totalState, BusState otherState) {
        if (circuit != null) {
            circuit.getSecond().updateInputs(totalState);
        }
        this.currentBusState = totalState;
    }

    @Override
    public BusState getEmittedState() {
        if (circuit != null) {
            return circuit.getSecond().getOutputState();
        } else {
            return BusState.EMPTY;
        }
    }

    @Override
    public boolean canConnect(Direction fromSide) {
        return fromSide == getFacing(getBlockState()).getClockWise();
    }

    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(energy::insertOnlyView);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(
            @Nonnull Capability<T> cap, @Nullable Direction side
    ) {
        if (cap == CapabilityEnergy.ENERGY && side == getFacing(getBlockState())) {
            return energyCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
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

    public void setCircuit(@Nullable Schematic schematic) {
        this.circuit = null;
        if (schematic != null) {
            Optional<BusConnectedCircuit> busCircuit = SchematicCircuitConverter.toCircuit(schematic);
            if (busCircuit.isPresent()) {
                this.circuit = Pair.of(schematic, busCircuit.get());
            }
        }
        if (this.circuit != null) {
            this.numTubes = this.circuit.getSecond().getNumTubes();
            this.circuit.getSecond().updateInputs(currentBusState);
        } else {
            this.numTubes = 0;
        }
    }

    private static Direction getFacing(BlockState state) {
        return state.getValue(LogicCabinetBlock.FACING);
    }

    private static boolean isUpper(BlockState state) {
        return state.getValue(LogicCabinetBlock.HEIGHT) != 0;
    }

    private final CachedValue<BlockState, SelectionShapes> selectionShapes = new CachedValue<>(
            this::getBlockState,
            state -> createSelectionShapes(getFacing(state), computeMasterBE(state), isUpper(state))
    );

    @Override
    public SelectionShapes getShape() {
        return selectionShapes.get();
    }

    private static SelectionShapes createSelectionShapes(Direction d, LogicCabinetBlockEntity bEntity, boolean upper) {
        List<SelectionShapes> subshapes = new ArrayList<>(1);
        DirectionalShapeProvider baseShape = upper ? LogicCabinetBlock.TOP_SHAPE : LogicCabinetBlock.BOTTOM_SHAPE;
        if (!upper) {
            subshapes.add(makeClockInteraction(bEntity));
        } else {
            subshapes.add(makeViewDesignInteraction(bEntity));
        }
        subshapes.add(makeBoardInteraction(bEntity, upper));
        return new ListShapes(
                baseShape.apply(d), MatrixUtils.inverseFacing(d), subshapes, $ -> InteractionResult.PASS
        );
    }

    private static SelectionShapes makeClockInteraction(LogicCabinetBlockEntity bEntity) {
        return new SingleShape(
                ShapeUtils.createPixelRelative(0, 6, 6, 5, 10, 10), ctx -> {
            if (ctx.getPlayer() == null) {
                return InteractionResult.PASS;
            }
            ClockGenerator<?> currentClock = bEntity.clock.getType();
            RegistryObject<Item> clockItem = CEItems.CLOCK_GENERATORS.get(currentClock.getRegistryName());
            if (!ctx.getLevel().isClientSide) {
                if (clockItem != null) {
                    ItemUtil.giveOrDrop(ctx.getPlayer(), new ItemStack(clockItem.get()));
                    bEntity.clock = ClockTypes.NEVER.newInstance();
                    BEUtil.markDirtyAndSync(bEntity);
                } else {
                    ItemStack item = ctx.getItemInHand();
                    ClockGenerator<?> newClock = ClockTypes.REGISTRY.get(item.getItem().getRegistryName());
                    if (newClock != null) {
                        bEntity.clock = newClock.newInstance();
                        item.shrink(1);
                        BEUtil.markDirtyAndSync(bEntity);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        });
    }

    private static SelectionShapes makeBoardInteraction(LogicCabinetBlockEntity bEntity, boolean upper) {
        final int yOff = upper ? -16 : 0;
        final VoxelShape fullShape = ShapeUtils.createPixelRelative(
                1, 11 + yOff, 1, 15, 31 + yOff, 15
        );
        return new SingleShape(
                fullShape, ctx -> {
            if (ctx.getPlayer() == null) {
                return InteractionResult.PASS;
            }
            if (!ctx.getLevel().isClientSide) {
                final Pair<Schematic, BusConnectedCircuit> oldSchematic = bEntity.circuit;
                Pair<Schematic, BusConnectedCircuit> schematic = PCBStackItem.getSchematic(ctx.getItemInHand());
                if (schematic != null) {
                    bEntity.setCircuit(schematic.getFirst());
                    ctx.getItemInHand().shrink(1);
                } else {
                    bEntity.setCircuit(null);
                    bEntity.markBusDirty.run();
                }
                if (oldSchematic != null) {
                    ItemUtil.giveOrDrop(ctx.getPlayer(), PCBStackItem.forSchematic(oldSchematic.getFirst()));
                }
                BEUtil.markDirtyAndSync(bEntity);
            }
            return InteractionResult.SUCCESS;
        });
    }

    private static SelectionShapes makeViewDesignInteraction(LogicCabinetBlockEntity bEntity) {
        final VoxelShape shape = ShapeUtils.createPixelRelative(
                15, 1, 4, 16, 11, 12
        );
        return new SingleShape(
                shape, ctx -> {
            final Player player = ctx.getPlayer();
            if (player == null) {
                return InteractionResult.PASS;
            }
            if (player instanceof ServerPlayer && bEntity.circuit != null) {
                LogicDesignContainer.makeProvider(bEntity.level, bEntity.worldPosition, true)
                        .open((ServerPlayer) player);
            }
            return InteractionResult.SUCCESS;
        });
    }

    @Override
    public Schematic getSchematic() {
        if (circuit != null) {
            return circuit.getFirst();
        } else {
            // should never happen(?)
            return new Schematic();
        }
    }

    @Override
    public void getExtraDrops(Consumer<ItemStack> dropper) {
        if (circuit != null) {
            dropper.accept(PCBStackItem.forSchematic(circuit.getFirst()));
        }
        RegistryObject<Item> clockItem = CEItems.CLOCK_GENERATORS.get(clock.getType().getRegistryName());
        if (clockItem != null) {
            dropper.accept(clockItem.get().getDefaultInstance());
        }
    }

    @Nullable
    @Override
    public LogicCabinetBlockEntity computeMasterBE(BlockState stateHere) {
        if (isUpper(stateHere)) {
            BlockEntity below = level.getBlockEntity(worldPosition.below());
            if (below instanceof LogicCabinetBlockEntity) {
                return (LogicCabinetBlockEntity) below;
            } else {
                return null;
            }
        } else {
            return this;
        }
    }
}
