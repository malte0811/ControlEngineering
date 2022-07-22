package malte0811.controlengineering.client.model.panel;

import malte0811.controlengineering.blockentity.panels.ControlPanelBlockEntity;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.client.render.panel.PanelRenderer;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.controlpanels.PanelData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class PanelModel implements BakedModel {
    private static final ModelProperty<PanelData> COMPONENTS = new ModelProperty<>($ -> true);

    private final PanelModelCache CACHED_MODELS = new PanelModelCache(MixedModel.SOLID_STATIC);
    private final ItemTransforms transforms;

    public PanelModel(ItemTransforms transforms, ModelState transform) {
        this.transforms = transforms;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            @Nonnull RandomSource rand,
            @Nonnull ModelData extraData,
            @Nullable RenderType layer
    ) {
        return CACHED_MODELS.getStaticModel(extraData.get(COMPONENTS)).getQuads(state, side, rand, extraData, layer);
    }

    @Nonnull
    @Override
    public ModelData getModelData(
            @Nonnull BlockAndTintGetter world,
            @Nonnull BlockPos pos,
            @Nonnull BlockState state,
            @Nonnull ModelData beData
    ) {
        final ControlPanelBlockEntity bEntity = PanelBlock.getBase(world, state, pos);
        if (bEntity == null) {
            return beData;
        }
        return beData.derive().with(COMPONENTS, bEntity.getData()).build();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return PanelRenderer.PANEL_TEXTURE.get();
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public ItemTransforms getTransforms() {
        return transforms;
    }

    @Nonnull
    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
