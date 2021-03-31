package malte0811.controlengineering.logic.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.client.render.target.StaticRenderTarget;
import malte0811.controlengineering.client.render.target.TargetType;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ModelTransformComposition;
import net.minecraftforge.client.model.SimpleModelTransform;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class DynamicLogicModel implements IBakedModel {
    private static final Random RANDOM = new Random(1234);
    private static final Vector2f[] TUBE_OFFSETS;
    private static final float[] BOARD_HEIGHTS = {.5f / 16f, 5.5f / 16f, -4.5f / 16f, 10.5f / 16f,};
    public static final ModelProperty<ModelData> DATA = new ModelProperty<>();

    static {
        int[] tubeAxisOffsets = {0, 3, 7, 10};
        TUBE_OFFSETS = Arrays.stream(tubeAxisOffsets)
                .boxed()
                .flatMap(i -> Arrays.stream(tubeAxisOffsets).mapToObj(i2 -> new int[]{i, i2}))
                .map(a -> new Vector2f(a[0] / 16f, a[1] / 16f))
                .toArray(Vector2f[]::new);
        Collections.shuffle(Arrays.asList(TUBE_OFFSETS), RANDOM);
    }

    private final IUnbakedModel board;
    private final IUnbakedModel tube;
    private final ModelBakery bakery;
    private final Function<RenderMaterial, TextureAtlasSprite> spriteGetter;
    private final IModelTransform modelTransform;
    private final TextureAtlasSprite particles;
    private final BakedQuad clockQuad;

    private final List<FixedTubeModel> knownModels = new ArrayList<>();

    public DynamicLogicModel(
            IUnbakedModel board,
            IUnbakedModel tube,
            ModelBakery bakery,
            Function<RenderMaterial, TextureAtlasSprite> spriteGetter,
            IModelTransform modelTransform
    ) {

        this.board = board;
        this.tube = tube;
        this.bakery = bakery;
        this.spriteGetter = spriteGetter;
        this.modelTransform = modelTransform;
        particles = board.bakeModel(
                bakery, spriteGetter, modelTransform, new ResourceLocation(ControlEngineering.MODID, "temp")
        ).getQuads(null, null, RANDOM, EmptyModelData.INSTANCE).get(0).getSprite();

        StaticRenderTarget target = new StaticRenderTarget($ -> true);
        MatrixStack transform = new MatrixStack();
        modelTransform.getRotation().blockCenterToCorner().push(transform);
        new QuadBuilder(
                new Vector3d(1, 0.375 - 1, 0.625),
                new Vector3d(1, 0.375 - 1, 0.375),
                new Vector3d(1, 0.625 - 1, 0.375),
                new Vector3d(1, 0.625 - 1, 0.625)
        ).setSprite(particles)
                .setUCoords(15 / 16f, 15 / 16f, 1, 1)
                .setVCoords(0, 1 / 16f, 1 / 16f, 0)
                .setNormal(new Vector3d(1, 0, 0))
                .writeTo(transform, target, TargetType.STATIC);
        this.clockQuad = target.getQuads().get(0);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand
    ) {
        return getQuads(state, side, rand, EmptyModelData.INSTANCE);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData
    ) {
        ModelData data = extraData.getData(DATA);
        if (data == null) {
            data = new ModelData(0, false);
        }
        while (this.knownModels.size() <= data.numTubes) {
            this.knownModels.add(null);
        }
        FixedTubeModel result = this.knownModels.get(data.numTubes);
        if (result == null) {
            result = new FixedTubeModel(data.numTubes);
            this.knownModels.set(data.numTubes, result);
        }
        List<BakedQuad> quads = result.getQuads();
        if (data.hasClock) {
            quads = new ArrayList<>(quads);
            quads.add(clockQuad);
        }
        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return particles;
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    private class FixedTubeModel {
        private final List<BakedQuad> solid;
        private final List<BakedQuad> translucent;

        public FixedTubeModel(int numTubes) {
            List<BakedQuad> solid = new ArrayList<>();
            List<BakedQuad> translucent = new ArrayList<>();
            int numAdded = 0;
            for (float y : BOARD_HEIGHTS) {
                solid.addAll(translated(board, new Vector3f(0, y, 0)));
                for (Vector2f xz : TUBE_OFFSETS) {
                    translucent.addAll(translated(tube, new Vector3f(-xz.x, y, -xz.y)));
                    ++numAdded;
                    if (numAdded >= numTubes) {
                        break;
                    }
                }
                if (numAdded >= numTubes) {
                    break;
                }
            }
            this.solid = ImmutableList.copyOf(solid);
            this.translucent = ImmutableList.copyOf(translucent);
        }

        private List<BakedQuad> translated(IUnbakedModel model, Vector3f offset) {
            IModelTransform offsetTransform = new SimpleModelTransform(new TransformationMatrix(
                    offset,
                    null,
                    null,
                    null
            ));
            ResourceLocation dummy = new ResourceLocation(ControlEngineering.MODID, "dynamic");
            IBakedModel baked = model.bakeModel(
                    bakery, spriteGetter, new ModelTransformComposition(modelTransform, offsetTransform), dummy
            );
            if (baked == null) {
                return ImmutableList.of();
            } else {
                return baked.getQuads(null, null, RANDOM, EmptyModelData.INSTANCE);
            }
        }

        public List<BakedQuad> getQuads() {
            RenderType currentType = MinecraftForgeClient.getRenderLayer();
            if (currentType == RenderType.getSolid()) {
                return solid;
            } else if (currentType == RenderType.getTranslucent()) {
                return translucent;
            } else {
                return ImmutableList.of();
            }
        }
    }

    public static class ModelData {
        private final int numTubes;
        private final boolean hasClock;

        public ModelData(int numTubes, boolean hasClock) {
            this.numTubes = numTubes;
            this.hasClock = hasClock;
        }
    }
}
