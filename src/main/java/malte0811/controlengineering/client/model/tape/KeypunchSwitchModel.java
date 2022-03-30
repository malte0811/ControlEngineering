package malte0811.controlengineering.client.model.tape;

import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.tape.KeypunchBlockEntity;
import malte0811.controlengineering.client.model.CEBakedModel;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.client.render.utils.BakedQuadVertexBuilder;
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

public class KeypunchSwitchModel implements CEBakedModel {
    public static final ResourceLocation TEXTURE_LOC = new ResourceLocation(ControlEngineering.MODID, "block/keypunch");
    private static final ModelProperty<Boolean> LOOPBACK = new ModelProperty<>();

    private final Supplier<TextureAtlasSprite> texture;
    private final Supplier<BakedQuad> loopbackQuad;
    private final Supplier<BakedQuad> remoteQuad;

    public KeypunchSwitchModel(ItemTransforms transforms, ModelState modelTransform) {
        texture = Suppliers.memoize(() -> ForgeModelBakery.defaultTextureGetter().apply(
                new Material(InventoryMenu.BLOCK_ATLAS, TEXTURE_LOC)
        ));
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
                .writeTo(new BakedQuadVertexBuilder(texture.get(), transform, quads).dontInterpolateUV());
        return quads.get(0);
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleIcon(@Nonnull IModelData data) {
        return texture.get();
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData
    ) {
        var loopbackNullable = extraData.getData(LOOPBACK);
        if (loopbackNullable != null && loopbackNullable) {
            return List.of(loopbackQuad.get());
        } else {
            return List.of(remoteQuad.get());
        }
    }

    @Nonnull
    @Override
    public IModelData getModelData(
            @Nonnull BlockAndTintGetter world,
            @Nonnull BlockPos pos,
            @Nonnull BlockState state,
            @Nonnull IModelData tileData
    ) {
        if (world.getBlockEntity(pos) instanceof KeypunchBlockEntity keypunch) {
            return new SinglePropertyModelData<>(keypunch.isLoopback(), LOOPBACK);
        }
        return tileData;
    }
}
