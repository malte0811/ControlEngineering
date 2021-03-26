package malte0811.controlengineering.tiles.logic;

import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import malte0811.controlengineering.blocks.logic.LogicBoxBlock;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.bus.*;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Leafcells;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.logic.circuit.Circuit;
import malte0811.controlengineering.logic.circuit.CircuitBuilder;
import malte0811.controlengineering.logic.circuit.NetReference;
import malte0811.controlengineering.logic.clock.ClockGenerator.ClockInstance;
import malte0811.controlengineering.logic.clock.ClockTypes;
import malte0811.controlengineering.logic.model.DynamicLogicModel;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.util.Clearable;
import malte0811.controlengineering.util.energy.CEEnergyStorage;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LogicBoxTile extends TileEntity implements SelectionShapeOwner, IBusInterface, ITickableTileEntity {
    private static final BiMap<BusSignalRef, NetReference> INPUT_NETS = HashBiMap.create();
    private static final BiMap<BusSignalRef, NetReference> OUTPUT_NETS = HashBiMap.create();

    static {
        for (int color = 0; color < 16; ++color) {
            for (int line = 0; line < BusWireTypes.MAX_BUS_WIDTH; ++line) {
                INPUT_NETS.put(new BusSignalRef(line, color), new NetReference("in_" + line + "_" + color));
                OUTPUT_NETS.put(new BusSignalRef(line, color), new NetReference("out_" + line + "_" + color));
            }
        }
    }

    private final CEEnergyStorage energy = new CEEnergyStorage(2048, 2 * 128, 128);
    private Circuit circuit;
    @Nonnull
    private ClockInstance<?> clock = ClockTypes.NEVER.newInstance();
    private BusState outputValues = new BusState(BusWireTypes.MAX_BUS_WIDTH);
    private final MarkDirtyHandler markBusDirty = new MarkDirtyHandler();
    private int numTubes;

    public LogicBoxTile() {
        super(CETileEntities.LOGIC_BOX.get());
        setCircuit(CircuitBuilder.builder()
                .addInputNet(INPUT_NETS.get(new BusSignalRef(0, 0)), SignalType.DIGITAL)
                .addInputNet(INPUT_NETS.get(new BusSignalRef(0, 1)), SignalType.DIGITAL)
                .addStage()
                .addCell(Leafcells.BASIC_LOGIC.get(Pair.of(IBooleanFunction.AND, 2)).newInstance())
                .input(0, INPUT_NETS.get(new BusSignalRef(0, 0)))
                .input(1, INPUT_NETS.get(new BusSignalRef(0, 1)))
                .output(0, OUTPUT_NETS.get(new BusSignalRef(0, 2)))
                .buildCell()
                .buildStage()
                .build());
    }

    @Override
    public void tick() {
        if (world.isRemote || getBlockState().get(LogicBoxBlock.HEIGHT) != 0) {
            return;
        }
        //TODO less?
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
        circuit.tick();
        boolean changed = false;
        for (Object2DoubleMap.Entry<NetReference> netWithValue : circuit.getNetValues().object2DoubleEntrySet()) {
            final BusSignalRef signal = OUTPUT_NETS.inverse().get(netWithValue.getKey());
            if (signal != null) {
                final int currentValue = outputValues.getSignal(signal);
                final int newValue = (int) MathHelper.clamp(
                        BusLine.MAX_VALID_VALUE * netWithValue.getDoubleValue(),
                        BusLine.MIN_VALID_VALUE,
                        BusLine.MAX_VALID_VALUE
                );
                if (currentValue != newValue) {
                    outputValues = outputValues.with(signal, newValue);
                    changed = true;
                }
            }
        }
        if (changed) {
            markBusDirty.run();
        }
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
        super.read(state, nbt);
        clock = ClockInstance.fromNBT(nbt.getCompound("clock"));
        if (clock == null) {
            clock = ClockTypes.NEVER.newInstance();
        }
        setCircuit(new Circuit(nbt.getCompound("circuit")));
        energy.readNBT(nbt.get("energy"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        compound = super.write(compound);
        compound.put("clock", clock.toNBT());
        compound.put("circuit", circuit.toNBT());
        compound.put("energy", energy.writeNBT());
        return compound;
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT result = super.getUpdateTag();
        result.putInt("numTubes", numTubes);
        return result;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        numTubes = tag.getInt("numTubes");
        requestModelDataUpdate();
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new SinglePropertyModelData<>(numTubes, DynamicLogicModel.NUM_TUBES);
    }

    @Override
    public SelectionShapes getShape() {
        // TODO
        return SingleShape.FULL_BLOCK;
    }

    @Override
    public void onBusUpdated(BusState newState) {
        for (NetReference inputSignal : circuit.getInputNets()) {
            BusSignalRef busSignal = INPUT_NETS.inverse().get(inputSignal);
            if (busSignal != null) {
                circuit.updateInputValue(inputSignal, newState.getSignal(busSignal) / (double) BusLine.MAX_VALID_VALUE);
            }
        }
    }

    @Override
    public BusState getEmittedState() {
        return outputValues;
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

    public void setCircuit(Circuit circuit) {
        this.circuit = circuit;
        this.numTubes = MathHelper.ceil(circuit.getCellTypes()
                .mapToDouble(LeafcellType::getNumTubes)
                .sum());
    }

    private Direction getFacing() {
        return getBlockState().get(LogicBoxBlock.FACING);
    }
}
