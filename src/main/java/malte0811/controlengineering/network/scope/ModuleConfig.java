package malte0811.controlengineering.network.scope;

import malte0811.controlengineering.scope.ScopeModuleInstance;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;

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
    public boolean process(List<ScopeModuleInstance<?>> modules) {
        final var toModify = modules.get(index);
        if (!processWithGenerics(instanceWithNewConfig, toModify)) {
            return false;
        }
        if (toModify.triggerActive()) {
            for (int i = 0; i < modules.size(); ++i) {
                final var moduleAt = modules.get(i);
                if (i != index && moduleAt.triggerActive()) {
                    moduleAt.disableTrigger();
                }
            }
        }
        return true;
    }

    private <C1, C2> boolean processWithGenerics(ScopeModuleInstance<C1> newCfg, ScopeModuleInstance<C2> replaceIn) {
        if (replaceIn.getType() != instanceWithNewConfig.getType()) { return false; }
        replaceIn.setConfig((C2) newCfg.getCurrentState());
        return true;
    }
}
