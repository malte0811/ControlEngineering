package malte0811.controlengineering.controlpanels.model;

import blusunrize.immersiveengineering.api.utils.client.CombinedModelData;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.controlpanels.PanelData;
import malte0811.controlengineering.controlpanels.renders.PanelRenderer;
import malte0811.controlengineering.tiles.panels.ControlPanelTile;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class PanelModel implements IBakedModel {
    private static final ModelProperty<PanelData> COMPONENTS = new ModelProperty<>($ -> true);

    private final PanelModelCache CACHED_MODELS = new PanelModelCache(MixedModel.SOLID_STATIC);
    private final ItemCameraTransforms transforms;

    public PanelModel(ItemCameraTransforms transforms) {
        this.transforms = transforms;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand) {
        return getQuads(state, side, rand, EmptyModelData.INSTANCE);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData
    ) {
        return CACHED_MODELS.getStaticModel(extraData.getData(COMPONENTS)).getQuads(state, side, rand, extraData);
    }

    @Nonnull
    @Override
    public IModelData getModelData(
            @Nonnull IBlockDisplayReader world,
            @Nonnull BlockPos pos,
            @Nonnull BlockState state,
            @Nonnull IModelData tileData
    ) {
        final ControlPanelTile tile = PanelBlock.getBase(world, state, pos);
        if (tile == null) {
            return tileData;
        }
        return CombinedModelData.combine(tileData, new SinglePropertyModelData<>(tile.getData(), COMPONENTS));
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isSideLit() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return PanelRenderer.PANEL_TEXTURE.get();
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public ItemCameraTransforms getItemCameraTransforms() {
        return transforms;
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }
}
