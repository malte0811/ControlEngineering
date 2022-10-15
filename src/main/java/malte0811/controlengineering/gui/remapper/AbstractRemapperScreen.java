package malte0811.controlengineering.gui.remapper;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.network.remapper.ClearMapping;
import malte0811.controlengineering.network.remapper.SetMapping;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class AbstractRemapperScreen extends Screen implements MenuAccess<AbstractRemapperMenu> {
    protected static final ResourceLocation TEXTURE = ControlEngineering.ceLoc("textures/gui/rs_remapper.png");
    protected static final int WIDTH = 165;
    protected static final int HEIGHT = 154;
    private static final int WIRE_COLOR = 0xffb66232;
    private static final int HIGHLIGHT_WIRE_COLOR = 0xffb68332;
    private static final SubTexture BACKGROUND = new SubTexture(TEXTURE, 67, 0, 67 + WIDTH, HEIGHT);
    private final AbstractRemapperMenu menu;
    private final List<ConnectionPoint> sourceConnectionPoints;
    private final List<ConnectionPoint> targetConnectionPoints;
    protected int leftPos;
    protected int topPos;
    @Nullable
    private ConnectionPoint fixedEndOfConnecting;

    public AbstractRemapperScreen(
            AbstractRemapperMenu menu,
            List<ConnectionPoint> sourceConnectionPoints,
            List<ConnectionPoint> targetConnectionPoints
    ) {
        super(Component.empty());
        this.menu = menu;
        this.sourceConnectionPoints = sourceConnectionPoints;
        this.targetConnectionPoints = targetConnectionPoints;
    }

    @Override
    public void render(@Nonnull PoseStack transform, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(transform);
        transform.pushPose();
        transform.translate(leftPos, topPos, 0);
        renderConnections(transform);
        if (fixedEndOfConnecting != null) {
            renderWireAtMouse(transform, fixedEndOfConnecting, mouseX - this.leftPos, mouseY - this.topPos);
        } else {
            var hovered = getCPUnder(mouseX, mouseY);
            if (hovered != null) {
                var connectedTo = getOtherEnd(hovered);
                if (connectedTo != null) {
                    renderFullyConnectedWire(transform, hovered, connectedTo, HIGHLIGHT_WIRE_COLOR);
                }
            }
        }
        transform.popPose();
        super.render(transform, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(@Nonnull PoseStack transform, int vOffset) {
        super.renderBackground(transform, vOffset);
        transform.pushPose();
        transform.translate(leftPos, topPos, vOffset);
        BACKGROUND.blit(transform, 0, 0);
        transform.popPose();
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (width - WIDTH) / 2;
        this.topPos = (height - HEIGHT) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (fixedEndOfConnecting != null && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            fixedEndOfConnecting = null;
            return true;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            var clicked = getCPUnder(mouseX, mouseY);
            if (clicked != null) {
                onClicked(clicked);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Nullable
    private ConnectionPoint getCPUnder(double mouseX, double mouseY) {
        var relativeX = (int) (mouseX - leftPos);
        var relativeY = (int) (mouseY - topPos);
        for (var connPoint : getAllConnectionPoints()) {
            if (connPoint.area().contains(relativeX, relativeY)) {
                return connPoint;
            }
        }
        return null;
    }

    private void onClicked(ConnectionPoint clicked) {
        if (fixedEndOfConnecting != null) {
            if (clicked.isMappingSource != fixedEndOfConnecting.isMappingSource) {
                var oldOtherEnd = getOtherEnd(clicked);
                var colorIndex = getIndexAtColor(clicked, fixedEndOfConnecting);
                var grayIndex = clicked.isMappingSource ? fixedEndOfConnecting.index : clicked.index;
                menu.processAndSend(new SetMapping(colorIndex, grayIndex));
                fixedEndOfConnecting = oldOtherEnd;
            } else {
                fixedEndOfConnecting = null;
            }
        } else {
            var otherEnd = getOtherEnd(clicked);
            if (otherEnd == null) {
                fixedEndOfConnecting = clicked;
            } else {
                menu.processAndSend(new ClearMapping(getIndexAtColor(clicked, otherEnd)));
                fixedEndOfConnecting = otherEnd;
            }
        }
    }

    private void renderConnections(PoseStack transform) {
        var mapping = menu.getMapping();
        for (int sourceIndex = 0; sourceIndex < mapping.length; ++sourceIndex) {
            var mappedTo = mapping[sourceIndex];
            if (mappedTo == AbstractRemapperMenu.NOT_MAPPED) {
                continue;
            }
            var sourceCP = sourceConnectionPoints.get(sourceIndex);
            var targetCP = targetConnectionPoints.get(mappedTo);
            sourceCP.sprite.blit(transform, sourceCP.area().getX(), sourceCP.area().getY());
            targetCP.sprite.blit(transform, targetCP.area().getX(), targetCP.area().getY());
            renderFullyConnectedWire(transform, sourceCP, targetCP, WIRE_COLOR);
        }
    }

    private void renderFullyConnectedWire(PoseStack transform, ConnectionPoint start, ConnectionPoint end, int color) {
        if (!start.isMappingSource) {
            var temp = start;
            start = end;
            end = temp;
        }
        final var yStart = start.wireY();
        final var xStart = start.wireX();
        final var yEnd = end.wireY();
        final var xEnd = end.wireX();
        final float wireRadius = 1;
        float deltaX = xEnd - xStart;
        float deltaY = yEnd - yStart;
        float cosAlpha = (float) (deltaX / Math.sqrt(deltaY * deltaY + deltaX * deltaX));
        float halfWireHeight = Math.abs(wireRadius / cosAlpha);

        renderWire(
                transform,
                xStart, yStart + halfWireHeight,
                xEnd, yEnd + halfWireHeight,
                xEnd, yEnd - halfWireHeight,
                xStart, yStart - halfWireHeight,
                color
        );
    }

    private void renderWireAtMouse(PoseStack transform, ConnectionPoint fixed, int mouseX, int mouseY) {
        var fixedY = fixed.wireY();
        var fixedX = fixed.wireX();
        Vec2 radius = new Vec2(fixedY - mouseY, mouseX - fixedX).normalized();

        renderWire(
                transform,
                fixedX + radius.x, fixedY + radius.y,
                mouseX + radius.x, mouseY + radius.y,
                mouseX - radius.x, mouseY - radius.y,
                fixedX - radius.x, fixedY - radius.y,
                WIRE_COLOR
        );
    }

    private void renderWire(
            PoseStack transform,
            float x1, float y1,
            float x2, float y2,
            float x3, float y3,
            float x4, float y4,
            int color
    ) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var matrix = transform.last().pose();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(matrix, x1, y1, 1.0F).color(color).endVertex();
        bufferbuilder.vertex(matrix, x2, y2, 1.0F).color(color).endVertex();
        bufferbuilder.vertex(matrix, x3, y3, 1.0F).color(color).endVertex();
        bufferbuilder.vertex(matrix, x4, y4, 1.0F).color(color).endVertex();
        BufferUploader.draw(bufferbuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    @Nonnull
    @Override
    public AbstractRemapperMenu getMenu() {
        return menu;
    }

    @Nullable
    private ConnectionPoint getOtherEnd(ConnectionPoint first) {
        int otherIndex;
        int invalid;
        var mapping = menu.getMapping();
        if (first.isMappingSource) {
            otherIndex = mapping[first.index];
            invalid = AbstractRemapperMenu.NOT_MAPPED;
        } else {
            otherIndex = ArrayUtils.indexOf(mapping, first.index);
            invalid = ArrayUtils.INDEX_NOT_FOUND;
        }
        if (otherIndex != invalid) {
            if (first.isMappingSource) {
                return targetConnectionPoints.get(otherIndex);
            } else {
                return sourceConnectionPoints.get(otherIndex);
            }
        } else {
            return null;
        }
    }

    private int getIndexAtColor(ConnectionPoint first, ConnectionPoint second) {
        return first.isMappingSource ? first.index : second.index;
    }

    private Iterable<ConnectionPoint> getAllConnectionPoints() {
        return Iterables.concat(sourceConnectionPoints, targetConnectionPoints);
    }

    protected record ConnectionPoint(
            boolean isMappingSource, int index, Vec2i min, SubTexture sprite
    ) {
        public Rect2i area() {
            return new Rect2i(min.x(), min.y(), sprite.getWidth(), sprite.getHeight());
        }

        public int wireX() {
            return isMappingSource ? area().getX() + area().getWidth() : area().getX();
        }

        public int wireY() {
            return area().getY() + area().getHeight() / 2;
        }
    }
}
