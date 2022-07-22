package malte0811.controlengineering.client.render.utils;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class BakedQuadBuilder
{
    public static final VertexFormat FORMAT = DefaultVertexFormat.BLOCK;

    private int nextVertex = 0;
    private final int[] data = new int[FORMAT.getIntegerSize()*4];

    public void putVertexData(Vector4f pos, Vector3f faceNormal, double u, double v, Vector4f color)
    {
        int next = nextVertex*FORMAT.getIntegerSize();

        pos.perspectiveDivide();
        data[next++] = Float.floatToIntBits(pos.x());
        data[next++] = Float.floatToIntBits(pos.y());
        data[next++] = Float.floatToIntBits(pos.z());

        data[next++] = (int)(color.x()*255)|
                ((int)(color.y()*255)<<8)|
                ((int)(color.z()*255)<<16)|
                ((int)(color.w()*255)<<24);

        data[next++] = Float.floatToIntBits((float)u);
        data[next++] = Float.floatToIntBits((float)v);

        data[next++] = 0;

        data[next] |= (int)(faceNormal.x()*127)&255;
        data[next] |= ((int)(faceNormal.y()*127)&255)<<8;
        data[next] |= ((int)(faceNormal.z()*127)&255)<<16;
        ++next;

        ++nextVertex;
        Preconditions.checkState(next==nextVertex*FORMAT.getIntegerSize());
    }

    public BakedQuad bake(int tint, Direction side, TextureAtlasSprite texture, boolean shade)
    {
        return new BakedQuad(data, tint, side, texture, shade);
    }
}
