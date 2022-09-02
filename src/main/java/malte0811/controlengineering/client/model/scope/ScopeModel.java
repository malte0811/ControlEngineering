package malte0811.controlengineering.client.model.scope;

import blusunrize.immersiveengineering.api.ApiUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity;
import malte0811.controlengineering.client.model.CEBakedModel;
import malte0811.controlengineering.controlpanels.scope.ScopeModule;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.QuadTransformers;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ScopeModel implements CEBakedModel {
    private static final ModelProperty<Key> KEY_PROP = new ModelProperty<>();
    private final ChunkRenderTypeSet SOLID_CHUNK = ChunkRenderTypeSet.of(RenderType.solid());
    private final List<RenderType> SOLID_ITEM = List.of(RenderType.solid());

    private final List<BakedQuad> mainModel;
    private final TextureAtlasSprite particleIcon;
    private final Transformation modelTransform;
    private final ItemTransforms transforms;
    private final Map<ResourceLocation, List<BakedQuad>> modules;
    private final LoadingCache<Key, List<BakedQuad>> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(60, TimeUnit.SECONDS)
            .build(CacheLoader.from(this::computeQuads));

    public ScopeModel(
            BakedModel mainModel,
            Map<ResourceLocation, BakedModel> modules,
            Transformation modelTransform,
            ItemTransforms transforms
    ) {
        this.mainModel = getDefaultQuads(mainModel);
        this.particleIcon = mainModel.getParticleIcon(ModelData.EMPTY);
        this.modelTransform = modelTransform;
        this.transforms = transforms;
        this.modules = new HashMap<>();
        for (final var moduleModel : modules.entrySet()) {
            this.modules.put(moduleModel.getKey(), getDefaultQuads(moduleModel.getValue()));
        }
    }

    private static List<BakedQuad> getDefaultQuads(BakedModel model) {
        return model.getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, RenderType.solid());
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand,
            @Nonnull ModelData extraData, @Nullable RenderType layer
    ) {
        if (side != null) {
            // TODO this model actually has properly cullable quads. Cull those?
            return List.of();
        }
        var key = extraData.get(KEY_PROP);
        if (key == null) { key = new Key(List.of()); }
        return cache.getUnchecked(key);
    }

    @Override
    @NotNull
    public ModelData getModelData(
            @NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state,
            @NotNull ModelData modelData
    ) {
        if (level.getBlockEntity(pos) instanceof ScopeBlockEntity scope) {
            return modelData.derive().with(KEY_PROP, new Key(scope.getModules().toList())).build();
        } else {
            return modelData;
        }
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@Nonnull ModelData data) {
        return particleIcon;
    }

    @Nonnull
    @Override
    public ChunkRenderTypeSet getRenderTypes(
            @NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data
    ) {
        return SOLID_CHUNK;
    }

    @Nonnull
    @Override
    public List<RenderType> getRenderTypes(@Nonnull ItemStack itemStack, boolean fabulous) {
        return SOLID_ITEM;
    }

    @Nonnull
    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }

    private List<BakedQuad> computeQuads(Key key) {
        List<BakedQuad> quads = new ArrayList<>(mainModel);
        int offsetSlots = 0;
        for (final var module : key.orderedModules()) {
            final var translation = new Vector3f(offsetSlots * (-3 / 16f), 0, 0);
            translation.transform(modelTransform.getNormalMatrix());
            final var transformer = QuadTransformers.applying(
                    new Transformation(translation, null, null, null)
            );
            offsetSlots += module.getWidth();
            final var moduleQuads = modules.get(module.getRegistryName());
            if (moduleQuads == null) { continue; }
            quads.addAll(transformer.process(moduleQuads));
            // TODO add side quad at start/end of filled/empty sequences
        }
        return quads;
    }

    public record Key(List<ScopeModule<?>> orderedModules) {}
}
