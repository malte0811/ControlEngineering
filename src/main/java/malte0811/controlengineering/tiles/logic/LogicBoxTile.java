package malte0811.controlengineering.tiles.logic;

import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.blocks.logic.LogicBoxBlock;
import malte0811.controlengineering.blocks.shapes.ListShapes;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.IBusInterface;
import malte0811.controlengineering.bus.MarkDirtyHandler;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Leafcells;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.logic.circuit.BusConnectedCircuit;
import malte0811.controlengineering.logic.circuit.Circuit;
import malte0811.controlengineering.logic.circuit.CircuitBuilder;
import malte0811.controlengineering.logic.circuit.NetReference;
import malte0811.controlengineering.logic.clock.ClockGenerator;
import malte0811.controlengineering.logic.clock.ClockGenerator.ClockInstance;
import malte0811.controlengineering.logic.clock.ClockTypes;
import malte0811.controlengineering.logic.model.DynamicLogicModel;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.util.CachedValue;
import malte0811.controlengineering.util.Clearable;
import malte0811.controlengineering.util.ItemUtil;
import malte0811.controlengineering.util.Matrix4;
import malte0811.controlengineering.util.energy.CEEnergyStorage;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
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

public class LogicBoxTile extends TileEntity implements SelectionShapeOwner, IBusInterface, ITickableTileEntity {
    private final CEEnergyStorage energy = new CEEnergyStorage(2048, 2 * 128, 128);
    private BusConnectedCircuit circuit;
    @Nonnull
    private ClockInstance<?> clock = ClockTypes.NEVER.newInstance();
    private final MarkDirtyHandler markBusDirty = new MarkDirtyHandler();
    private int numTubes;

    public LogicBoxTile() {
        super(CETileEntities.LOGIC_BOX.get());
        NetReference inA = new NetReference("A");
        NetReference inB = new NetReference("B");
        NetReference out = new NetReference("out");
        Circuit defaultCircuit = CircuitBuilder.builder()
                .addInputNet(inA, SignalType.ANALOG)
                .addInputNet(inB, SignalType.ANALOG)
                .addStage()
                .addCell(Leafcells.AND2.newInstance())
                .input(0, inA)
                .input(1, inB)
                .output(0, out)
                .buildCell()
                .buildStage()
                .build();
        setCircuit(new BusConnectedCircuit(
                defaultCircuit,
                ImmutableMap.of(out, ImmutableList.of(new BusSignalRef(0, 2))),
                ImmutableMap.of(
                        new BusSignalRef(0, 0), ImmutableList.of(inA),
                        new BusSignalRef(0, 1), ImmutableList.of(inB)
                ),
                ImmutableMap.of()
        ));
    }

    @Override
    public void tick() {
        if (world.isRemote || isUpper()) {
            return;
        }
        //TODO less? config?
        if (energy.extractOrTrue(128) || world.getGameTime() % 2 != 0) {
            return;
        }
        final Direction facing = getFacing();
        final Direction clockFace = facing.rotateYCCW();
        boolean rsIn = world.getRedstonePower(pos.offset(clockFace), clockFace.getOpposite()) > 0;
        if (!clock.tick(rsIn)) {
            return;
        }
        // Inputs are updated in onBusUpdated
        if (circuit.tick()) {
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
        setCircuit(new BusConnectedCircuit(nbt.getCompound("circuit")));
        energy.readNBT(nbt.get("energy"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        compound = super.write(compound);
        compound.put("clock", Codecs.encode(ClockInstance.CODEC, clock));
        compound.put("circuit", circuit.toNBT());
        compound.put("energy", energy.writeNBT());
        return compound;
    }

    private CompoundNBT writeDynamicSyncNBT(CompoundNBT result) {
        result.putBoolean("hasClock", clock.getType().isActiveClock());
        return result;
    }

    private void readDynamicSyncNBT(CompoundNBT tag) {
        if (tag.getBoolean("hasClock"))
            clock = ClockTypes.ALWAYS_ON.newInstance();
        else
            clock = ClockTypes.NEVER.newInstance();
        requestModelDataUpdate();
        world.notifyBlockUpdate(
                pos, getBlockState(), getBlockState(), Constants.BlockFlags.DEFAULT
        );
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        numTubes = tag.getInt("numTubes");
        readDynamicSyncNBT(tag);
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT result = writeDynamicSyncNBT(super.getUpdateTag());
        result.putInt("numTubes", numTubes);
        return result;
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
        circuit.updateInputs(newState);
    }

    @Override
    public BusState getEmittedState() {
        return circuit.getOutputState();
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

    public void setCircuit(BusConnectedCircuit circuit) {
        this.circuit = circuit;
        this.numTubes = MathHelper.ceil(circuit.getCircuit()
                .getCellTypes()
                .mapToDouble(LeafcellType::getNumTubes)
                .sum());
    }

    private Direction getFacing() {
        return getBlockState().get(LogicBoxBlock.FACING);
    }

    private boolean isUpper() {
        return getBlockState().get(LogicBoxBlock.HEIGHT) != 0;
    }

    private final CachedValue<Pair<Direction, Boolean>, SelectionShapes> selectionShapes = new CachedValue<>(
            () -> Pair.of(getFacing(), isUpper()),
            f -> {
                if (f.getSecond()) {
                    return new SingleShape(LogicBoxBlock.TOP_SHAPE.apply(f.getFirst()), $ -> ActionResultType.PASS);
                } else {
                    return createSelectionShapes(f.getFirst(), this);
                }
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

    private static SelectionShapes createSelectionShapes(Direction d, LogicBoxTile tile) {
        List<SelectionShapes> subshapes = new ArrayList<>(1);
        // Add clear tape to input/take it from input
        subshapes.add(new SingleShape(
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
        }));
        return new ListShapes(
                LogicBoxBlock.BOTTOM_SHAPE.apply(d), Matrix4.inverseFacing(d), subshapes, $ -> ActionResultType.PASS
        );
    }
}
