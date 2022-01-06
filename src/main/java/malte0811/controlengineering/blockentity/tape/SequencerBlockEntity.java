package malte0811.controlengineering.blockentity.tape;

import malte0811.controlengineering.blockentity.base.CEBlockEntity;
import malte0811.controlengineering.blockentity.logic.ClockSlot;
import malte0811.controlengineering.blocks.shapes.ListShapes;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.blocks.tape.SequencerBlock;
import malte0811.controlengineering.util.BEUtil;
import malte0811.controlengineering.util.CachedValue;
import malte0811.controlengineering.util.ShapeUtils;
import malte0811.controlengineering.util.math.MatrixUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static malte0811.controlengineering.util.ShapeUtils.createPixelRelative;

public class SequencerBlockEntity extends CEBlockEntity implements SelectionShapeOwner {
    private final TapeDrive tape = new TapeDrive(this::onTapeChange, this::onTapeChange, () -> true);
    private boolean compact = true;
    //TODO is this ever a useful feature?
    private boolean autoreset = true;
    private final ClockSlot clock = new ClockSlot();
    private int currentTapePosition = 0;

    public SequencerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean isCompact() {
        return compact;
    }

    public boolean isAutoreset() {
        return autoreset;
    }

    @Override
    public void load(@Nonnull CompoundTag pTag) {
        super.load(pTag);
        readSharedData(pTag);
        tape.loadNBT(pTag.get("tape"));
        clock.load(pTag.get("clock"));
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag pTag) {
        super.saveAdditional(pTag);
        writeSharedData(pTag);
        pTag.put("tape", tape.toNBT());
        pTag.put("clock", clock.toNBT());
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
        }));
        shapes.add(new SingleShape(createPixelRelative(10, 3, 0, 12, 6, 1), $ -> {
            if (level != null && !level.isClientSide()) {
                compact = !compact;
                BEUtil.markDirtyAndSync(this);
            }
            return InteractionResult.SUCCESS;
        }));
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
}
