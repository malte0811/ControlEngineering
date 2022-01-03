package malte0811.controlengineering.client.render.panel;

import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.panels.ControlPanelBlockEntity;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.client.model.panel.PanelModelCache;
import malte0811.controlengineering.client.render.target.MixedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class PanelRenderer implements BlockEntityRenderer<ControlPanelBlockEntity> {
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

    public PanelRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(
            ControlPanelBlockEntity panelBE,
            float partialTicks,
            @Nonnull PoseStack transform,
            @Nonnull MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay
    ) {
        BlockState state = panelBE.getBlockState();
        panelBE = PanelBlock.getBase(panelBE.getLevel(), state, panelBE.getBlockPos());
        if (panelBE == null) {
            return;
        }
        CACHED_MODELS.getMixedModel(panelBE.getData()).renderTo(buffer, transform, combinedLight, combinedOverlay);
    }
}
