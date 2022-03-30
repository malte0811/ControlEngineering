package malte0811.controlengineering.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public interface CEBakedModel extends BakedModel {
    @Nonnull
    @Override
    default List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand
    ) {
        return getQuads(state, side, rand, EmptyModelData.INSTANCE);
    }

    @Nonnull
    @Override
    List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData
    );

    @Override
    TextureAtlasSprite getParticleIcon(@Nonnull IModelData data);

    @Nonnull
    @Override
    default TextureAtlasSprite getParticleIcon() {
        return getParticleIcon(EmptyModelData.INSTANCE);
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
}
