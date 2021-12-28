package malte0811.controlengineering.blockentity.panels;

import malte0811.controlengineering.blockentity.base.CEBlockEntity;
import malte0811.controlengineering.blockentity.base.IHasMaster;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.bus.BusEmitterCombiner;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.IBusInterface;
import malte0811.controlengineering.bus.MarkDirtyHandler;
import malte0811.controlengineering.controlpanels.PanelData;
import malte0811.controlengineering.controlpanels.PanelSelectionShapes;
import malte0811.controlengineering.controlpanels.PanelTransform;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.util.Clearable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ControlPanelBlockEntity extends CEBlockEntity implements IBusInterface, SelectionShapeOwner,
        IHasMaster<ControlPanelBlockEntity> {
    private final MarkDirtyHandler markBusDirty = new MarkDirtyHandler();
    private List<PlacedComponent> components = new ArrayList<>();
    private PanelTransform transform = new PanelTransform(
            0.25F,
            (float) Math.toDegrees(Math.atan(0.5)),
            PanelOrientation.DOWN_NORTH
    );
    private BusState inputState = BusState.EMPTY;
    private final BusEmitterCombiner<Integer> stateHandler = new BusEmitterCombiner<>(
            i -> components.get(i).getComponent().getEmittedState(),
            i -> components.get(i).getComponent().updateTotalState(getTotalState())
    );

    public ControlPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        resetStateHandler();
    }

    private void resetStateHandler() {
        stateHandler.clear();
        for (int i = 0; i < components.size(); ++i) {
            stateHandler.addEmitter(i);
        }
        updateBusState(SyncType.NEVER);
    }

    public void updateBusState(SyncType sync) {
        BusState oldState = stateHandler.getTotalEmittedState();
        stateHandler.updateState(this.inputState);
        boolean changed = !oldState.equals(stateHandler.getTotalEmittedState());
        if (changed) {
            markBusDirty.run();
        }
        if (sync.shouldSync(changed) && level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 0);
        }
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        readComponentsAndTransform(nbt);
    }

    public void readComponentsAndTransform(CompoundTag nbt) {
        final PanelData data = new PanelData(nbt, getBlockState().getValue(PanelOrientation.PROPERTY));
        this.transform = data.getTransform();
        this.components = data.getComponents();
        if (level != null && !level.isClientSide) {
            resetStateHandler();
        }
    }

    @Override
    public void setLevel(@Nonnull Level pLevel) {
        super.setLevel(pLevel);
        if (!level.isClientSide) {
            resetStateHandler();
        }
    }

    @Override
    protected void writeSyncedData(CompoundTag out) {
        out.merge(new PanelData(this).toNBT());
    }

    @Override
    protected void readSyncedData(CompoundTag in) {
        readComponentsAndTransform(in);
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        super.saveAdditional(compound);
        writeSyncedData(compound);
    }

    public List<PlacedComponent> getComponents() {
        return components;
    }

    public PanelTransform getTransform() {
        return transform;
    }

    private BusState getTotalState() {
        return stateHandler.getTotalState();
    }

    @Override
    public void onBusUpdated(BusState totalState, BusState otherState) {
        if (!totalState.equals(inputState)) {
            this.inputState = totalState;
            this.updateBusState(SyncType.IF_CHANGED);
        }
    }

    @Override
    public BusState getEmittedState() {
        return this.stateHandler.getTotalEmittedState();
    }

    @Override
    public boolean canConnect(Direction fromSide) {
        //TODO? At least forbid for panel top?
        return true;
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

    @Override
    public SelectionShapes getShape() {
        BlockState state = getBlockState();
        if (!state.hasProperty(PanelBlock.IS_BASE) || state.getValue(PanelBlock.IS_BASE)) {
            return SingleShape.FULL_BLOCK;
        }
        ControlPanelBlockEntity base = PanelBlock.getBase(level, state, worldPosition);
        if (base == null) {
            return SingleShape.FULL_BLOCK;
        }
        return new PanelSelectionShapes(base);
    }

    public PanelData getData() {
        return new PanelData(this);
    }

    @Nullable
    @Override
    public ControlPanelBlockEntity computeMasterBE(BlockState stateHere) {
        return PanelBlock.getBase(level, stateHere, worldPosition);
    }

    public enum SyncType {
        NEVER,
        IF_CHANGED,
        ALWAYS;

        public boolean shouldSync(boolean changed) {
            return switch (this) {
                case NEVER -> false;
                case IF_CHANGED -> changed;
                case ALWAYS -> true;
            };
        }
    }
}
