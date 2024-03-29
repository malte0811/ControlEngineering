package malte0811.controlengineering.client.model.tape;

import blusunrize.immersiveengineering.api.utils.client.ModelDataUtils;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.blockentity.tape.KeypunchBlockEntity;
import malte0811.controlengineering.client.model.CEBakedModel;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.client.render.utils.BakedQuadVertexBuilder;
import malte0811.controlengineering.util.RLUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class KeypunchSwitchModel implements CEBakedModel {
    public static final ResourceLocation TEXTURE_LOC = RLUtils.ceLoc("block/keypunch");
    private static final ModelProperty<Boolean> LOOPBACK = new ModelProperty<>();

    private final Supplier<TextureAtlasSprite> texture;
    private final Supplier<BakedQuad> loopbackQuad;
    private final Supplier<BakedQuad> remoteQuad;

    public KeypunchSwitchModel(ItemTransforms transforms, ModelState modelTransform) {
        texture = Suppliers.memoize(
                () -> Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE_LOC)
        );
        PoseStack transform = new PoseStack();
        modelTransform.getRotation().blockCenterToCorner().push(transform);
        this.loopbackQuad = Suppliers.memoize(() -> makeQuad(6.5, transform));
        this.remoteQuad = Suppliers.memoize(() -> makeQuad(8.5, transform));
    }

    private BakedQuad makeQuad(double xMin, PoseStack transform) {
        List<BakedQuad> quads = new ArrayList<>();
        final double epsilon = 5e-4;
        new QuadBuilder(
                new Vec3(xMin / 16, 13 / 16., 1 + epsilon),
                new Vec3((xMin + 1) / 16, 13 / 16., 1 + epsilon),
                new Vec3((xMin + 1) / 16, 16 / 16., 1 + epsilon),
                new Vec3(xMin / 16, 16 / 16., 1 + epsilon)
        ).setSprite(texture.get())
                .setUCoords(44 / 64f, 44 / 64f, 47 / 64f, 47 / 64f)
                .setVCoords(16 / 32f, 15 / 32f, 15 / 32f, 16 / 32f)
                .writeTo(BakedQuadVertexBuilder.makeNonInterpolating(texture.get(), transform, quads));
        return quads.get(0);
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleIcon(@Nonnull ModelData data) {
        return texture.get();
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData extraData,
            @Nullable RenderType layer
    ) {
        var loopbackNullable = extraData.get(LOOPBACK);
        if (loopbackNullable != null && loopbackNullable) {
            return List.of(loopbackQuad.get());
        } else {
            return List.of(remoteQuad.get());
        }
    }

    @Nonnull
    @Override
    public ModelData getModelData(
            @Nonnull BlockAndTintGetter world,
            @Nonnull BlockPos pos,
            @Nonnull BlockState state,
            @Nonnull ModelData tileData
    ) {
        if (world.getBlockEntity(pos) instanceof KeypunchBlockEntity keypunch) {
            return ModelDataUtils.single(LOOPBACK, keypunch.isLoopback());
        }
        return tileData;
    }
}
