package malte0811.controlengineering.controlpanels;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.util.math.MatrixUtils;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.EnumMap;
import java.util.Map;

public class TransformCaches {
    private static final Map<PanelOrientation, Matrix4fc> PANEL_BOTTOM_TO_WORLD = new EnumMap<>(PanelOrientation.class);
    private static final Map<PanelOrientation, Matrix4fc> WORLD_TO_PANEL_BOTTOM = new EnumMap<>(PanelOrientation.class);
    private static final LoadingCache<PanelTopState, Matrix4fc> PANEL_TOP_TO_BOTTOM = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build(CacheLoader.from(
                    pts -> new Matrix4f()
                            .translate(0, pts.borderHeight, 0)
                            .rotateZ(pts.angle)
                            .translate(0.5f, 0, 0.5f)
                            .rotateY(Mth.HALF_PI)
                            .translate(-0.5f, 0, -0.5f)
            ));
    private static final LoadingCache<PanelTopState, Matrix4fc> PANEL_BOTTOM_TO_TOP = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build(CacheLoader.from(pts -> new Matrix4f(PANEL_TOP_TO_BOTTOM.getUnchecked(pts)).invert()));

    public static Matrix4fc makePanelBottomToWorld(PanelOrientation bd) {
        return PANEL_BOTTOM_TO_WORLD.computeIfAbsent(bd, blockData -> {
            Matrix4f panelBottomToWorld = new Matrix4f()
                    .translate(0.5f, 0.5f, 0.5f);
            MatrixUtils.rotateFacing(panelBottomToWorld, blockData.top, 1);
            panelBottomToWorld.rotateX(-Mth.HALF_PI);
            if (blockData.top == Direction.DOWN) {
                MatrixUtils.rotateFacing(panelBottomToWorld, blockData.front, -1);
            } else {
                MatrixUtils.rotateFacing(panelBottomToWorld, blockData.front, 1);
            }
            panelBottomToWorld.rotateY(-Mth.HALF_PI);
            panelBottomToWorld.translate(-0.5f, -0.5f, -0.5f);
            return panelBottomToWorld;
        });
    }

    public static Matrix4fc makePanelTopToPanelBottom(float borderHeight, float radians) {
        return PANEL_TOP_TO_BOTTOM.getUnchecked(new PanelTopState(borderHeight, radians));
    }

    public static Matrix4fc makePanelBottomToPanelTop(float borderHeight, float radians) {
        return PANEL_BOTTOM_TO_TOP.getUnchecked(new PanelTopState(borderHeight, radians));
    }

    public static Matrix4fc makeWorldToPanelBottom(PanelOrientation bd) {
        return WORLD_TO_PANEL_BOTTOM.computeIfAbsent(
                bd,
                blockData -> new Matrix4f(makePanelBottomToWorld(blockData)).invert()
        );
    }

    private record PanelTopState(float borderHeight, float angle) { }
}
