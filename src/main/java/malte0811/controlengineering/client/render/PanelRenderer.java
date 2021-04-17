package malte0811.controlengineering.client.render;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.client.render.target.TargetType;
import malte0811.controlengineering.tiles.panels.ControlPanelTile;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.EmptyModelData;

import javax.annotation.Nonnull;
import java.util.List;

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
    private final PanelModelCache CACHED_MODELS = new PanelModelCache(TargetType.DYNAMIC::equals);

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
        IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());
        List<BakedQuad> quads = CACHED_MODELS.getModel(tile.getData())
                .getQuads(null, null, ApiUtils.RANDOM, EmptyModelData.INSTANCE);
        //TODO
        final float[] ones = {1, 1, 1, 1};
        final int[] lights = {combinedLight, combinedLight, combinedLight, combinedLight};
        for (BakedQuad q : quads) {
            builder.addQuad(
                    transform.getLast(), q, ones, 1, 1, 1, lights, combinedOverlay, true
            );
        }
    }
}
