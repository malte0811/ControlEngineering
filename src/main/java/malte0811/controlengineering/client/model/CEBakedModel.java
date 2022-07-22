package malte0811.controlengineering.client.model;

import blusunrize.immersiveengineering.api.client.ICacheKeyProvider;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface CEBakedModel extends BakedModel {
    @Nonnull
    @Override
    default List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand
    ) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Nonnull
    @Override
    List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            @Nonnull RandomSource rand,
            @Nonnull ModelData extraData,
            @Nullable RenderType layer
    );

    @Override
    TextureAtlasSprite getParticleIcon(@Nonnull ModelData data);

    @Nonnull
    @Override
    default TextureAtlasSprite getParticleIcon() {
        return getParticleIcon(ModelData.EMPTY);
    }

    @Override
    default boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    default boolean isGui3d() {
        return false;
    }

    @Override
    default boolean usesBlockLight() {
        return false;
    }

    @Override
    default boolean isCustomRenderer() {
        return false;
    }

    @Nonnull
    @Override
    default ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    interface Cacheable<Key> extends CEBakedModel, ICacheKeyProvider<Key> {
        @Override
        List<BakedQuad> getQuads(Key key);

        @Nullable
        @Override
        Key getKey(
                @Nullable BlockState state,
                @Nullable Direction side,
                @Nonnull RandomSource rand,
                @Nonnull ModelData beData,
                @Nullable RenderType layer
        );

        @Nonnull
        @Override
        default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand) {
            return CEBakedModel.super.getQuads(state, side, rand);
        }

        @Nonnull
        @Override
        default List<BakedQuad> getQuads(
                @Nullable BlockState state,
                @Nullable Direction side,
                @Nonnull RandomSource rand,
                @Nonnull ModelData extraData,
                @Nullable RenderType layer
        ) {
            return ICacheKeyProvider.super.getQuads(state, side, rand, extraData, layer);
        }

        @Override
        TextureAtlasSprite getParticleIcon(@Nonnull ModelData data);
    }
}
