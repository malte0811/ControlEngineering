package malte0811.controlengineering.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import malte0811.controlengineering.ControlEngineering;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

import static malte0811.controlengineering.ControlEngineering.MODID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CEShaders {
    private static ShaderInstance scopeTrace;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent ev) throws IOException {
        ev.registerShader(new ShaderInstance(
                ev.getResourceManager(), ControlEngineering.ceLoc("scope_trace"), DefaultVertexFormat.BLOCK
        ), shader -> scopeTrace = shader);
    }

    public static ShaderInstance getScopeTrace() {
        return scopeTrace;
    }
}
