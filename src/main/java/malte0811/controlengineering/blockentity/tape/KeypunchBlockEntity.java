package malte0811.controlengineering.blockentity.tape;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.MultiblockBEType;
import malte0811.controlengineering.blockentity.base.CEBlockEntity;
import malte0811.controlengineering.blockentity.base.IExtraDropBE;
import malte0811.controlengineering.blockentity.base.IHasMaster;
import malte0811.controlengineering.blockentity.bus.IParallelPortOwner;
import malte0811.controlengineering.blockentity.bus.ParallelPort;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.shapes.ListShapes;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.blocks.tape.KeypunchBlock;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.IBusInterface;
import malte0811.controlengineering.bus.MarkDirtyHandler;
import malte0811.controlengineering.gui.tape.KeypunchMenu;
import malte0811.controlengineering.items.EmptyTapeItem;
import malte0811.controlengineering.items.PunchedTapeItem;
import malte0811.controlengineering.util.BEUtil;
import malte0811.controlengineering.util.BitUtils;
import malte0811.controlengineering.util.CachedValue;
import malte0811.controlengineering.util.Clearable;
import malte0811.controlengineering.util.math.MatrixUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static malte0811.controlengineering.util.ShapeUtils.createPixelRelative;

public class KeypunchBlockEntity extends CEBlockEntity
        implements IExtraDropBE, IBusInterface, SelectionShapeOwner, IParallelPortOwner {
    public static final VoxelShape SWITCH_SHAPE = createPixelRelative(6, 13, 0, 10, 16, 1);
    public static final VoxelShape CONNECTOR_SHAPE = createPixelRelative(4, 4, 0, 12, 12, 1);
    public static final VoxelShape INPUT_SHAPE = createPixelRelative(11, 3, 2, 15, 6, 4);
    public static final VoxelShape OUTPUT_SHAPE = createPixelRelative(2, 3, 1, 6, 7, 5);
    public static final String LOOPBACK_KEY = ControlEngineering.MODID + ".gui.loopback";
    public static final String REMOTE_KEY = ControlEngineering.MODID + ".gui.no_loopback";

    private final MarkDirtyHandler markBusDirty = new MarkDirtyHandler();
    private final Set<KeypunchMenu> onTapeChanged = new ReferenceOpenHashSet<>();
    private KeypunchState state = new KeypunchState(this::setChanged);
    private boolean loopback = true;
    private final ParallelPort busInterface = new ParallelPort();

    private final CachedValue<Direction, SelectionShapes> selectionShapes = new CachedValue<>(
            () -> getBlockState().getValue(KeypunchBlock.FACING),
            f -> createSelectionShapes(f, this)
    );

    public KeypunchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tickServer() {
        if (loopback) {
            return;
        }
        if (busInterface.tickTX()) {
            markBusDirty.run();
        }
        busInterface.tickRX().ifPresent(read -> {
            state.tryTypeChar(read, false);
            for (KeypunchMenu c : onTapeChanged) {
                c.onTypedOnServer(read);
            }
            setChanged();
        });
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        readSyncedData(nbt);
        state = new KeypunchState(this::setChanged, nbt.get("state"));
        onTapeChanged.forEach(KeypunchMenu::resyncFullTape);
        busInterface.readNBT(nbt.getCompound("busInterface"));
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        super.saveAdditional(compound);
        writeSyncedData(compound);
        compound.put("state", state.toNBT());
        compound.put("busInterface", busInterface.toNBT());
    }

    @Override
    protected void readSyncedData(CompoundTag in) {
        super.readSyncedData(in);
        final boolean oldLoopback = loopback;
        loopback = in.getBoolean("loopback");
        if (loopback != oldLoopback && level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void writeSyncedData(CompoundTag out) {
        super.writeSyncedData(out);
        out.putBoolean("loopback", loopback);
    }

    public KeypunchState getState() {
        return state;
    }

    @Override
    public void getExtraDrops(Consumer<ItemStack> dropper) {
        if (state.getAvailable() > 0) {
            dropper.accept(EmptyTapeItem.withLength(state.getAvailable()));
        }
        if (!state.getData().isEmpty() || state.getErased() > 0) {
            byte[] bytes = new byte[state.getData().size() + state.getErased()];
            System.arraycopy(state.getData().toByteArray(), 0, bytes, 0, state.getData().size());
            Arrays.fill(bytes, state.getData().size(), bytes.length, BitUtils.fixParity((byte) 0xff));
            dropper.accept(PunchedTapeItem.withBytes(bytes));
        }
    }

    public static MultiblockBEType<KeypunchBlockEntity, ?> register(DeferredRegister<BlockEntityType<?>> register) {
        return MultiblockBEType.makeType(
                register, "keypunch", KeypunchBlockEntity::new, Dummy::new, CEBlocks.KEYPUNCH, KeypunchBlock::isMaster
        );
    }

    @Override
    public void onBusUpdated(BusState totalState, BusState otherState) {
        if (!loopback) {
            busInterface.onBusStateChange(otherState);
            setChanged();
        }
    }

    @Override
    public BusState getEmittedState() {
        if (loopback) {
            return BusState.EMPTY;
        } else {
            return busInterface.getOutputState();
        }
    }

    @Override
    public boolean canConnect(Direction fromSide) {
        return fromSide == getBlockState().getValue(KeypunchBlock.FACING);
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

    public boolean isLoopback() {
        return loopback;
    }

    public void queueForRemotePrint(byte toAdd) {
        Preconditions.checkState(!loopback);
        busInterface.queueChar(BitUtils.fixParity(toAdd));
    }

    public Set<KeypunchMenu> getOpenContainers() {
        return onTapeChanged;
    }

    @Override
    public SelectionShapes getShape() {
        return selectionShapes.get();
    }

    private static SelectionShapes createSelectionShapes(Direction d, KeypunchBlockEntity bEntity) {
        List<SelectionShapes> subshapes = new ArrayList<>(1);
        subshapes.add(new SingleShape(SWITCH_SHAPE, $ -> {
            if (!bEntity.level.isClientSide()) {
                bEntity.loopback = !bEntity.loopback;
                BEUtil.markDirtyAndSync(bEntity);
            }
            return InteractionResult.SUCCESS;
        }).setTextGetter(() -> new TranslatableComponent(bEntity.isLoopback() ? LOOPBACK_KEY : REMOTE_KEY)));
        subshapes.add(new SingleShape(CONNECTOR_SHAPE, bEntity.getPort().makeRemapInteraction(bEntity)));
        return new ListShapes(
                Shapes.block(),
                MatrixUtils.inverseFacing(d),
                subshapes,
                ctx -> {
                    CEBlocks.KEYPUNCH.get().openContainer(
                            ctx.getPlayer(), bEntity.getBlockState(), ctx.getLevel(), ctx.getClickedPos()
                    );
                    return InteractionResult.SUCCESS;
                }
        );
    }

    @Override
    public ParallelPort getPort() {
        return busInterface;
    }

    private static class Dummy extends CEBlockEntity implements SelectionShapeOwner, IHasMaster<KeypunchBlockEntity> {
        private final CachedValue<Direction, SelectionShapes> selectionShapes = new CachedValue<>(
                () -> getBlockState().getValue(KeypunchBlock.FACING),
                f -> createSelectionShapes(f, getOrComputeMasterBE(getBlockState()))
        );

        public Dummy(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        @Override
        public SelectionShapes getShape() {
            return selectionShapes.get();
        }

        private static SelectionShapes createSelectionShapes(Direction d, KeypunchBlockEntity bEntity) {
            List<SelectionShapes> subshapes = new ArrayList<>(2);
            // Punched tape output
            subshapes.add(new SingleShape(OUTPUT_SHAPE, ctx -> {
                var result = bEntity.getState().removeWrittenTape(ctx.getPlayer());
                bEntity.onTapeChanged.forEach(KeypunchMenu::resyncFullTape);
                return result;
            }));
            // Add clear tape to input/take it from input
            subshapes.add(new SingleShape(
                    INPUT_SHAPE, ctx -> bEntity.getState().removeOrAddClearTape(ctx.getPlayer(), ctx.getItemInHand())
            ));
            return new ListShapes(
                    KeypunchBlock.SHAPE_PROVIDER.apply(d),
                    MatrixUtils.inverseFacing(d),
                    subshapes,
                    ctx -> {
                        CEBlocks.KEYPUNCH.get().openContainer(
                                ctx.getPlayer(), bEntity.getBlockState(), ctx.getLevel(), ctx.getClickedPos().below()
                        );
                        return InteractionResult.SUCCESS;
                    }
            ).setAllowTargetThrough(true);
        }

        @Nullable
        @Override
        public KeypunchBlockEntity computeMasterBE(BlockState stateHere) {
            var beBelow = level.getBlockEntity(worldPosition.below());
            return beBelow instanceof KeypunchBlockEntity keypunch ? keypunch : null;
        }
    }
}
