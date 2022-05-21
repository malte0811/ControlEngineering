package malte0811.controlengineering.blockentity.tape;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.base.CEBlockEntity;
import malte0811.controlengineering.blockentity.logic.ClockSlot;
import malte0811.controlengineering.blocks.shapes.ListShapes;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.blocks.tape.SequencerBlock;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.IBusInterface;
import malte0811.controlengineering.bus.MarkDirtyHandler;
import malte0811.controlengineering.util.*;
import malte0811.controlengineering.util.energy.CEEnergyStorage;
import malte0811.controlengineering.util.math.MatrixUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static malte0811.controlengineering.util.ShapeUtils.createPixelRelative;

public class SequencerBlockEntity extends CEBlockEntity implements SelectionShapeOwner, IBusInterface {
    private static final int BASE_CONSUMPTION = 16;
    private static final int CONSUMPTION_PER_STEP = 128;
    public static final String COMPACT_KEY = ControlEngineering.MODID + ".gui.sequencer.compact";
    public static final String ANALOG_KEY = ControlEngineering.MODID + ".gui.sequencer.analog";
    public static final String AUTORESET_KEY = ControlEngineering.MODID + ".gui.sequencer.autoreset";
    public static final String MANUAL_RESET_KEY = ControlEngineering.MODID + ".gui.sequencer.manualreset";

    private boolean compact = true;
    private boolean autoreset = true;
    private final TapeDrive tape = new TapeDrive(this::onTapeChange, this::onTapeChange, () -> true);
    private final ClockSlot clock = new ClockSlot();
    private final CEEnergyStorage energy = new CEEnergyStorage(20 * CONSUMPTION_PER_STEP, 2 * CONSUMPTION_PER_STEP, 0);
    private int currentTapePosition = 0;
    private final MarkDirtyHandler markBusDirty = new MarkDirtyHandler();

    public SequencerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean isCompact() {
        return compact;
    }

    public boolean isAutoreset() {
        return autoreset;
    }

    public int getTapeLength() {
        return tape.getTapeLength();
    }

    public int getCurrentTapePosition() {
        return currentTapePosition;
    }

    public void tick() {
        if (!clock.isPresent() || !tape.hasTape() || currentTapePosition >= tape.getTapeLength() || level == null) {
            return;
        }
        if (energy.extractOrTrue(BASE_CONSUMPTION) || level.getGameTime() % 2 != 0) {
            return;
        }
        final Direction clockFace = getBlockState().getValue(SequencerBlock.FACING).getCounterClockWise();
        boolean rsIn = level.getSignal(worldPosition.relative(clockFace), clockFace) > 0;
        if (clock.getClock().tick(rsIn) && !energy.extractOrTrue(CONSUMPTION_PER_STEP)) {
        	BlockState blockState = getBlockState();
        	
            int newPos = ++currentTapePosition;
            if (newPos < tape.getTapeLength()) {
            	level.setBlockAndUpdate(worldPosition, blockState.setValue(SequencerBlock.HALTED, false));
                currentTapePosition = newPos;
            } else {
            	if (autoreset) {
            		currentTapePosition = 0;
            	}
                level.setBlockAndUpdate(worldPosition, blockState.setValue(SequencerBlock.HALTED, true));
            }
            BEUtil.markDirtyAndSync(this);
        }
    }

    @Override
    public void load(@Nonnull CompoundTag pTag) {
        super.load(pTag);
        readSharedData(pTag);
        tape.loadNBT(pTag.get("tape"));
        clock.load(pTag.get("clock"));
        energy.readNBT(pTag.get("energy"));
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag pTag) {
        super.saveAdditional(pTag);
        writeSharedData(pTag);
        pTag.put("tape", tape.toNBT());
        pTag.put("clock", clock.toNBT());
        pTag.put("energy", energy.writeNBT());
    }

    @Override
    protected void readSyncedData(CompoundTag in) {
        super.readSyncedData(in);
        final boolean oldCompact = compact;
        final boolean oldAutoreset = autoreset;
        final boolean hadClock = hasClock();
        readSharedData(in);
        clock.loadClientNBT(in.get("hasClock"));
        tape.loadClientNBT(in.get("syncTape"));
        if ((oldCompact != compact || oldAutoreset != autoreset || hadClock != hasClock()) && level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void writeSyncedData(CompoundTag out) {
        super.writeSyncedData(out);
        writeSharedData(out);
        out.put("hasClock", clock.toClientNBT());
        out.put("syncTape", tape.toClientNBT());
    }

    private void readSharedData(CompoundTag in) {
        compact = in.getBoolean("compact");
        autoreset = in.getBoolean("autoreset");
        currentTapePosition = in.getInt("tapePosition");
    }

    private void writeSharedData(CompoundTag out) {
        out.putBoolean("compact", compact);
        out.putBoolean("autoreset", autoreset);
        out.putInt("tapePosition", currentTapePosition);
    }

    public boolean hasClock() {
        return clock.getType().isActiveClock();
    }

    private final CachedValue<Direction, SelectionShapes> shapes = new CachedValue<>(
            () -> getBlockState().getValue(SequencerBlock.FACING), this::makeSelectionShapes
    );

    private SelectionShapes makeSelectionShapes(Direction d) {
        List<SelectionShapes> shapes = new ArrayList<>();
        shapes.add(new SingleShape(createPixelRelative(2, 4, 14, 14, 12, 16), tape::click));
        shapes.add(new SingleShape(createPixelRelative(4, 3, 0, 6, 6, 1), $ -> {
            if (level != null && !level.isClientSide()) {
                autoreset = !autoreset;
                BEUtil.markDirtyAndSync(this);
            }
            return InteractionResult.SUCCESS;
        }).setTextGetter(() -> new TranslatableComponent(autoreset ? AUTORESET_KEY : MANUAL_RESET_KEY)));
        shapes.add(new SingleShape(createPixelRelative(10, 3, 0, 12, 6, 1), $ -> {
            if (level != null && !level.isClientSide()) {
                compact = !compact;
                BEUtil.markDirtyAndSync(this);
            }
            return InteractionResult.SUCCESS;
        }).setTextGetter(() -> new TranslatableComponent(compact ? COMPACT_KEY : ANALOG_KEY)));
        shapes.add(new SingleShape(
                ShapeUtils.createPixelRelative(0, 6, 6, 5, 10, 10),
                ctx -> clock.click(ctx, () -> BEUtil.markDirtyAndSync(this))
        ));
        return new ListShapes(
                Shapes.block(), MatrixUtils.inverseFacing(d), shapes, $ -> InteractionResult.PASS
        );
    }

    @Override
    public SelectionShapes getShape() {
        return shapes.get();
    }

    private void onTapeChange() {
        this.currentTapePosition = 0;
        BEUtil.markDirtyAndSync(this);
    }

    private final LazyOptional<IEnergyStorage> energyCap = CapabilityUtils.constantOptional(energy);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY && (side == Direction.UP || side == null)) {
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
    public void onBusUpdated(BusState totalState, BusState otherState) {}

    @Override
    public BusState getEmittedState() {
        if (!tape.hasTape() || currentTapePosition >= tape.getTapeContent().length) {
            return BusState.EMPTY;
        }
        final byte toSend = tape.getTapeContent()[currentTapePosition];
        if (compact) {
            int[] line = new int[BusLine.LINE_SIZE];
            for (int i = 0; i < Byte.SIZE; ++i) {
                if (BitUtils.getBit(toSend, i)) {
                    line[i] = BusLine.MAX_VALID_VALUE;
                }
            }
            return BusState.EMPTY.withLine(0, new BusLine(line));
        } else {
            int color = RedstoneTapeUtils.getColorId(toSend);
            int strength = RedstoneTapeUtils.getStrength(toSend);
            return BusState.EMPTY.with(0, color, strength * 17);
        }
    }

    @Override
    public boolean canConnect(Direction fromSide) {
        return fromSide == getBlockState().getValue(SequencerBlock.FACING).getClockWise();
    }

    @Override
    public void addMarkDirtyCallback(Clearable<Runnable> markDirty) {
        markBusDirty.addCallback(markDirty);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        markBusDirty.run();
    }
}
