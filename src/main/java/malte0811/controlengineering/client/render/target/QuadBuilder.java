package malte0811.controlengineering.client.render.target;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.util.BitUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.OptionalInt;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ControlEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class QuadBuilder {
    public static final ResourceLocation WHITE_WITH_BORDER = new ResourceLocation(
            ControlEngineering.MODID,
            "white_with_border"
    );

    private final Vertex[] vertices;
    @Nullable
    private TextureAtlasSprite sprite;
    @Nullable
    private Vector3d normal;
    // Range: [0, 15]
    private OptionalInt blockLightOverride = OptionalInt.empty();
    private float red = 1;
    private float green = 1;
    private float blue = 1;
    private float alpha = 1;

    public QuadBuilder(Vector3d v1, Vector3d v2, Vector3d v3, Vector3d v4) {
        this.vertices = new Vertex[]{
                new Vertex(v1, 0, 0),
                new Vertex(v2, 0, 1),
                new Vertex(v3, 1, 1),
                new Vertex(v4, 1, 0),
        };
    }

    public QuadBuilder setSprite(@Nonnull TextureAtlasSprite sprite) {
        this.sprite = sprite;
        return this;
    }

    public QuadBuilder setNormal(@Nullable Vector3d normal) {
        this.normal = normal;
        return this;
    }

    public QuadBuilder setUCoords(float u1, float u2, float u3, float u4) {
        vertices[0].spriteU = u1;
        vertices[1].spriteU = u2;
        vertices[2].spriteU = u3;
        vertices[3].spriteU = u4;
        return this;
    }

    public QuadBuilder setVCoords(float v1, float v2, float v3, float v4) {
        vertices[0].spriteV = v1;
        vertices[1].spriteV = v2;
        vertices[2].spriteV = v3;
        vertices[3].spriteV = v4;
        return this;
    }

    public QuadBuilder setRGBA(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        return this;
    }

    public QuadBuilder setRGB(int packed) {
        return setRGBA(
                extract8BitFloat(packed, 16), extract8BitFloat(packed, 8),
                extract8BitFloat(packed, 0), 1
        );
    }

    private static float extract8BitFloat(int value, int offset) {
        return BitUtils.getBits(value, offset, 8) / 255f;
    }

    public QuadBuilder setBlockLightOverride(int blockLightOverride) {
        this.blockLightOverride = OptionalInt.of(blockLightOverride);
        return this;
    }

    public void writeTo(MatrixStack transform, RenderTarget target, TargetType type) {
        if (!target.isEnabled(type)) {
            return;
        }
        TextureAtlasSprite sprite = this.sprite == null ? getWhiteTexture() : this.sprite;
        target.setTexture(sprite);
        Vector3d normalD = this.normal == null ? automaticNormal() : this.normal;
        Vector3f normal = new Vector3f(normalD);
        MatrixStack.Entry last = transform.getLast();
        normal.transform(last.getNormal());
        for (Vertex v : vertices) {
            Vector4f posF = new Vector4f((float) v.position.x, (float) v.position.y, (float) v.position.z, 1);
            posF.transform(last.getMatrix());
            posF.perspectiveDivide();
            target.addVertex(
                    posF, normal, red, green, blue, alpha,
                    sprite.getInterpolatedU(16 * v.spriteU), sprite.getInterpolatedV(16 * v.spriteV),
                    blockLightOverride
            );
        }
    }

    private Vector3d automaticNormal() {
        Vector3d first = vertices[0].position;
        Vector3d second = vertices[1].position;
        Vector3d third = vertices[2].position;
        return first.subtract(second).crossProduct(first.subtract(third)).normalize();
    }

    public static TextureAtlasSprite getWhiteTexture() {
        return Minecraft.getInstance()
                .getModelManager()
                .getAtlasTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE)
                .getSprite(WHITE_WITH_BORDER);
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre ev) {
        if (ev.getMap().getTextureLocation().equals(PlayerContainer.LOCATION_BLOCKS_TEXTURE)) {
            ev.addSprite(WHITE_WITH_BORDER);
        }
    }

    private static class Vertex {
        private final Vector3d position;
        private float spriteU;
        private float spriteV;

        private Vertex(Vector3d position, float spriteU, float spriteV) {
            this.position = position;
            this.spriteU = spriteU;
            this.spriteV = spriteV;
        }
    }
}
