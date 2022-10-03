package malte0811.controlengineering.blockentity.bus;

import blusunrize.immersiveengineering.api.IETags;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import malte0811.controlengineering.ControlEngineering;
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
import malte0811.controlengineering.network.scope.SetGlobalCfg;
import malte0811.controlengineering.network.scope.SetGlobalState;
import malte0811.controlengineering.scope.GlobalConfig;
import malte0811.controlengineering.scope.GlobalState;
import malte0811.controlengineering.scope.module.ScopeModule;
import malte0811.controlengineering.scope.module.ScopeModuleInstance;
import malte0811.controlengineering.scope.module.ScopeModules;
import malte0811.controlengineering.scope.trace.Traces;
import malte0811.controlengineering.util.*;
import malte0811.controlengineering.util.math.MatrixUtils;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec3;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

// TODO power consumption (respecting disabled modules? UI on-switch?)
public class ScopeBlockEntity extends CEBlockEntity implements SelectionShapeOwner, IBusInterface {
    private static final int NUM_SLOTS = 4;
    public static final int NUM_HORIZONTAL_DIVS = 8;
    public static final int NUM_VERTICAL_DIVS = 10;
    private static final MyCodec<List<ModuleInScope>> MODULES_CODEC = MyCodecs.list(ModuleInScope.CODEC);
    private static final MyCodec<List<ModuleInScope>> SYNC_MODULES_CODEC = MyCodecs.list(ModuleInScope.SYNC_CODEC);
    public static final String MODULE_LOCKED_KEY = ControlEngineering.MODID + ".gui.scope.moduleLocked";
    public static final String WARN_MODULE_LOCKED_KEY = ControlEngineering.MODID + ".gui.scope.warnModuleLocked";
    public static final String MODULE_UNLOCKED_KEY = ControlEngineering.MODID + ".gui.scope.moduleUnlocked";
    public static final String WARN_SCOPE_POWERED_KEY = ControlEngineering.MODID + ".gui.scope.warnScopePowered";
    private static final int BASE_POWER_PER_TICK = 64;

    private List<ModuleInScope> modules = fixModuleList(List.of());
    private final CachedValue<ShapeKey, SelectionShapes> shape = new CachedValue<>(
            () -> new ShapeKey(getFacing(), getModuleTypes().toList()),
            key -> makeShapes(key, this)
    );
    private BusState currentBusState = BusState.EMPTY;
    private GlobalConfig globalConfig = new GlobalConfig();
    private Traces traces = new Traces();
    private final Set<ScopeMenu> openMenus = new ReferenceOpenHashSet<>();
    private final EnergyStorage energy = new EnergyStorage(BASE_POWER_PER_TICK * 60, 2 * BASE_POWER_PER_TICK) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            final var hadPower = hasPower();
            final var result = super.receiveEnergy(maxReceive, simulate);
            if (hadPower != hasPower()) { sendGlobalStateUpdate(); }
            return result;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            final var hadPower = hasPower();
            final var result = super.extractEnergy(maxExtract, simulate);
            if (hadPower != hasPower()) { sendGlobalStateUpdate(); }
            return result;
        }
    };
    private final LazyOptional<IEnergyStorage> energyCap = CapabilityUtils.constantOptional(energy);

    public ScopeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tickServer() {
        if (!globalConfig.powered()) { return; }
        final int requiredPower = getPowerConsumption();
        final int consumed = energy.extractEnergy(requiredPower, false);
        if (consumed < requiredPower) {
            processAndSend(new SetGlobalCfg(globalConfig.withPowered(false)));
            sendGlobalStateUpdate();
            return;
        }
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
        processAndSend(InitTraces.createForModules(getModules(), globalConfig.ticksPerDiv()));
    }

    private void processAndSend(ScopeSubPacket.IScopeSubPacket packet) {
        packet.process(
                modules,
                new LambdaMutable<>(this::getTraces, t -> this.traces = t),
                new LambdaMutable<>(this::getGlobalConfig, this::setGlobalConfig),
                LambdaMutable.getterOnly(this::getGlobalSyncState)
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
        energy.deserializeNBT(tag.get("energy"));
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("modules", MODULES_CODEC.toNBT(this.modules));
        tag.put("busInput", BusState.CODEC.toNBT(this.currentBusState));
        tag.put("globalConfig", GlobalConfig.CODEC.toNBT(this.globalConfig));
        tag.put("traces", Traces.CODEC.toNBT(traces));
        tag.put("energy", energy.serializeNBT());
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

    private static void sendStatusMessage(UseOnContext ctx, String key) {
        final var player = ctx.getPlayer();
        final var level = ctx.getLevel();
        if (!level.isClientSide() && player != null) {
            player.displayClientMessage(Component.translatable(key), true);
        }
    }

    private InteractionResult interactWithModule(int indexOfTarget, UseOnContext ctx) {
        if (level == null) { return InteractionResult.PASS; }
        final var scopeModule = modules.get(indexOfTarget);
        final var targetedModule = scopeModule.type();
        final var held = ctx.getItemInHand();
        if (held.is(IETags.screwdrivers) && !targetedModule.isEmpty()) {
            if (!level.isClientSide()) {
                modules.set(indexOfTarget, scopeModule.toggleLock());
                sendStatusMessage(ctx, !scopeModule.isLocked() ? MODULE_LOCKED_KEY : MODULE_UNLOCKED_KEY);
                BEUtil.markDirtyAndSync(this);
            }
            return InteractionResult.SUCCESS;
        }
        if (globalConfig.powered()) {
            sendStatusMessage(ctx, WARN_SCOPE_POWERED_KEY);
            return InteractionResult.FAIL;
        }
        if (!targetedModule.isEmpty()) {
            if (scopeModule.isLocked()) {
                sendStatusMessage(ctx, WARN_MODULE_LOCKED_KEY);
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide()) {
                final var dropped = removeModule(indexOfTarget);
                final var player = ctx.getPlayer();
                if (!dropped.isEmpty() && player != null) {
                    ItemUtil.giveOrDrop(player, dropped);
                }
            }
            return InteractionResult.SUCCESS;
        }
        final var newModule = ScopeModules.REGISTRY.get(ForgeRegistries.ITEMS.getKey(held.getItem()));
        if (newModule == null) { return InteractionResult.PASS; }
        final var targetSlot = modules.get(indexOfTarget).firstSlot();
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
            insertModule(new ModuleInScope(targetSlot, newModule.newInstance(), false), indexOfTarget);
            held.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private ItemStack removeModule(int indexToRemove) {
        final var removed = modules.remove(indexToRemove).type();
        for (int i = 0; i < removed.getWidth(); ++i) {
            modules.add(
                    indexToRemove + i, new ModuleInScope(indexToRemove + i, ScopeModules.NONE.newInstance(), false)
            );
        }
        modules = fixModuleList(modules);
        sendGlobalStateUpdate();
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
        sendGlobalStateUpdate();
    }

    private void sendGlobalStateUpdate() {
        sendToListening(new SetGlobalState(getGlobalSyncState()));
    }

    private static SelectionShapes makeShapes(ShapeKey key, ScopeBlockEntity bEntity) {
        List<SelectionShapes> subShapes = new ArrayList<>();
        int offset = 0;
        for (int i = 0; i < key.modules().size(); ++i) {
            final var nextModule = key.modules().get(i);
            final var nextOffset = offset + nextModule.getWidth() * 3;
            final var shape = ShapeUtils.createPixelRelative(2 + offset, 1, 7, 2 + nextOffset, 8, 15);
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

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (side == getFacing() || side == null) {
            return ForgeCapabilities.ENERGY.orEmpty(cap, energyCap);
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
    }

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
                fixed.add(i, new ModuleInScope(nextFreeSlot, ScopeModules.NONE.newInstance(), false));
                ++nextFreeSlot;
            } else {
                nextFreeSlot += next.type().getWidth();
            }
        }
        for (int i = nextFreeSlot; i < NUM_SLOTS; ++i) {
            // If modules add up to <4 slots, fill on the right with empty modules
            fixed.add(new ModuleInScope(i, ScopeModules.NONE.newInstance(), false));
        }
        return fixed;
    }

    public boolean hasPower() {
        if (globalConfig.powered()) {
            return energy.getEnergyStored() > 0;
        } else {
            return energy.getEnergyStored() > getPowerConsumption() * 10;
        }
    }

    public int getPowerConsumption() {
        // TODO module consumption
        return BASE_POWER_PER_TICK;
    }

    public GlobalState getGlobalSyncState() {
        return new GlobalState(hasPower(), getPowerConsumption());
    }

    private record ShapeKey(Direction facing, List<ScopeModule<?>> modules) { }

    public record ModuleInScope(int firstSlot, ScopeModuleInstance<?> module, boolean isLocked) {
        public static final MyCodec<ModuleInScope> CODEC = new RecordCodec3<>(
                MyCodecs.INTEGER.fieldOf("firstSlot", ModuleInScope::firstSlot),
                ScopeModuleInstance.CODEC.fieldOf("module", ModuleInScope::module),
                MyCodecs.BOOL.fieldOf("isLocked", ModuleInScope::isLocked),
                ModuleInScope::new
        );
        public static final MyCodec<ModuleInScope> SYNC_CODEC = new RecordCodec3<>(
                MyCodecs.INTEGER.fieldOf("firstSlot", ModuleInScope::firstSlot),
                MyCodecs.RESOURCE_LOCATION.<ScopeModuleInstance<?>>xmap(
                        rl -> ScopeModules.REGISTRY.getOrDefault(rl, ScopeModules.NONE).newInstance(),
                        smi -> smi.getType().getRegistryName()
                ).fieldOf("moduleType", ModuleInScope::module),
                MyCodecs.BOOL.fieldOf("isLocked", ModuleInScope::isLocked),
                ModuleInScope::new
        );

        public int firstSlotAfter() {
            return firstSlot + type().getWidth();
        }

        public ScopeModule<?> type() {
            return module().getType();
        }

        public ModuleInScope toggleLock() {
            return new ModuleInScope(firstSlot, module, !isLocked);
        }
    }
}
