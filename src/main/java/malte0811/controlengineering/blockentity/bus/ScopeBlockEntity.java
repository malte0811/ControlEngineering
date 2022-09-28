package malte0811.controlengineering.blockentity.bus;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import malte0811.controlengineering.blockentity.base.CEBlockEntity;
import malte0811.controlengineering.blocks.bus.ScopeBlock;
import malte0811.controlengineering.blocks.shapes.ListShapes;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.IBusInterface;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.gui.scope.ScopeMenu;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.network.scope.InitTraces;
import malte0811.controlengineering.network.scope.ScopeSubPacket;
import malte0811.controlengineering.scope.GlobalConfig;
import malte0811.controlengineering.scope.module.ScopeModule;
import malte0811.controlengineering.scope.module.ScopeModuleInstance;
import malte0811.controlengineering.scope.module.ScopeModules;
import malte0811.controlengineering.scope.trace.Traces;
import malte0811.controlengineering.util.*;
import malte0811.controlengineering.util.math.MatrixUtils;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

// TODO power consumption (respecting disabled modules? UI on-switch?)
// TODO once the main on-switch is in, only allow modules to be swapped when the scope is off. This saves a bunch of
//  headaches.
public class ScopeBlockEntity extends CEBlockEntity implements SelectionShapeOwner, IBusInterface {
    private static final int NUM_SLOTS = 4;
    public static final int NUM_HORIZONTAL_DIVS = 8;
    private static final MyCodec<List<ModuleInScope>> MODULES_CODEC = MyCodecs.list(ModuleInScope.CODEC);
    private static final MyCodec<List<ModuleInScope>> SYNC_MODULES_CODEC = MyCodecs.list(ModuleInScope.SYNC_CODEC);

    private List<ModuleInScope> modules = fixModuleList(List.of());
    private final CachedValue<ShapeKey, SelectionShapes> shape = new CachedValue<>(
            () -> new ShapeKey(getFacing(), getModuleTypes().toList()),
            key -> makeShapes(key, this)
    );
    private BusState currentBusState = BusState.EMPTY;
    private GlobalConfig globalConfig = new GlobalConfig();
    private Traces traces = new Traces();
    private final Set<ScopeMenu> openMenus = new ReferenceOpenHashSet<>();

    public ScopeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tickServer() {
        tryStartSweep();
        final var toSend = traces.collectSample(modules, currentBusState);
        if (toSend != null) {
            sendToListening(toSend);
        }
    }

    private void tryStartSweep() {
        boolean shouldStart = false;
        for (final var module : getModules()) {
            if (module.module().checkTriggered(currentBusState)) {
                shouldStart = true;
            }
        }
        if (traces.isSweeping() || !shouldStart || !globalConfig.triggerArmed()) { return; }
        final var packet = InitTraces.createForModules(getModules(), globalConfig.ticksPerDiv());
        packet.process(
                modules,
                new LambdaMutable<>(this::getTraces, t -> this.traces = t),
                new LambdaMutable<>(this::getGlobalConfig, this::setGlobalConfig)
        );
        sendToListening(packet);
    }

    private void sendToListening(ScopeSubPacket.IScopeSubPacket packet) {
        for (final var menu : getOpenMenus()) {
            menu.sendToListeningPlayers(packet);
        }
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        modules = fixModuleList(MODULES_CODEC.fromNBT(tag.get("modules"), ArrayList::new));
        currentBusState = BusState.CODEC.fromNBT(tag.get("busInput"), () -> BusState.EMPTY);
        globalConfig = GlobalConfig.CODEC.fromNBT(tag.get("globalConfig"), GlobalConfig::new);
        traces = Traces.CODEC.fromNBT(tag.get("traces"), Traces::new);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("modules", MODULES_CODEC.toNBT(this.modules));
        tag.put("busInput", BusState.CODEC.toNBT(this.currentBusState));
        tag.put("globalConfig", GlobalConfig.CODEC.toNBT(this.globalConfig));
        tag.put("traces", Traces.CODEC.toNBT(traces));
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
        modules = fixModuleList(SYNC_MODULES_CODEC.fromNBT(in.get("modules"), ArrayList::new));
        if (level != null && !oldModuleList.equals(getModuleTypes().toList())) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public Stream<ScopeModule<?>> getModuleTypes() {
        return modules.stream()
                .map(ModuleInScope::module)
                .map(ScopeModuleInstance::getType);
    }

    public List<ModuleInScope> getModules() {
        return modules;
    }

    @Override
    public SelectionShapes getShape() {
        return shape.get();
    }

    private InteractionResult interactWithModule(int indexOfTarget, UseOnContext ctx) {
        // TODO support for locking modules in place with a screwdriver?
        if (level == null) { return InteractionResult.PASS; }
        final var targetedModule = modules.get(indexOfTarget).type();
        final var targetSlot = modules.get(indexOfTarget).firstSlot();
        if (!targetedModule.isEmpty()) {
            if (!level.isClientSide()) {
                final var dropped = removeModule(indexOfTarget);
                final var player = ctx.getPlayer();
                if (!dropped.isEmpty() && player != null) {
                    ItemUtil.giveOrDrop(player, dropped);
                }
            }
            return InteractionResult.SUCCESS;
        }
        final var held = ctx.getItemInHand();
        final var newModule = ScopeModules.REGISTRY.get(ForgeRegistries.ITEMS.getKey(held.getItem()));
        if (newModule == null) { return InteractionResult.PASS; }
        final var firstSlotAfter = targetSlot + newModule.getWidth();
        if (firstSlotAfter > NUM_SLOTS) { return InteractionResult.FAIL; }
        for (int i = indexOfTarget; i < modules.size(); ++i) {
            final var existingModule = modules.get(i);
            if (firstSlotAfter <= existingModule.firstSlot()) { break; }
            if (!existingModule.type().isEmpty()) {
                return InteractionResult.FAIL;
            }
        }
        if (!level.isClientSide()) {
            insertModule(new ModuleInScope(targetSlot, newModule.newInstance()), indexOfTarget);
            held.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private ItemStack removeModule(int indexToRemove) {
        final var removed = modules.remove(indexToRemove).type();
        for (int i = 0; i < removed.getWidth(); ++i) {
            modules.add(indexToRemove + i, new ModuleInScope(indexToRemove + i, ScopeModules.NONE.newInstance()));
        }
        modules = fixModuleList(modules);
        final var moduleItem = CEItems.SCOPE_MODULES.get(removed.getRegistryName());
        if (moduleItem != null) {
            return moduleItem.get().getDefaultInstance();
        } else {
            return ItemStack.EMPTY;
        }
    }

    private void insertModule(ModuleInScope module, int indexInList) {
        modules.set(indexInList, module);
        final int indexToErase = indexInList + 1;
        while (indexToErase < modules.size() && modules.get(indexToErase).firstSlot < module.firstSlotAfter()) {
            modules.remove(indexToErase);
        }
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
                    ScopeModuleInstance.ensureOneTriggerActive(bEntity.getModules(), -1);
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
        return fromSide == getFacing().getCounterClockWise();
    }

    @Override
    public void addMarkDirtyCallback(Clearable<Runnable> markDirty) { }

    public Direction getFacing() {
        return getBlockState().getValue(ScopeBlock.FACING);
    }

    public Set<ScopeMenu> getOpenMenus() {
        return openMenus;
    }

    public Traces getTraces() {
        return traces;
    }

    public void setTraces(Traces traces) {
        this.traces = traces;
        setChanged();
    }

    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    public void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
        setChanged();
    }

    private static List<ModuleInScope> fixModuleList(List<ModuleInScope> untrusted) {
        List<ModuleInScope> fixed = new ArrayList<>(untrusted);
        fixed.sort(Comparator.comparingInt(ModuleInScope::firstSlot));
        int nextFreeSlot = 0;
        for (int i = 0; i < fixed.size(); ++i) {
            final var next = fixed.get(i);
            if (next.firstSlot() < nextFreeSlot || next.firstSlotAfter() > NUM_SLOTS) {
                // Overlapping modules or modules going beyond the scope
                fixed.remove(i);
                --i;
            } else if (next.firstSlot() > nextFreeSlot) {
                // Fill gaps with empty modules
                fixed.add(i, new ModuleInScope(nextFreeSlot, ScopeModules.NONE.newInstance()));
                ++nextFreeSlot;
            } else {
                nextFreeSlot += next.type().getWidth();
            }
        }
        for (int i = nextFreeSlot; i < NUM_SLOTS; ++i) {
            // If modules add up to <4 slots, fill on the right with empty modules
            fixed.add(new ModuleInScope(i, ScopeModules.NONE.newInstance()));
        }
        return fixed;
    }

    private record ShapeKey(Direction facing, List<ScopeModule<?>> modules) { }

    public record ModuleInScope(int firstSlot, ScopeModuleInstance<?> module) {
        public static final MyCodec<ModuleInScope> CODEC = new RecordCodec2<>(
                MyCodecs.INTEGER.fieldOf("firstSlot", ModuleInScope::firstSlot),
                ScopeModuleInstance.CODEC.fieldOf("module", ModuleInScope::module),
                ModuleInScope::new
        );
        public static final MyCodec<ModuleInScope> SYNC_CODEC = new RecordCodec2<>(
                MyCodecs.INTEGER.fieldOf("firstSlot", ModuleInScope::firstSlot),
                MyCodecs.RESOURCE_LOCATION.<ScopeModuleInstance<?>>xmap(
                        rl -> ScopeModules.REGISTRY.getOrDefault(rl, ScopeModules.NONE).newInstance(),
                        smi -> smi.getType().getRegistryName()
                ).fieldOf("moduleType", ModuleInScope::module),
                ModuleInScope::new
        );

        public int firstSlotAfter() {
            return firstSlot + type().getWidth();
        }

        public ScopeModule<?> type() {
            return module().getType();
        }
    }
}
