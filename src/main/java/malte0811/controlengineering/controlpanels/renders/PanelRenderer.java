package malte0811.controlengineering.controlpanels.renders;

import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.controlpanels.model.PanelModelCache;
import malte0811.controlengineering.tiles.panels.ControlPanelTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nonnull;

public class PanelRenderer extends BlockEntityRenderer<ControlPanelTile> {
    public static final ResourceLocation PANEL_TEXTURE_LOC = new ResourceLocation(
            ControlEngineering.MODID,
            "block/control_panel"
    );
    //TODO reset
    public static final ResettableLazy<TextureAtlasSprite> PANEL_TEXTURE = new ResettableLazy<>(
            () -> Minecraft.getInstance()
                    .getModelManager()
                    .getAtlas(InventoryMenu.BLOCK_ATLAS)
                    .getSprite(PANEL_TEXTURE_LOC)
    );
    private final PanelModelCache CACHED_MODELS = new PanelModelCache(MixedModel.SOLID_STATIC);

    public PanelRenderer(BlockEntityRenderDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(
            ControlPanelTile tile,
            float partialTicks,
            @Nonnull PoseStack transform,
            @Nonnull MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay
    ) {
        BlockState state = tile.getBlockState();
        if (state.getValue(PanelBlock.IS_BASE)) {
            return;
        }
        tile = PanelBlock.getBase(tile.getLevel(), state, tile.getBlockPos());
        if (tile == null) {
            return;
        }
        //CACHED_MODELS
        new PanelModelCache(MixedModel.SOLID_STATIC)
                .getMixedModel(tile.getData()).renderTo(buffer, transform, combinedLight, combinedOverlay);
    }
}
