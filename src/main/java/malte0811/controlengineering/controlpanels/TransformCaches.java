package malte0811.controlengineering.controlpanels;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.util.math.MatrixUtils;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import java.util.EnumMap;
import java.util.Map;

public class TransformCaches {
    private static final Quaternion QUARTER_Y = new Quaternion(0, Mth.HALF_PI, 0, false);
    private static final Quaternion MINUS_QUARTER_Y = new Quaternion(0, -Mth.HALF_PI, 0, false);
    private static final Quaternion MINUS_QUARTER_X = new Quaternion(-Mth.HALF_PI, 0, 0, false);

    private static final Map<PanelOrientation, Matrix4f> PANEL_BOTTOM_TO_WORLD = new EnumMap<>(PanelOrientation.class);
    private static final Map<PanelOrientation, Matrix4f> WORLD_TO_PANEL_BOTTOM = new EnumMap<>(PanelOrientation.class);
    private static final LoadingCache<PanelTopState, Matrix4f> PANEL_TOP_TO_BOTTOM = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build(CacheLoader.from(pts -> {
                Matrix4f panelTopToPanelBottom = new Matrix4f();
                panelTopToPanelBottom.setIdentity();
                panelTopToPanelBottom.multiplyWithTranslation(0, pts.borderHeight, 0);
                panelTopToPanelBottom.multiply(new Quaternion(0, 0, pts.angle, false));
                panelTopToPanelBottom.multiplyWithTranslation(0.5f, 0, 0.5f);
                panelTopToPanelBottom.multiply(QUARTER_Y);
                panelTopToPanelBottom.multiplyWithTranslation(-0.5f, 0, -0.5f);
                return panelTopToPanelBottom;
            }));
    private static final LoadingCache<PanelTopState, Matrix4f> PANEL_BOTTOM_TO_TOP = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build(CacheLoader.from(pts -> {
                var temp = PANEL_TOP_TO_BOTTOM.getUnchecked(pts).copy();
                temp.invert();
                return temp;
            }));

    public static Matrix4f makePanelBottomToWorld(PanelOrientation bd) {
        return PANEL_BOTTOM_TO_WORLD.computeIfAbsent(bd, blockData -> {
            Matrix4f panelBottomToWorld = new Matrix4f();
            panelBottomToWorld.setIdentity();
            panelBottomToWorld.multiplyWithTranslation(0.5f, 0.5f, 0.5f);
            MatrixUtils.rotateFacing(panelBottomToWorld, blockData.top, 1);
            panelBottomToWorld.multiply(MINUS_QUARTER_X);
            if (blockData.top == Direction.DOWN) {
                MatrixUtils.rotateFacing(panelBottomToWorld, blockData.front, -1);
            } else {
                MatrixUtils.rotateFacing(panelBottomToWorld, blockData.front, 1);
            }
            panelBottomToWorld.multiply(MINUS_QUARTER_Y);
            panelBottomToWorld.multiplyWithTranslation(-0.5f, -0.5f, -0.5f);
            return panelBottomToWorld;
        });
    }

    public static Matrix4f makePanelTopToPanelBottom(float borderHeight, float radians) {
        return PANEL_TOP_TO_BOTTOM.getUnchecked(new PanelTopState(borderHeight, radians));
    }

    public static Matrix4f makePanelBottomToPanelTop(float borderHeight, float radians) {
        return PANEL_BOTTOM_TO_TOP.getUnchecked(new PanelTopState(borderHeight, radians));
    }

    public static Matrix4f makeWorldToPanelBottom(PanelOrientation bd) {
        return WORLD_TO_PANEL_BOTTOM.computeIfAbsent(bd, blockData -> {
            var as4f = makePanelBottomToWorld(blockData).copy();
            as4f.invert();
            return as4f;
        });
    }

    private static record PanelTopState(float borderHeight, float angle) {}
}
