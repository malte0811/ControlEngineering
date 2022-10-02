package malte0811.controlengineering.gui.scope;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import malte0811.controlengineering.client.CEShaders;
import malte0811.controlengineering.client.render.utils.ScreenUtils;
import malte0811.controlengineering.scope.trace.Trace;
import malte0811.controlengineering.scope.trace.TraceId;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

import static malte0811.controlengineering.blockentity.bus.ScopeBlockEntity.NUM_HORIZONTAL_DIVS;
import static malte0811.controlengineering.blockentity.bus.ScopeBlockEntity.NUM_VERTICAL_DIVS;

public class CRTDisplay {
    private static final int BACKGROUND = 0xff007695;
    private static final int DIVIDER_COLOR = 0xff00586f;
    private static final int CENTER_DIVIDER_COLOR = 0xff00323e;
    private static final float VERTICAL_DIVIDER_RADIUS = 0.05f;

    private final ScopeMenu menu;
    private final int xMin;
    private final int yMin;
    private final int width;
    private final int height;
    private final float divWidth;
    private final float divHeight;

    public CRTDisplay(ScopeMenu menu, int xMin, int yMin, int width, int height) {
        this.menu = menu;
        this.xMin = xMin;
        this.yMin = yMin;
        this.width = width;
        this.height = height;
        this.divWidth = width / (float) NUM_HORIZONTAL_DIVS;
        this.divHeight = height / (float) NUM_VERTICAL_DIVS;
    }

    public void draw(PoseStack transform, @Nullable TraceId hovered) {
        ScreenUtils.setupScissorMCScaled(xMin, yMin, width, height);
        transform.pushPose();
        transform.translate(xMin, yMin, 0);
        transform.scale(divWidth, divHeight, 1);
        drawBackground(transform);
        for (final var trace : menu.getTraces().traces()) {
            drawTrace(transform, trace, trace.getTraceId().equals(hovered));
        }
        RenderSystem.disableScissor();
        transform.popPose();
    }

    private void drawBackground(PoseStack transform) {
        Screen.fill(transform, 0, 0, NUM_HORIZONTAL_DIVS, NUM_VERTICAL_DIVS, BACKGROUND);
        for (int divH = 0; divH <= NUM_HORIZONTAL_DIVS; ++divH) {
            drawVerticalDivider(transform, divH, DIVIDER_COLOR);
        }
        for (int divV = 0; divV <= NUM_VERTICAL_DIVS; ++divV) {
            drawHorizontalDivider(transform, divV, DIVIDER_COLOR);
        }
        drawHorizontalDivider(transform, NUM_VERTICAL_DIVS / 2, CENTER_DIVIDER_COLOR);
        drawVerticalDivider(transform, NUM_HORIZONTAL_DIVS / 2, CENTER_DIVIDER_COLOR);
    }

    private void drawHorizontalDivider(PoseStack transform, int pos, int color) {
        ScreenUtils.fill(
                transform,
                0,
                pos - VERTICAL_DIVIDER_RADIUS,
                NUM_HORIZONTAL_DIVS,
                pos + VERTICAL_DIVIDER_RADIUS,
                color
        );
    }

    private void drawVerticalDivider(PoseStack transform, int pos, int color) {
        final var radius = divHeight / divWidth * VERTICAL_DIVIDER_RADIUS;
        ScreenUtils.fill(transform, pos - radius, 0, pos + radius, NUM_VERTICAL_DIVS, color);
    }

    private static final int[][] RG_SEQUENCE = {
            new int[]{0, 1}, new int[]{1, 1}, new int[]{1, 0}, new int[]{0, 0},
    };

    private static void putTraceVertex(
            VertexConsumer out, Matrix4f mat, double x, double y, int rgIndex, float scaledDelay, float alpha
    ) {
        final var rg = RG_SEQUENCE[rgIndex % RG_SEQUENCE.length];
        out.vertex(mat, (float) x, (float) y, 0.0F).color(rg[0], rg[1], scaledDelay, alpha).endVertex();
    }

    private static void fillTrace(
            PoseStack transform,
            double minX, double minY, double maxX, double maxY,
            float alpha, double delayLeft, double delayRight, boolean vertical
    ) {
        Matrix4f matrix = transform.last().pose();
        final var bufferbuilder = Tesselator.getInstance().getBuilder();
        final float scaledDLeft = Math.min(1, (float) (delayLeft / 1e4));
        final float scaledDRight = Math.min(1, (float) (delayRight / 1e4));
        final int indexOff = vertical ? 1 : 0;
        putTraceVertex(bufferbuilder, matrix, minX, maxY, indexOff, scaledDLeft, alpha);
        putTraceVertex(bufferbuilder, matrix, maxX, maxY, indexOff + 1, scaledDRight, alpha);
        putTraceVertex(bufferbuilder, matrix, maxX, minY, indexOff + 2, scaledDRight, alpha);
        putTraceVertex(bufferbuilder, matrix, minX, minY, indexOff + 3, scaledDLeft, alpha);
    }

    private void drawTrace(PoseStack transform, Trace trace, boolean highlight) {
        final double samplesPerDiv = menu.getTraces().ticksPerDiv();
        ScreenUtils.startPositionColorDraw();
        RenderSystem.setShader(CEShaders::getScopeTrace);
        final var samples = trace.getSamples();
        final long now = System.currentTimeMillis();
        for (int i = 0; i < samples.size() - 1; ++i) {
            final var yHere = 10 - samples.getDouble(i);
            final float delayHere;
            final float delayNext;
            if (highlight) {
                delayHere = delayNext = 0;
            } else {
                delayHere = (float) (now - trace.getSampleTimestamps().getLong(i));
                delayNext = (float) (now - trace.getSampleTimestamps().getLong(i + 1));
            }
            final var traceRadius = 0.1 / Math.sqrt(divHeight / 10.);
            fillTrace(
                    transform,
                    i / samplesPerDiv, yHere - traceRadius, (i + 1) / samplesPerDiv, yHere + traceRadius,
                    1, delayHere, delayNext, false
            );
            final var yNext = 10 - samples.getDouble(i + 1);
            final var minY = Math.min(yNext, yHere);
            final var maxY = Math.max(yNext, yHere);
            final var height = maxY - minY;
            final var alpha = (int) (255 * Mth.clamp(0.75 - height / 5, 0, 1));
            fillTrace(
                    transform,
                    (i + 1) / samplesPerDiv - traceRadius / 2, minY,
                    (i + 1) / samplesPerDiv + traceRadius / 2, maxY,
                    alpha, delayHere, delayHere, true
            );
        }
        ScreenUtils.endPositionColorDraw();
    }
}
