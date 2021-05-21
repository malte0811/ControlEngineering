package malte0811.controlengineering.client.render.target;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import malte0811.controlengineering.client.render.utils.BakedQuadVertexBuilder;
import malte0811.controlengineering.client.render.utils.TransformingVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MixedModel implements IRenderTypeBuffer {
    public static final RenderType SOLID_STATIC = RenderType.getSolid();
    public static final RenderType SOLID_DYNAMIC = createCopy("solid_ter", false, SOLID_STATIC);

    private final Set<RenderType> staticTypes;

    private final List<BakedQuad> staticQuads = new ArrayList<>();
    private final Map<RenderType, List<DynamicVertex>> dynamicQuads = new Object2ObjectArrayMap<>();
    private TextureAtlasSprite staticSprite = Minecraft.getInstance().getModelManager()
            .getAtlasTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE)
            .getSprite(MissingTextureSprite.getLocation());

    public MixedModel(RenderType... staticTypes) {
        this.staticTypes = new ObjectArraySet<>(staticTypes);
    }

    @Nonnull
    @Override
    public IVertexBuilder getBuffer(@Nonnull RenderType type) {
        if (staticTypes.contains(type)) {
            return new BakedQuadVertexBuilder(staticSprite, new MatrixStack(), staticQuads);
        } else {
            return new DynamicVertexBuilder(dynamicQuads.computeIfAbsent(type, $ -> new ArrayList<>()));
        }
    }

    public void setSpriteForStaticTargets(TextureAtlasSprite sprite) {
        this.staticSprite = sprite;
    }

    public List<BakedQuad> getStaticQuads() {
        return staticQuads;
    }

    public void renderTo(IRenderTypeBuffer out, MatrixStack transform, int combinedLight, int combinedOverlay) {
        for (Map.Entry<RenderType, List<DynamicVertex>> vertices : dynamicQuads.entrySet()) {
            IVertexBuilder buffer = new TransformingVertexBuilder(out.getBuffer(vertices.getKey()), transform);
            for (DynamicVertex v : vertices.getValue()) {
                v.accept(buffer, combinedLight, combinedOverlay);
            }
        }
    }

    private static RenderType createCopy(String name, boolean needsSorting, RenderType original) {
        return new RenderType(
                name,
                original.getVertexFormat(),
                original.getDrawMode(),
                original.getBufferSize(),
                original.isUseDelegate(),
                needsSorting,
                original::setupRenderState,
                original::clearRenderState
        ) {
        };
    }
}
