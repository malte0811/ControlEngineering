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
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class DynamicLogicModel implements CEBakedModel.Cacheable<Pair<DynamicLogicModel.LogicModelData, RenderType>> {
    private static final Vec2[] TUBE_OFFSETS;
    private static final float[] BOARD_HEIGHTS = {16.5f / 16f, 21.5f / 16f, 12.5f / 16f, 26.5f / 16f,};
    public static final ModelProperty<LogicModelData> DATA = new ModelProperty<>();

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
            ResourceLocation board,
            ResourceLocation tube,
            ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelTransform
    ) {

        this.board = bakery.getModel(board);
        this.tube = bakery.getModel(tube);
        this.bakery = bakery;
        this.spriteGetter = spriteGetter;
        this.modelTransform = modelTransform;
        particles = this.board.bake(
                bakery, spriteGetter, modelTransform, ControlEngineering.ceLoc("temp")
        ).getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null).get(0).getSprite();

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
                .writeTo(BakedQuadVertexBuilder.makeNonInterpolating(particles, transform, quads));
        this.clockQuad = quads.get(0);
    }

    @Override
    public List<BakedQuad> getQuads(Pair<LogicModelData, RenderType> dataPair) {
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
    public Pair<LogicModelData, RenderType> getKey(
            @Nullable BlockState state,
            @Nullable Direction side,
            @Nonnull RandomSource rand,
            @Nonnull ModelData extraData,
            @Nullable RenderType layer
    ) {
        return Pair.of(
                Objects.requireNonNullElseGet(extraData.get(DATA), () -> new LogicModelData(-1, false)),
                layer
        );
    }

    private static final ChunkRenderTypeSet RENDER_TYPES = ChunkRenderTypeSet.of(
            RenderType.solid(), RenderType.translucent()
    );

    @Nonnull
    @Override
    public ChunkRenderTypeSet getRenderTypes(
            @NotNull BlockState state,
            @NotNull RandomSource rand,
            @NotNull ModelData data
    ) {
        return RENDER_TYPES;
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleIcon(@Nonnull ModelData data) {
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
                    for (var tubeQuad : translated(tube, new Vector3f(-xz.x, y, -xz.y))) {
                        var sumOfCorners = Vec2d.ZERO;
                        final var quadData = tubeQuad.getVertices();
                        for (int i = 0; i < 4; ++i) {
                            sumOfCorners = sumOfCorners.add(
                                    Float.intBitsToFloat(quadData[i * (quadData.length / 4) + 4]),
                                    Float.intBitsToFloat(quadData[i * (quadData.length / 4) + 5])
                            );
                        }
                        final var centerUVAbs = sumOfCorners.scale(1 / 4.);
                        final var quadSprite = tubeQuad.getSprite();
                        final var centerUV = new Vec2d(
                                Mth.inverseLerp(centerUVAbs.x(), quadSprite.getU0(), quadSprite.getU1()),
                                Mth.inverseLerp(centerUVAbs.y(), quadSprite.getV0(), quadSprite.getV1())
                        );
                        final int centerRGBA = quadSprite.getPixelRGBA(
                                0,
                                (int)(centerUV.x() * quadSprite.getWidth()),
                                (int) (centerUV.y() * quadSprite.getHeight())
                        );
                        if (centerRGBA >>> 24 == 255) {
                            solid.add(tubeQuad);
                        } else {
                            translucent.add(tubeQuad);
                        }
                    }
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
            ModelState offsetTransform = new SimpleModelState(modelTransform.getRotation().compose(new Transformation(
                    offset, null, null, null
            )));
            ResourceLocation dummy = ControlEngineering.ceLoc("dynamic");
            BakedModel baked = model.bake(bakery, spriteGetter, offsetTransform, dummy);
            if (baked == null) {
                return ImmutableList.of();
            } else {
                return baked.getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
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

    public record LogicModelData(int numTubes, boolean hasClock) {}
}
