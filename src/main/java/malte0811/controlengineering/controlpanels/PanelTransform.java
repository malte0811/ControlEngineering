package malte0811.controlengineering.controlpanels;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.util.math.Matrix4;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
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
        if (blockData.top == Direction.DOWN) {
            panelBottomToWorld.rotateFacing(blockData.front, -1);
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
        panelTopToWorld.translate(0.5, 0, 0.5);
        panelTopToWorld.rotate(Math.PI / 2, 0, 1, 0);
        panelTopToWorld.translate(-0.5, 0, -0.5);

        worldToPanelTop = new Matrix4();
        worldToPanelTop.translate(0.5, 0, 0.5);
        worldToPanelTop.rotate(-Math.PI / 2, 0, 1, 0);
        worldToPanelTop.translate(-0.5, 0, -0.5);
        worldToPanelTop.rotate(-radians, 0, 0, 1);
        worldToPanelTop.translate(0, -borderHeight, 0);
        //TODO deduplicate?
        worldToPanelTop.translate(0.5, 0.5, 0.5);
        worldToPanelTop.rotate(Math.PI / 2, 0, 1, 0);
        if (blockData.top == Direction.DOWN) {
            worldToPanelTop.rotateFacing(blockData.front, 1);
        } else {
            worldToPanelTop.rotateFacing(blockData.front, -1);
        }
        worldToPanelTop.rotate(Math.PI / 2, 1, 0, 0);
        worldToPanelTop.rotateFacing(blockData.top, -1);
        worldToPanelTop.translate(-0.5, -0.5, -0.5);
    }

    public PanelTransform() {
        this(0.25F, (float) Math.toDegrees(Math.atan(0.5)), PanelOrientation.DOWN_NORTH);
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

    public void addTo(CompoundTag out) {
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

    public static PanelTransform from(CompoundTag nbt, PanelOrientation orientation) {
        TileTransformData tile = Codecs.read(CODEC, nbt, "transform")
                .result()
                .orElseGet(TileTransformData::new);
        return new PanelTransform(tile, orientation);
    }

    public double getCenterHeight() {
        return tileData.centerHeight;
    }

    public Vec3[] getBottomVertices() {
        Vec3[] bottomVertices = layerVertices(1);
        for (int i = 0; i < 4; ++i) {
            bottomVertices[i] = getPanelBottomToWorld().apply(bottomVertices[i]);
        }
        return bottomVertices;
    }

    public Vec3[] getTopVertices() {
        Vec3[] topVertices = layerVertices(getTopFaceHeight());
        for (int i = 0; i < 4; ++i) {
            topVertices[i] = getPanelTopToWorld().apply(topVertices[i]);
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
        return tileData.equals(that.tileData) &&
                panelTopToWorld.equals(that.panelTopToWorld) &&
                panelBottomToWorld.equals(that.panelBottomToWorld) &&
                worldToPanelTop.equals(that.worldToPanelTop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tileData, panelTopToWorld, panelBottomToWorld, worldToPanelTop);
    }

    private static record TileTransformData(float centerHeight, float degrees) {
        private TileTransformData() {
            this(0.25F, (float) -Math.toDegrees(Math.atan(0.5)));
        }
    }
}
