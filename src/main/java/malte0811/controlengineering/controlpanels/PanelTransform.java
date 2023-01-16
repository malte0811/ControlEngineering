package malte0811.controlengineering.controlpanels;

import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.util.math.MatrixUtils;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.Objects;

//All transforms are for the "top" block of the control panel
public class PanelTransform {
    private static final MyCodec<BETransformData> CODEC = new RecordCodec2<>(
            new CodecField<>("height", p -> p.centerHeight, MyCodecs.FLOAT),
            new CodecField<>("angle", p -> p.degrees, MyCodecs.FLOAT),
            BETransformData::new
    );

    private final BETransformData bEntityData;
    //Transforms panel top coords (x, 0, y) to world coords
    private final Matrix4f panelTopToWorld;
    //Transforms rotated world coords (panel base) to actual world coords
    private final Matrix4fc panelBottomToWorld;
    private final Matrix4fc worldToPanelTop;

    public PanelTransform(float centerHeight, float degrees, PanelOrientation blockData) {
        this(new BETransformData(centerHeight, degrees), blockData);
    }

    public PanelTransform(BETransformData bEntityData, PanelOrientation blockData) {
        this.bEntityData = bEntityData;
        final float radians = (float) Math.toRadians(bEntityData.degrees);
        final float borderHeight = getFrontHeight();

        panelBottomToWorld = TransformCaches.makePanelBottomToWorld(blockData);
        panelTopToWorld = new Matrix4f(panelBottomToWorld);
        panelTopToWorld.mul(TransformCaches.makePanelTopToPanelBottom(borderHeight, radians));

        worldToPanelTop = new Matrix4f(TransformCaches.makePanelBottomToPanelTop(borderHeight, radians))
                .mul(TransformCaches.makeWorldToPanelBottom(blockData));
    }

    public PanelTransform() {
        this(0.25F, (float) Math.toDegrees(Math.atan(0.5)), PanelOrientation.DOWN_NORTH);
    }

    public static PanelTransform withHeights(float frontHeight, float backHeight, PanelOrientation orientation) {
        var centerHeight = (frontHeight + backHeight) / 2;
        var angle = Math.atan(backHeight - frontHeight);
        return new PanelTransform(centerHeight, (float) Math.toDegrees(angle), orientation);
    }

    public Matrix4fc getPanelBottomToWorld() {
        return panelBottomToWorld;
    }

    public Matrix4f getPanelTopToWorld() {
        return panelTopToWorld;
    }

    public Matrix4fc getWorldToPanelTop() {
        return worldToPanelTop;
    }

    public double getTopFaceHeight() {
        return 1 / Math.cos(Math.toRadians(bEntityData.degrees));
    }

    public void addTo(CompoundTag out) {
        out.put("transform", CODEC.toNBT(bEntityData));
    }

    public float getFrontHeight() {
        final double radians = Math.toRadians(bEntityData.degrees);
        return (float) (bEntityData.centerHeight - (Math.tan(radians) / 2));
    }

    public float getBackHeight() {
        final double radians = Math.toRadians(bEntityData.degrees);
        return (float) (bEntityData.centerHeight + (Math.tan(radians) / 2));
    }

    public static PanelTransform from(CompoundTag nbt, PanelOrientation orientation) {
        BETransformData beData = CODEC.fromNBT(nbt.get("transform"), BETransformData::new);
        return new PanelTransform(beData, orientation);
    }

    public double getCenterHeight() {
        return bEntityData.centerHeight;
    }

    public Vec3[] getBottomVertices() {
        Vec3[] bottomVertices = layerVertices(1);
        for (int i = 0; i < 4; ++i) {
            bottomVertices[i] = MatrixUtils.transform(getPanelBottomToWorld(), bottomVertices[i]);
        }
        return bottomVertices;
    }

    public Vec3[] getTopVertices() {
        Vec3[] topVertices = layerVertices(getTopFaceHeight());
        for (int i = 0; i < 4; ++i) {
            topVertices[i] = MatrixUtils.transform(getPanelTopToWorld(), topVertices[i]);
        }
        Vec3 temp = topVertices[0];
        System.arraycopy(topVertices, 1, topVertices, 0, topVertices.length - 1);
        topVertices[topVertices.length - 1] = temp;
        return topVertices;
    }

    public static Vec3[] layerVertices(double xMax) {
        return new Vec3[]{
                new Vec3(0, 0, 0),
                new Vec3(1, 0, 0),
                new Vec3(1, 0, xMax),
                new Vec3(0, 0, xMax),
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PanelTransform that = (PanelTransform) o;
        return bEntityData.equals(that.bEntityData) &&
                panelTopToWorld.equals(that.panelTopToWorld) &&
                panelBottomToWorld.equals(that.panelBottomToWorld) &&
                worldToPanelTop.equals(that.worldToPanelTop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bEntityData, panelTopToWorld, panelBottomToWorld, worldToPanelTop);
    }

    private record BETransformData(float centerHeight, float degrees) {
        private BETransformData() {
            this(0.25F, (float) -Math.toDegrees(Math.atan(0.5)));
        }
    }
}
