package malte0811.controlengineering.tiles.logic;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.BusWireTypes;
import malte0811.controlengineering.bus.IBusInterface;
import malte0811.controlengineering.logic.cells.Leafcells;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.logic.circuit.Circuit;
import malte0811.controlengineering.logic.circuit.CircuitBuilder;
import malte0811.controlengineering.logic.circuit.NetReference;
import malte0811.controlengineering.logic.clock.ClockGenerator;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.util.Clearable;
import malte0811.controlengineering.util.typereg.TypedInstance;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraftforge.energy.EnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LogicBoxTile extends TileEntity implements SelectionShapeOwner, IBusInterface {
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

    private final EnergyStorage energy = new EnergyStorage(2048, 128);
    private Circuit circuit = CircuitBuilder.builder()
            .addInputNet(INPUT_NETS.get(new BusSignalRef(0, 0)), SignalType.DIGITAL)
            .addInputNet(INPUT_NETS.get(new BusSignalRef(0, 1)), SignalType.DIGITAL)
            .addStage()
            .addCell(Leafcells.BASIC_LOGIC.get(Pair.of(IBooleanFunction.AND, 2)).newInstance())
            .input(0, INPUT_NETS.get(new BusSignalRef(0, 0)))
            .input(1, INPUT_NETS.get(new BusSignalRef(0, 1)))
            .output(0, OUTPUT_NETS.get(new BusSignalRef(0, 2)))
            .buildCell()
            .buildStage()
            .build();
    @Nullable
    private TypedInstance<?, ? extends ClockGenerator<?>> clock;

    public LogicBoxTile() {
        super(CETileEntities.LOGIC_BOX.get());
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
        super.read(state, nbt);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        return super.write(compound);
    }

    @Override
    public SelectionShapes getShape() {
        // TODO
        return SingleShape.FULL_BLOCK;
    }

    @Override
    public void onBusUpdated(BusState newState) {

    }

    @Override
    public BusState getEmittedState() {
        return null;
    }

    @Override
    public boolean canConnect(Direction fromSide) {
        return false;
    }

    @Override
    public void addMarkDirtyCallback(Clearable<Runnable> markDirty) {

    }
}
