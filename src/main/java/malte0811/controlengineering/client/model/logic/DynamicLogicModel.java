package malte0811.controlengineering.client.model.logic;

import blusunrize.immersiveengineering.api.ApiUtils;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.client.model.CEBakedModel;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.client.render.utils.BakedQuadVertexBuilder;
import malte0811.controlengineering.client.model.logic.DynamicLogicModel.ModelData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.CompositeModelState;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class DynamicLogicModel implements CEBakedModel.Cacheable<Pair<ModelData, RenderType>> {
    private static final Vec2[] TUBE_OFFSETS;
    private static final float[] BOARD_HEIGHTS = {16.5f / 16f, 21.5f / 16f, 12.5f / 16f, 26.5f / 16f,};
    public static final ModelProperty<ModelData> DATA = new ModelProperty<>();

    static {
        int[] tubeAxisOffsets = {0, 3, 7, 10};
        TUBE_OFFSETS = Arrays.stream(tubeAxisOffsets)
                .boxed()
                .flatMap(i -> Arrays.stream(tubeAxisOffsets).mapToObj(i2 -> new int[]{i, i2}))
                .map(a -> new Vec2(a[0] / 16f, a[1] / 16f))
                .toArray(Vec2[]::new);
        Collections.shuffle(Arrays.asList(TUBE_OFFSETS), ApiUtils.RANDOM);
    }

    private final UnbakedModel board;
    private final UnbakedModel tube;
    private final ModelBakery bakery;
    private final Function<Material, TextureAtlasSprite> spriteGetter;
    private final ModelState modelTransform;
    private final TextureAtlasSprite particles;
    private final BakedQuad clockQuad;

    private final List<FixedTubeModel> knownModels = new ArrayList<>();

    public DynamicLogicModel(
            UnbakedModel board,
            UnbakedModel tube,
            ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelTransform
    ) {

        this.board = board;
        this.tube = tube;
        this.bakery = bakery;
        this.spriteGetter = spriteGetter;
        this.modelTransform = modelTransform;
        particles = board.bake(
                bakery, spriteGetter, modelTransform, new ResourceLocation(ControlEngineering.MODID, "temp")
        ).getQuads(null, null, ApiUtils.RANDOM_SOURCE, EmptyModelData.INSTANCE).get(0).getSprite();

        PoseStack transform = new PoseStack();
        modelTransform.getRotation().blockCenterToCorner().push(transform);
        List<BakedQuad> quads = new ArrayList<>();
        new QuadBuilder(
                new Vec3(1, 0.375, 0.625),
                new Vec3(1, 0.375, 0.375),
                new Vec3(1, 0.625, 0.375),
                new Vec3(1, 0.625, 0.625)
        ).setSprite(particles)
                .setUCoords(15 / 16f, 15 / 16f, 1, 1)
                .setVCoords(0, 1 / 16f, 1 / 16f, 0)
                .writeTo(new BakedQuadVertexBuilder(particles, transform, quads).dontInterpolateUV());
        this.clockQuad = quads.get(0);
    }

    @Override
    public List<BakedQuad> getQuads(Pair<ModelData, RenderType> dataPair) {
        var data = dataPair.getFirst();
        while (this.knownModels.size() <= data.numTubes) {
            this.knownModels.add(null);
        }
        List<BakedQuad> quads;
        if (data.numTubes >= 0) {
            FixedTubeModel result = this.knownModels.get(data.numTubes);
            if (result == null) {
                result = new FixedTubeModel(data.numTubes);
                this.knownModels.set(data.numTubes, result);
            }
            quads = result.getQuads(dataPair.getSecond());
        } else {
            quads = new ArrayList<>();
        }
        if (data.hasClock) {
            quads = new ArrayList<>(quads);
            quads.add(clockQuad);
        }
        return quads;
    }

    @Nullable
    @Override
    public Pair<ModelData, RenderType> getKey(
            @Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull IModelData extraData
    ) {
        return Pair.of(
                Objects.requireNonNullElseGet(extraData.getData(DATA), () -> new ModelData(-1, false)),
                MinecraftForgeClient.getRenderType()
        );
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleIcon(@Nonnull IModelData data) {
        return particles;
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
                for (Vec2 xz : TUBE_OFFSETS) {
                    if (numAdded >= numTubes) {
                        break;
                    }
                    translucent.addAll(translated(tube, new Vector3f(-xz.x, y, -xz.y)));
                    ++numAdded;
                }
                if (numAdded >= numTubes) {
                    break;
                }
            }
            this.solid = ImmutableList.copyOf(solid);
            this.translucent = ImmutableList.copyOf(translucent);
        }

        private List<BakedQuad> translated(UnbakedModel model, Vector3f offset) {
            ModelState offsetTransform = new SimpleModelState(new Transformation(
                    offset, null, null, null
            ));
            ResourceLocation dummy = new ResourceLocation(ControlEngineering.MODID, "dynamic");
            BakedModel baked = model.bake(
                    bakery, spriteGetter, new CompositeModelState(modelTransform, offsetTransform), dummy
            );
            if (baked == null) {
                return ImmutableList.of();
            } else {
                return baked.getQuads(null, null, ApiUtils.RANDOM_SOURCE, EmptyModelData.INSTANCE);
            }
        }

        public List<BakedQuad> getQuads(RenderType currentType) {
            if (currentType == RenderType.solid()) {
                return solid;
            } else if (currentType == RenderType.translucent()) {
                return translucent;
            } else {
                return ImmutableList.of();
            }
        }
    }

    public record ModelData(int numTubes, boolean hasClock) {}
}
