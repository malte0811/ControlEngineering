package malte0811.controlengineering.client.model.tape;

import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.tape.SequencerBlockEntity;
import malte0811.controlengineering.client.model.CEBakedModel;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.client.render.utils.BakedQuadVertexBuilder;
import malte0811.controlengineering.util.Bool2ObjectMap;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

//TODO deduplicate with KeypunchSwitchModel and DynamicLogicModel
public class SequencerSwitchModel extends CEBakedModel {
    public static final ResourceLocation TEXTURE_LOC = new ResourceLocation(
            ControlEngineering.MODID,
            "block/sequencer"
    );
    private static final ModelProperty<Data> DATA = new ModelProperty<>();

    private final Supplier<TextureAtlasSprite> texture;
    private final Supplier<Bool2ObjectMap<BakedQuad>> compactSwitch;
    private final Supplier<Bool2ObjectMap<BakedQuad>> autoresetSwitch;
    private final Supplier<BakedQuad> clockQuad;

    public SequencerSwitchModel(ItemTransforms transforms, ModelState modelTransform) {
        texture = Suppliers.memoize(() -> ForgeModelBakery.defaultTextureGetter().apply(
                new Material(InventoryMenu.BLOCK_ATLAS, TEXTURE_LOC)
        ));
        PoseStack transform = new PoseStack();
        modelTransform.getRotation().blockCenterToCorner().push(transform);
        this.compactSwitch = makeSwitchQuads(4.5, transform);
        this.autoresetSwitch = makeSwitchQuads(10.5, transform);
        this.clockQuad = Suppliers.memoize(() -> {
            List<BakedQuad> quads = new ArrayList<>();
            new QuadBuilder(
                    new Vec3(1, 10 / 16., 6 / 16.),
                    new Vec3(1, 10 / 16., 10 / 16.),
                    new Vec3(1, 6 / 16., 10 / 16.),
                    new Vec3(1, 6 / 16., 6 / 16.)
            ).setSprite(texture.get())
                    .setUCoords(24 / 64f, 28 / 64f, 28 / 64f, 24 / 64f)
                    .setVCoords(2 / 32f, 2 / 32f, 6 / 32f, 6 / 32f)
                    .writeTo(new BakedQuadVertexBuilder(texture.get(), transform, quads).dontInterpolateUV());
            return quads.get(0);
        });
    }

    private Supplier<Bool2ObjectMap<BakedQuad>> makeSwitchQuads(double xMin, PoseStack transform) {
        return Suppliers.memoize(() -> {
            Bool2ObjectMap<BakedQuad> result = new Bool2ObjectMap<>();
            result.put(false, makeSwitchQuad(xMin, 16, transform));
            result.put(true, makeSwitchQuad(xMin, 17, transform));
            return result;
        });
    }

    private BakedQuad makeSwitchQuad(double xMin, float uMin, PoseStack transform) {
        List<BakedQuad> quads = new ArrayList<>();
        final double epsilon = 5e-4;
        new QuadBuilder(
                new Vec3(xMin / 16, 3.5 / 16., 1 + epsilon),
                new Vec3((xMin + 1) / 16, 3.5 / 16., 1 + epsilon),
                new Vec3((xMin + 1) / 16, 5.5 / 16., 1 + epsilon),
                new Vec3(xMin / 16, 5.5 / 16., 1 + epsilon)
        ).setSprite(texture.get())
                .setUCoords(uMin / 64f, (uMin + 1) / 64f, (uMin + 1) / 64f, uMin / 64f)
                .setVCoords(2 / 32f, 2 / 32f, 4 / 32f, 4 / 32f)
                .writeTo(new BakedQuadVertexBuilder(texture.get(), transform, quads).dontInterpolateUV());
        return quads.get(0);
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return texture.get();
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData
    ) {
        var data = extraData.getData(DATA);
        if (data == null) {
            return List.of();
        }
        List<BakedQuad> result = new ArrayList<>(3);
        result.add(compactSwitch.get().get(data.compact));
        result.add(autoresetSwitch.get().get(data.autoReset));
        if (data.hasClock) {
            result.add(clockQuad.get());
        }
        return result;
    }

    @Nonnull
    @Override
    public IModelData getModelData(
            @Nonnull BlockAndTintGetter world,
            @Nonnull BlockPos pos,
            @Nonnull BlockState state,
            @Nonnull IModelData tileData
    ) {
        if (world.getBlockEntity(pos) instanceof SequencerBlockEntity sequencer)
            return new SinglePropertyModelData<>(
                    new Data(sequencer.isCompact(), sequencer.isAutoreset(), sequencer.hasClock()), DATA
            );
        return super.getModelData(world, pos, state, tileData);
    }

    private record Data(boolean compact, boolean autoReset, boolean hasClock) {}
}
