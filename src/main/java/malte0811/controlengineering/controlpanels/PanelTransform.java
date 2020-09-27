package malte0811.controlengineering.controlpanels;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.util.Codecs;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.*;

import static malte0811.controlengineering.util.MatrixUtils.transform;

public class PanelTransform {
    public static final Codec<PanelTransform> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Codec.FLOAT.fieldOf("height").forGetter(p -> p.centerHeight),
                    Codec.FLOAT.fieldOf("angle").forGetter(p -> p.degrees),
                    Codecs.DIRECTION_CODEC.fieldOf("placedOn").forGetter(p -> p.placedOn)
            ).apply(inst, PanelTransform::new)
    );

    private final float centerHeight;
    private final float degrees;
    private final Direction placedOn;
    private final TransformationMatrix panelTopToWorld;
    private final TransformationMatrix panelBottomToWorld;
    private final TransformationMatrix worldToPanelTop;

    public PanelTransform(float centerHeight, float degrees, Direction placedOn) {
        this.centerHeight = centerHeight;
        this.degrees = degrees;
        this.placedOn = placedOn;
        //TODO include placedOn
        float borderHeight = centerHeight - (float) (Math.tan(Math.toRadians(degrees)) / 2);
        panelTopToWorld = new TransformationMatrix(
                new Vector3f(0, borderHeight, 0),
                new Quaternion(0, 0, degrees, true),
                null,
                null
        );
        worldToPanelTop = panelTopToWorld.inverse();
        panelBottomToWorld = new TransformationMatrix(null, null, null, null);
    }

    public RayTraceContext toPanelRay(Vector3d start, Vector3d end, BlockPos panelPos) {
        Vector3d offset = Vector3d.copy(panelPos);
        return new RayTraceContext(
                transform(start.subtract(offset), worldToPanelTop),
                transform(end.subtract(offset), worldToPanelTop),
                RayTraceContext.BlockMode.VISUAL,
                RayTraceContext.FluidMode.NONE,
                null
        );
    }

    public TransformationMatrix getPanelBottomToWorld() {
        return panelBottomToWorld;
    }

    public TransformationMatrix getPanelTopToWorld() {
        return panelTopToWorld;
    }

    public TransformationMatrix getWorldToPanelTop() {
        return worldToPanelTop;
    }

    public double getTopFaceHeight() {
        return 1 / Math.cos(Math.toRadians(degrees));
    }
}
