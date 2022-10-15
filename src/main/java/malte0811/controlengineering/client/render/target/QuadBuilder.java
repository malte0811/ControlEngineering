package malte0811.controlengineering.client.render.target;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.util.BitUtils;
import malte0811.controlengineering.util.RLUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.OptionalInt;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ControlEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class QuadBuilder {
    public static final ResourceLocation WHITE_WITH_BORDER = RLUtils.ceLoc("white_with_border");

    private final Vertex[] vertices;
    @Nullable
    private TextureAtlasSprite sprite;
    @Nullable
    private Vec3 normal;
    // Range: [0, 15]
    private OptionalInt blockLightOverride = OptionalInt.empty();
    private float red = 1;
    private float green = 1;
    private float blue = 1;
    private float alpha = 1;

    public QuadBuilder(Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4) {
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

    public QuadBuilder setNormal(@Nullable Vec3 normal) {
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

    public void writeTo(VertexConsumer target) {
        TextureAtlasSprite sprite = this.sprite == null ? getWhiteTexture() : this.sprite;
        Vec3 normalD = this.normal == null ? automaticNormal() : this.normal;
        Vector3f normal = new Vector3f(normalD);
        for (Vertex v : vertices) {
            target.vertex(
                    (float) v.position.x, (float) v.position.y, (float) v.position.z,
                    red, green, blue, alpha,
                    sprite.getU(16 * v.spriteU), sprite.getV(16 * v.spriteV),
                    OverlayTexture.NO_OVERLAY, blockLightOverride.orElse(0),
                    normal.x(), normal.y(), normal.z()
            );
        }
    }

    private Vec3 automaticNormal() {
        Vec3 first = vertices[0].position;
        Vec3 second = vertices[1].position;
        Vec3 third = vertices[2].position;
        return first.subtract(second).cross(first.subtract(third)).normalize();
    }

    public static TextureAtlasSprite getWhiteTexture() {
        return Objects.requireNonNull(Minecraft.getInstance()
                .getModelManager()
                .getAtlas(InventoryMenu.BLOCK_ATLAS)
                .getSprite(WHITE_WITH_BORDER));
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre ev) {
        if (ev.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            ev.addSprite(WHITE_WITH_BORDER);
        }
    }

    private static class Vertex {
        private final Vec3 position;
        private float spriteU;
        private float spriteV;

        private Vertex(Vec3 position, float spriteU, float spriteV) {
            this.position = position;
            this.spriteU = spriteU;
            this.spriteV = spriteV;
        }
    }
}
