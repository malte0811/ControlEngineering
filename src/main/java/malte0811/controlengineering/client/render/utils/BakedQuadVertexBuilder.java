package malte0811.controlengineering.client.render.utils;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BakedQuadVertexBuilder extends TransformingVertexBuilder {
    private final TextureAtlasSprite sprite;
    private final List<BakedQuad> quads;
    private BakedQuadBuilder builder = null;
    private int nextVertex = 0;
    private boolean interpolateUV = true;

    public BakedQuadVertexBuilder(
            TextureAtlasSprite sprite, PoseStack transform, List<BakedQuad> quads
    ) {
        super(null, transform, DefaultVertexFormat.BLOCK);
        this.sprite = sprite;
        this.quads = quads;
    }

    public BakedQuadVertexBuilder dontInterpolateUV() {
        interpolateUV = false;
        return this;
    }

    @Override
    public void endVertex() {
        if (builder == null) {
            builder = new BakedQuadBuilder();
        }
        Vec3 pos = this.pos.read();
        Vector4f transformedPos = new Vector4f((float) pos.x, (float) pos.y, (float) pos.z, 1);
        transformedPos.transform(transform.last().pose());
        transformedPos.perspectiveDivide();
        Vec2 uv = this.uv.read();
        Vector3f normal = this.normal.read();
        normal.transform(transform.last().normal());
        builder.putVertexData(
                transformedPos, normal,
                interpolateUV ? sprite.getU(uv.x) : uv.x,
                interpolateUV ? sprite.getV(uv.y) : uv.y,
                color.read()
        );
        this.lightmap.clear();
        this.overlay.clear();
        ++nextVertex;
        if (nextVertex == 4) {
            nextVertex = 0;
            quads.add(builder.bake(-1, Direction.getNearest(normal.x(), normal.y(), normal.z()), sprite, true));
            builder = null;
        }
    }
}
