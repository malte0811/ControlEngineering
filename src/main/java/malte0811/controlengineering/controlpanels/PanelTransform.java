package malte0811.controlengineering.controlpanels;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.util.Codecs;
import malte0811.controlengineering.util.Matrix4;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Objects;

//All transforms are for the "top" block of the control panel
public class PanelTransform {
    private static final Codec<TileTransformData> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Codec.FLOAT.fieldOf("height").forGetter(p -> p.centerHeight),
                    Codec.FLOAT.fieldOf("angle").forGetter(p -> p.degrees)
            ).apply(inst, TileTransformData::new)
    );

    private final TileTransformData tileData;
    //Transforms panel top coords (x, 0, y) to world coords
    private final Matrix4 panelTopToWorld;
    //Transforms rotated world coords (panel base) to actual world coords
    private final Matrix4 panelBottomToWorld;
    private final Matrix4 worldToPanelTop;

    public PanelTransform(float centerHeight, float degrees, PanelOrientation blockData) {
        this(new TileTransformData(centerHeight, degrees), blockData);
    }

    public PanelTransform(TileTransformData tileData, PanelOrientation blockData) {
        this.tileData = tileData;
        panelBottomToWorld = new Matrix4();
        panelBottomToWorld.translate(0.5, 0.5, 0.5);
        panelBottomToWorld.rotateFacing(blockData.top, 1);
        panelBottomToWorld.rotate(-Math.PI / 2, 1, 0, 0);
        if (blockData.top == Direction.DOWN && blockData.front.getAxis() == Direction.Axis.Z) {
            panelBottomToWorld.rotateFacing(blockData.front.getOpposite(), 1);
        } else {
            panelBottomToWorld.rotateFacing(blockData.front, 1);
        }
        panelBottomToWorld.rotate(-Math.PI / 2, 0, 1, 0);
        panelBottomToWorld.translate(-0.5, -0.5, -0.5);
        final double radians = Math.toRadians(tileData.degrees);
        double borderHeight = getFrontHeight();

        panelTopToWorld = new Matrix4();
        panelTopToWorld.multiply(getPanelBottomToWorld());
        panelTopToWorld.translate(0, borderHeight, 0);
        panelTopToWorld.rotate(radians, 0, 0, 1);

        worldToPanelTop = new Matrix4();
        worldToPanelTop.rotate(-radians, 0, 0, 1);
        worldToPanelTop.translate(0, -borderHeight, 0);
        //TODO deduplicate?
        worldToPanelTop.translate(0.5, 0.5, 0.5);
        worldToPanelTop.rotate(Math.PI / 2, 0, 1, 0);
        if (blockData.top == Direction.DOWN && blockData.front.getAxis() == Direction.Axis.Z) {
            worldToPanelTop.rotateFacing(blockData.front.getOpposite(), -1);
        } else {
            worldToPanelTop.rotateFacing(blockData.front, -1);
        }
        worldToPanelTop.rotate(Math.PI / 2, 1, 0, 0);
        worldToPanelTop.rotateFacing(blockData.top, -1);
        worldToPanelTop.translate(-0.5, -0.5, -0.5);
    }

    public RayTraceContext toPanelRay(Vector3d start, Vector3d end, BlockPos panelPos) {
        Vector3d offset = Vector3d.copy(panelPos);
        return new RayTraceContext(
                worldToPanelTop.apply(start.subtract(offset)),
                worldToPanelTop.apply(end.subtract(offset)),
                RayTraceContext.BlockMode.VISUAL,
                RayTraceContext.FluidMode.NONE,
                null
        );
    }

    public Matrix4 getPanelBottomToWorld() {
        return panelBottomToWorld;
    }

    public Matrix4 getPanelTopToWorld() {
        return panelTopToWorld;
    }

    public Matrix4 getWorldToPanelTop() {
        return worldToPanelTop;
    }

    public double getTopFaceHeight() {
        return 1 / Math.cos(Math.toRadians(tileData.degrees));
    }

    public void addTo(CompoundNBT out) {
        Codecs.add(CODEC, tileData, out, "transform");
    }

    public double getFrontHeight() {
        final double radians = Math.toRadians(tileData.degrees);
        return tileData.centerHeight - (Math.tan(radians) / 2);
    }

    public double getBackHeight() {
        final double radians = Math.toRadians(tileData.degrees);
        return tileData.centerHeight + (Math.tan(radians) / 2);
    }

    public static PanelTransform from(CompoundNBT nbt, PanelOrientation orientation) {
        TileTransformData tile = Codecs.read(CODEC, nbt, "transform");
        return new PanelTransform(tile, orientation);
    }

    public double getCenterHeight() {
        return tileData.centerHeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PanelTransform that = (PanelTransform) o;
        return tileData.equals(that.tileData) &&
                panelTopToWorld.equals(that.panelTopToWorld) &&
                panelBottomToWorld.equals(that.panelBottomToWorld) &&
                worldToPanelTop.equals(that.worldToPanelTop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tileData, panelTopToWorld, panelBottomToWorld, worldToPanelTop);
    }

    private static class TileTransformData {
        private final float centerHeight;
        private final float degrees;

        private TileTransformData(float centerHeight, float degrees) {
            this.centerHeight = centerHeight;
            this.degrees = degrees;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TileTransformData that = (TileTransformData) o;
            return Float.compare(that.centerHeight, centerHeight) == 0 && Float.compare(
                    that.degrees,
                    degrees
            ) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(centerHeight, degrees);
        }
    }
}
