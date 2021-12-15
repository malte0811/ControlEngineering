package malte0811.controlengineering.blocks;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

public class BlockRenderLayers {
    public static void init() {
        setLayers(CEBlocks.BUS_RELAY, RenderType.solid());
        setLayers(CEBlocks.KEYPUNCH, RenderType.cutout());
        setLayers(CEBlocks.PANEL_DESIGNER, RenderType.cutout());
        setLayers(CEBlocks.LOGIC_CABINET, RenderType.solid(), RenderType.translucent());
        setLayers(CEBlocks.LINE_ACCESS, RenderType.solid(), RenderType.cutout());
    }

    private static void setLayers(RegistryObject<? extends Block> b, RenderType... types) {
        if (types.length == 1) {
            ItemBlockRenderTypes.setRenderLayer(b.get(), types[0]);
        } else {
            ItemBlockRenderTypes.setRenderLayer(b.get(), rt -> {
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
