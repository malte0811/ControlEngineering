package malte0811.controlengineering.controlpanels.renders;

import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.controlpanels.model.PanelModelCache;
import malte0811.controlengineering.tiles.panels.ControlPanelTile;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class PanelRenderer extends TileEntityRenderer<ControlPanelTile> {
    public static final ResourceLocation PANEL_TEXTURE_LOC = new ResourceLocation(
            ControlEngineering.MODID,
            "block/control_panel"
    );
    //TODO reset
    public static final ResettableLazy<TextureAtlasSprite> PANEL_TEXTURE = new ResettableLazy<>(
            () -> Minecraft.getInstance()
                    .getModelManager()
                    .getAtlasTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE)
                    .getSprite(PANEL_TEXTURE_LOC)
    );
    private final PanelModelCache CACHED_MODELS = new PanelModelCache(MixedModel.SOLID_STATIC);

    public PanelRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(
            ControlPanelTile tile,
            float partialTicks,
            @Nonnull MatrixStack transform,
            @Nonnull IRenderTypeBuffer buffer,
            int combinedLight,
            int combinedOverlay
    ) {
        BlockState state = tile.getBlockState();
        if (state.get(PanelBlock.IS_BASE)) {
            return;
        }
        tile = PanelBlock.getBase(tile.getWorld(), state, tile.getPos());
        if (tile == null) {
            return;
        }
        //CACHED_MODELS
        new PanelModelCache(MixedModel.SOLID_STATIC)
                .getMixedModel(tile.getData()).renderTo(buffer, transform, combinedLight, combinedOverlay);
    }
}
