package malte0811.controlengineering.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.RegistryObject;

public class BlockRenderLayers {
    public static void init() {
        setLayers(CEBlocks.BUS_RELAY, RenderType.getSolid());
        setLayers(CEBlocks.TELETYPE, RenderType.getCutout());
        setLayers(CEBlocks.PANEL_DESIGNER, RenderType.getCutout());
        setLayers(CEBlocks.LOGIC_CABINET, RenderType.getSolid(), RenderType.getTranslucent());
        setLayers(CEBlocks.LINE_ACCESS, RenderType.getSolid(), RenderType.getCutout());
    }

    private static void setLayers(RegistryObject<? extends Block> b, RenderType... types) {
        if (types.length == 1) {
            RenderTypeLookup.setRenderLayer(b.get(), types[0]);
        } else {
            RenderTypeLookup.setRenderLayer(b.get(), rt -> {
                for (RenderType allowed : types) {
                    if (rt == allowed) {
                        return true;
                    }
                }
                return false;
            });
        }
    }
}
