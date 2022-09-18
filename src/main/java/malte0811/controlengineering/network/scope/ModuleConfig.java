package malte0811.controlengineering.network.scope;

import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity;
import malte0811.controlengineering.scope.module.ScopeModuleInstance;
import malte0811.controlengineering.scope.trace.Trace;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;

import javax.annotation.Nullable;
import java.util.List;

public record ModuleConfig(
        int index, ScopeModuleInstance<?> instanceWithNewConfig
) implements ScopeSubPacket.IScopeSubPacket {
    public static final MyCodec<ModuleConfig> CODEC = new RecordCodec2<>(
            MyCodecs.INTEGER.fieldOf("index", ModuleConfig::index),
            ScopeModuleInstance.CODEC.fieldOf("withNewCfg", ModuleConfig::instanceWithNewConfig),
            ModuleConfig::new
    );

    @Override
    public boolean process(List<ScopeBlockEntity.ModuleInScope> modules, @Nullable List<Trace> traces) {
        if (processWithGenerics(instanceWithNewConfig, modules.get(index).module())) {
            ScopeModuleInstance.ensureOneTriggerActive(modules, index);
            return true;
        }
        return false;
    }

    private <C1, C2> boolean processWithGenerics(ScopeModuleInstance<C1> newCfg, ScopeModuleInstance<C2> replaceIn) {
        if (replaceIn.getType() != instanceWithNewConfig.getType()) { return false; }
        replaceIn.setConfig((C2) newCfg.getCurrentState());
        return true;
    }
}
