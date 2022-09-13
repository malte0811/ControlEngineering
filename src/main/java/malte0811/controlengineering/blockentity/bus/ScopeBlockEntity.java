package malte0811.controlengineering.blockentity.bus;

import malte0811.controlengineering.blockentity.base.CEBlockEntity;
import malte0811.controlengineering.blocks.bus.ScopeBlock;
import malte0811.controlengineering.blocks.shapes.ListShapes;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.IBusInterface;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.scope.ScopeModule;
import malte0811.controlengineering.scope.ScopeModuleInstance;
import malte0811.controlengineering.scope.ScopeModules;
import malte0811.controlengineering.util.*;
import malte0811.controlengineering.util.math.MatrixUtils;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

// TODO power consumption (respecting disabled modules? UI on-switch?)
public class ScopeBlockEntity extends CEBlockEntity implements SelectionShapeOwner, IBusInterface {
    private static final MyCodec<List<ScopeModuleInstance<?>>> MODULES_CODEC = MyCodecs.list(ScopeModuleInstance.CODEC);
    private static final MyCodec<List<ScopeModuleInstance<?>>> SYNC_MODULES_CODEC = MyCodecs.list(
            MyCodecs.RESOURCE_LOCATION.xmap(
                    rl -> ScopeModules.REGISTRY.getOrDefault(rl, ScopeModules.NONE).newInstance(),
                    i -> i.getType().getRegistryName()
            )
    );
    private List<ScopeModuleInstance<?>> modules = new ArrayList<>(List.of(
            ScopeModules.NONE.newInstance(),
            ScopeModules.NONE.newInstance(),
            ScopeModules.NONE.newInstance(),
            ScopeModules.NONE.newInstance()
    ));
    private final CachedValue<ShapeKey, SelectionShapes> shape = new CachedValue<>(
            () -> new ShapeKey(getFacing(), getModuleTypes().toList()),
            key -> makeShapes(key, this)
    );
    private BusState currentBusState = BusState.EMPTY;

    public ScopeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        modules = MODULES_CODEC.fromNBT(tag.get("modules"), ArrayList::new);
        int width = modules.stream().mapToInt(smi -> smi.getType().getWidth()).sum();
        for (; width < 4; ++width) { modules.add(ScopeModules.NONE.newInstance()); }
        currentBusState = BusState.CODEC.fromNBT(tag.get("busInput"), () -> BusState.EMPTY);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("modules", MODULES_CODEC.toNBT(this.modules));
        tag.put("busInput", BusState.CODEC.toNBT(this.currentBusState));
    }

    @Override
    protected void writeSyncedData(CompoundTag out) {
        super.writeSyncedData(out);
        out.put("modules", SYNC_MODULES_CODEC.toNBT(this.modules));
    }

    @Override
    protected void readSyncedData(CompoundTag in) {
        super.readSyncedData(in);
        final var oldModuleList = getModuleTypes().toList();
        modules = SYNC_MODULES_CODEC.fromNBT(in.get("modules"), ArrayList::new);
        if (level != null && !oldModuleList.equals(getModuleTypes().toList())) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public Stream<ScopeModule<?>> getModuleTypes() {
        return modules.stream().map(ScopeModuleInstance::getType);
    }

    public List<ScopeModuleInstance<?>> getModules() {
        return modules;
    }

    @Override
    public SelectionShapes getShape() {
        return shape.get();
    }

    private InteractionResult interactWithModule(int moduleID, UseOnContext ctx) {
        // TODO support for locking modules in place with a screwdriver?
        if (level == null) { return InteractionResult.PASS; }
        final var targetedModule = modules.get(moduleID).getType();
        if (!targetedModule.isEmpty()) {
            if (!level.isClientSide()) {
                modules.remove(moduleID);
                for (int i = 0; i < targetedModule.getWidth(); ++i) {
                    modules.add(moduleID, ScopeModules.NONE.newInstance());
                }
                final var moduleItem = CEItems.SCOPE_MODULES.get(targetedModule.getRegistryName());
                final var player = ctx.getPlayer();
                if (moduleItem != null && player != null) {
                    ItemUtil.giveOrDrop(player, moduleItem.get().getDefaultInstance());
                }
            }
            return InteractionResult.SUCCESS;
        }
        final var held = ctx.getItemInHand();
        final var newModule = ScopeModules.REGISTRY.get(ForgeRegistries.ITEMS.getKey(held.getItem()));
        if (newModule == null) { return InteractionResult.PASS; }
        for (int offset = 1; offset < newModule.getWidth(); ++offset) {
            final var offsetId = moduleID + offset;
            if (offsetId >= modules.size() || !modules.get(offsetId).getType().isEmpty()) {
                return InteractionResult.FAIL;
            }
        }
        if (!level.isClientSide()) {
            modules.set(moduleID, newModule.newInstance());
            held.shrink(1);
            for (int extraSlot = 1; extraSlot < newModule.getWidth(); ++extraSlot) {
                modules.remove(moduleID + 1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static SelectionShapes makeShapes(ShapeKey key, ScopeBlockEntity bEntity) {
        List<SelectionShapes> subShapes = new ArrayList<>();
        int offset = 0;
        for (int i = 0; i < key.modules().size(); ++i) {
            final var nextModule = key.modules().get(i);
            final var nextOffset = offset + nextModule.getWidth() * 3;
            final var shape = ShapeUtils.createPixelRelative(2 + offset, 1, 8, 2 + nextOffset, 8, 15);
            final var finalI = i;
            subShapes.add(new SingleShape(shape, ctx -> {
                final var result = bEntity.interactWithModule(finalI, ctx);
                if (!bEntity.level.isClientSide) {
                    ScopeModuleInstance.ensureOneTriggerActive(bEntity.getModules());
                    BEUtil.markDirtyAndSync(bEntity);
                }
                return result;
            }));
            offset = nextOffset;
        }
        return new ListShapes(
                ScopeBlock.SHAPE.apply(key.facing()),
                MatrixUtils.inverseFacing(key.facing()),
                subShapes,
                ctx -> {
                    if (ctx.getPlayer() instanceof ServerPlayer serverPlayer) {
                        NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                                CEContainers.SCOPE.argConstructor(bEntity), Component.empty()
                        ));
                    }
                    return InteractionResult.SUCCESS;
                }
        );
    }

    @Override
    public void onBusUpdated(BusState totalState, BusState otherState) {
        currentBusState = totalState;
    }

    @Override
    public BusState getEmittedState() {
        return BusState.EMPTY;
    }

    @Override
    public boolean canConnect(Direction fromSide) {
        return fromSide == getFacing().getClockWise();
    }

    @Override
    public void addMarkDirtyCallback(Clearable<Runnable> markDirty) { }

    public Direction getFacing() {
        return getBlockState().getValue(ScopeBlock.FACING);
    }

    private record ShapeKey(Direction facing, List<ScopeModule<?>> modules) { }
}
