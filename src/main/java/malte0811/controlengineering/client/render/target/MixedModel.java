package malte0811.controlengineering.client.render.target;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import malte0811.controlengineering.client.render.utils.BakedQuadVertexBuilder;
import malte0811.controlengineering.client.render.utils.TransformingVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class MixedModel implements MultiBufferSource {
    public static final RenderType SOLID_STATIC = RenderType.solid();
    public static final RenderType SOLID_DYNAMIC = createCopy("solid_ter", false, SOLID_STATIC);

    private final Set<RenderType> staticTypes;

    private final List<BakedQuad> staticQuads = new ArrayList<>();
    private final Map<RenderType, DynamicVertexBuilder> dynamicQuads = new Object2ObjectArrayMap<>();
    @Nullable
    private BakedQuadVertexBuilder.WrappedConsumer activeStaticBuilder;
    private TextureAtlasSprite staticSprite = Minecraft.getInstance().getModelManager()
            .getAtlas(InventoryMenu.BLOCK_ATLAS)
            .getSprite(MissingTextureAtlasSprite.getLocation());

    public MixedModel(RenderType... staticTypes) {
        this.staticTypes = new ObjectArraySet<>(staticTypes);
    }

    @Nonnull
    @Override
    public VertexConsumer getBuffer(@Nonnull RenderType type) {
        // TODO both of these need RAII!
        if (staticTypes.contains(type)) {
            if (activeStaticBuilder == null) {
                this.activeStaticBuilder = BakedQuadVertexBuilder.makeInterpolating(staticSprite, new PoseStack(), staticQuads);
            }
            return this.activeStaticBuilder.consumer();
        } else {
            return dynamicQuads.computeIfAbsent(type, $ -> new DynamicVertexBuilder());
        }
    }

    public void setSpriteForStaticTargets(TextureAtlasSprite sprite) {
        this.staticSprite = sprite;
    }

    public List<BakedQuad> getStaticQuads() {
        if (activeStaticBuilder != null) {
            activeStaticBuilder.close();
            activeStaticBuilder = null;
        }
        return staticQuads;
    }

    public void renderTo(MultiBufferSource out, PoseStack transform, int combinedLight, int combinedOverlay) {
        for (var vertices : dynamicQuads.entrySet()) {
            VertexConsumer buffer = new TransformingVertexBuilder(out.getBuffer(vertices.getKey()), transform);
            for (DynamicVertex v : vertices.getValue().getFinishedVertices()) {
                v.accept(buffer, combinedLight, combinedOverlay);
            }
        }
    }

    private static RenderType createCopy(String name, boolean needsSorting, RenderType original) {
        return new RenderType(
                name,
                original.format(),
                original.mode(),
                original.bufferSize(),
                original.affectsCrumbling(),
                needsSorting,
                original::setupRenderState,
                original::clearRenderState
        ) {
        };
    }
}
