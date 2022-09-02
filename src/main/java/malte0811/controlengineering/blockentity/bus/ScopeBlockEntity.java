package malte0811.controlengineering.blockentity.bus;

import malte0811.controlengineering.blockentity.base.CEBlockEntity;
import malte0811.controlengineering.controlpanels.scope.ScopeModule;
import malte0811.controlengineering.controlpanels.scope.ScopeModuleInstance;
import malte0811.controlengineering.controlpanels.scope.ScopeModules;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ScopeBlockEntity extends CEBlockEntity {
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

    public ScopeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        modules = MODULES_CODEC.fromNBT(tag.get("modules"), ArrayList::new);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("modules", MODULES_CODEC.toNBT(this.modules));
    }

    @Override
    protected void writeSyncedData(CompoundTag out) {
        super.writeSyncedData(out);
        out.put("modules", SYNC_MODULES_CODEC.toNBT(this.modules));
    }

    @Override
    protected void readSyncedData(CompoundTag in) {
        super.readSyncedData(in);
        modules = SYNC_MODULES_CODEC.fromNBT(in.get("modules"), ArrayList::new);
    }

    public Stream<ScopeModule<?>> getModules() {
        return modules.stream().map(ScopeModuleInstance::getType);
    }
}
