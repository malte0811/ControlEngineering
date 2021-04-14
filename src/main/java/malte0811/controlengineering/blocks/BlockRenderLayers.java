package malte0811.controlengineering.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;

public class BlockRenderLayers {
    public static void init() {
        setLayers(CEBlocks.BUS_RELAY.get(), RenderType.getSolid());
        setLayers(CEBlocks.TELETYPE.get(), RenderType.getCutout());
        setLayers(CEBlocks.LOGIC_CABINET.get(), RenderType.getSolid(), RenderType.getTranslucent());
    }

    private static void setLayers(Block b, RenderType... types) {
        if (types.length == 1) {
            RenderTypeLookup.setRenderLayer(b, types[0]);
        } else {
            RenderTypeLookup.setRenderLayer(b, rt -> {
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
