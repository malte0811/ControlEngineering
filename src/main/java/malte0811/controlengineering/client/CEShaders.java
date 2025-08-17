package malte0811.controlengineering.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import malte0811.controlengineering.util.RLUtils;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

import static malte0811.controlengineering.ControlEngineering.MODID;

@EventBusSubscriber(value = Dist.CLIENT, modid = MODID)
public class CEShaders {
    private static ShaderInstance scopeTrace;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent ev) throws IOException {
        ev.registerShader(new ShaderInstance(
                ev.getResourceProvider(), RLUtils.ceLoc("scope_trace"), DefaultVertexFormat.BLOCK
        ), shader -> scopeTrace = shader);
    }

    public static ShaderInstance getScopeTrace() {
        return scopeTrace;
    }
}
