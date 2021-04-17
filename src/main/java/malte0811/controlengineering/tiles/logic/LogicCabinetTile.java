package malte0811.controlengineering.tiles.logic;

import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.blocks.logic.LogicCabinetBlock;
import malte0811.controlengineering.blocks.shapes.*;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.BusWireTypes;
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
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.util.CachedValue;
import malte0811.controlengineering.util.Clearable;
import malte0811.controlengineering.util.ItemUtil;
import malte0811.controlengineering.util.Matrix4;
import malte0811.controlengineering.util.energy.CEEnergyStorage;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LogicCabinetTile extends TileEntity implements SelectionShapeOwner, IBusInterface, ITickableTileEntity,
        ISchematicTile {
    private final CEEnergyStorage energy = new CEEnergyStorage(2048, 2 * 128, 128);
    @Nullable
    private Pair<Schematic, BusConnectedCircuit> circuit;
    @Nonnull
    private ClockInstance<?> clock = ClockTypes.NEVER.newInstance();
    private final MarkDirtyHandler markBusDirty = new MarkDirtyHandler();
    private int numTubes;
    private BusState currentBusState = new BusState(BusWireTypes.MAX_BUS_WIDTH);

    public LogicCabinetTile() {
        super(CETileEntities.LOGIC_CABINET.get());
    }

    @Override
    public void tick() {
        if (world.isRemote || isUpper()) {
            return;
        }
        //TODO less? config?
        if (circuit == null || energy.extractOrTrue(128) || world.getGameTime() % 2 != 0) {
            return;
        }
        final Direction facing = getFacing();
        final Direction clockFace = facing.rotateYCCW();
        boolean rsIn = world.getRedstonePower(pos.offset(clockFace), clockFace.getOpposite()) > 0;
        if (!clock.tick(rsIn)) {
            return;
        }
        // Inputs are updated in onBusUpdated
        if (circuit.getSecond().tick()) {
            markBusDirty.run();
        }
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
        super.read(state, nbt);
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

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        compound = super.write(compound);
        compound.put("clock", Codecs.encode(ClockInstance.CODEC, clock));
        if (circuit != null) {
            compound.put("circuit", Codecs.encode(Schematic.CODEC, circuit.getFirst()));
        }
        compound.put("energy", energy.writeNBT());
        return compound;
    }

    private CompoundNBT writeDynamicSyncNBT(CompoundNBT result) {
        result.putBoolean("hasClock", clock.getType().isActiveClock());
        result.putInt("numTubes", numTubes);
        return result;
    }

    private void readDynamicSyncNBT(CompoundNBT tag) {
        if (tag.getBoolean("hasClock"))
            clock = ClockTypes.ALWAYS_ON.newInstance();
        else
            clock = ClockTypes.NEVER.newInstance();
        numTubes = tag.getInt("numTubes");
        requestModelDataUpdate();
        world.notifyBlockUpdate(
                pos, getBlockState(), getBlockState(), Constants.BlockFlags.DEFAULT
        );
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        readDynamicSyncNBT(tag);
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return writeDynamicSyncNBT(super.getUpdateTag());
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(pos, -1, writeDynamicSyncNBT(new CompoundNBT()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        readDynamicSyncNBT(pkt.getNbtCompound());
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new SinglePropertyModelData<>(
                new DynamicLogicModel.ModelData(numTubes, clock.getType().isActiveClock()), DynamicLogicModel.DATA
        );
    }

    @Override
    public void onBusUpdated(BusState newState) {
        if (circuit != null) {
            circuit.getSecond().updateInputs(newState);
        }
        this.currentBusState = newState;
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
        return fromSide == getFacing().rotateY();
    }

    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(energy::insertOnlyView);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(
            @Nonnull Capability<T> cap, @Nullable Direction side
    ) {
        if (cap == CapabilityEnergy.ENERGY && side == getFacing()) {
            return energyCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
    }

    @Override
    public void addMarkDirtyCallback(Clearable<Runnable> markDirty) {
        this.markBusDirty.addCallback(markDirty);
    }

    @Override
    public void remove() {
        super.remove();
        this.markBusDirty.run();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.markBusDirty.run();
    }

    public void setCircuit(@Nullable Schematic schematic) {
        this.circuit = null;
        if (schematic != null) {
            Optional<BusConnectedCircuit> busCircuit = schematic.toCircuit().left();
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

    private Direction getFacing() {
        return getBlockState().get(LogicCabinetBlock.FACING);
    }

    private boolean isUpper() {
        return getBlockState().get(LogicCabinetBlock.HEIGHT) != 0;
    }

    private final CachedValue<Pair<Direction, Boolean>, SelectionShapes> selectionShapes = new CachedValue<>(
            () -> Pair.of(getFacing(), isUpper()),
            f -> {
                LogicCabinetTile tile = this;
                if (f.getSecond()) {
                    TileEntity below = world.getTileEntity(pos.down());
                    if (below instanceof LogicCabinetTile) {
                        tile = (LogicCabinetTile) below;
                    }
                }
                return createSelectionShapes(f.getFirst(), tile, f.getSecond());
            }
    );

    @Override
    public SelectionShapes getShape() {
        return selectionShapes.get();
    }

    private void markDirtyAndSync() {
        markDirty();
        if (world.getChunkProvider() instanceof ServerChunkProvider) {
            ((ServerChunkProvider) world.getChunkProvider()).markBlockChanged(pos);
        }
    }

    private static SelectionShapes createSelectionShapes(Direction d, LogicCabinetTile tile, boolean upper) {
        List<SelectionShapes> subshapes = new ArrayList<>(1);
        HorizontalShapeProvider baseShape = upper ? LogicCabinetBlock.TOP_SHAPE : LogicCabinetBlock.BOTTOM_SHAPE;
        if (!upper) {
            subshapes.add(makeClockInteraction(tile));
        } else {
            subshapes.add(makeViewDesignInteraction(tile));
        }
        subshapes.add(makeBoardInteraction(tile, upper));
        return new ListShapes(
                baseShape.apply(d), Matrix4.inverseFacing(d), subshapes, $ -> ActionResultType.PASS
        );
    }

    private static SelectionShapes makeClockInteraction(LogicCabinetTile tile) {
        return new SingleShape(
                VoxelShapes.create(0, 6 / 16., 6 / 16., 5 / 16., 10 / 16., 10 / 16.), ctx -> {
            if (ctx.getPlayer() == null) {
                return ActionResultType.PASS;
            }
            ClockGenerator<?> currentClock = tile.clock.getType();
            RegistryObject<Item> clockItem = CEItems.CLOCK_GENERATORS.get(currentClock.getRegistryName());
            if (!ctx.getWorld().isRemote) {
                if (clockItem != null) {
                    ItemUtil.giveOrDrop(ctx.getPlayer(), new ItemStack(clockItem.get()));
                    tile.clock = ClockTypes.NEVER.newInstance();
                    tile.markDirtyAndSync();
                } else {
                    ItemStack item = ctx.getItem();
                    ClockGenerator<?> newClock = ClockTypes.REGISTRY.get(item.getItem().getRegistryName());
                    if (newClock != null) {
                        tile.clock = newClock.newInstance();
                        item.shrink(1);
                        tile.markDirtyAndSync();
                    }
                }
            }
            return ActionResultType.SUCCESS;
        });
    }

    private static SelectionShapes makeBoardInteraction(LogicCabinetTile tile, boolean upper) {
        final int yOff = upper ? -16 : 0;
        final VoxelShape fullShape = VoxelShapes.create(
                0, (11 + yOff) / 16., 1 / 16.,
                15 / 16., (31 + yOff) / 16., 15 / 16.
        );
        return new SingleShape(
                fullShape, ctx -> {
            if (ctx.getPlayer() == null) {
                return ActionResultType.PASS;
            }
            if (!ctx.getWorld().isRemote) {
                final Pair<Schematic, BusConnectedCircuit> oldSchematic = tile.circuit;
                Pair<Schematic, BusConnectedCircuit> schematic = PCBStackItem.getSchematic(ctx.getItem());
                if (schematic != null) {
                    tile.setCircuit(schematic.getFirst());
                    ctx.getItem().shrink(1);
                } else {
                    tile.setCircuit(null);
                    tile.markBusDirty.run();
                }
                if (oldSchematic != null) {
                    ItemUtil.giveOrDrop(ctx.getPlayer(), PCBStackItem.forSchematic(oldSchematic.getFirst()));
                }
                tile.markDirtyAndSync();
            }
            return ActionResultType.SUCCESS;
        });
    }

    private static SelectionShapes makeViewDesignInteraction(LogicCabinetTile tile) {
        final VoxelShape shape = VoxelShapes.create(
                15 / 16., 1 / 16., 4 / 16.,
                1, 11 / 16., 12 / 16.
        );
        return new SingleShape(
                shape, ctx -> {
            final PlayerEntity player = ctx.getPlayer();
            if (player == null) {
                return ActionResultType.PASS;
            }
            if (player instanceof ServerPlayerEntity && tile.circuit != null) {
                LogicDesignContainer.makeProvider(tile.world, tile.pos, true)
                        .open((ServerPlayerEntity) player);
            }
            return ActionResultType.SUCCESS;
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
}
