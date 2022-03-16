package malte0811.controlengineering.gui.remapper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.bus.RSRemapperBlockEntity;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.network.remapper.ClearMapping;
import malte0811.controlengineering.network.remapper.SetMapping;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RSRemapperScreen extends Screen implements MenuAccess<RSRemapperMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ControlEngineering.MODID, "textures/gui/rs_remapper.png"
    );
    private static final int WIDTH = 113;
    private static final int HEIGHT = 154;
    private static final int WRAP_X_COLOR = 19;
    private static final int WRAP_X_GRAY = 90;
    private static final int FIRST_WRAP_Y = 15;
    private static final int COLOR_HEIGHT = 8;
    private static final int WIRE_COLOR = 0xffb66232;
    private static final SubTexture BACKGROUND = new SubTexture(TEXTURE, 0, 0, WIDTH, HEIGHT);
    private static final SubTexture WRAPPED_WIRE = new SubTexture(TEXTURE, 0, HEIGHT, 3, HEIGHT + 4);
    private static final List<Pair<Rect2i, ConnectionPoint>> RELATIVE_REGIONS = Util.make(new ArrayList<>(), list -> {
        for (int i = 0; i < BusLine.LINE_SIZE; ++i) {
            var yMin = FIRST_WRAP_Y + i * COLOR_HEIGHT;
            for (var color : List.of(true, false)) {
                var xMin = color ? WRAP_X_COLOR : WRAP_X_GRAY;
                list.add(Pair.of(
                        new Rect2i(xMin, yMin, WRAPPED_WIRE.getWidth(), WRAPPED_WIRE.getHeight()),
                        new ConnectionPoint(color, i)
                ));
            }
        }
    });
    private final RSRemapperMenu menu;
    private int leftPos;
    private int topPos;
    @Nullable
    private ConnectionPoint fixedEndOfConnecting;

    public RSRemapperScreen(RSRemapperMenu menu) {
        super(TextComponent.EMPTY);
        this.menu = menu;
    }

    @Override
    public void render(@Nonnull PoseStack transform, int mouseX, int mouseY, float partialTick) {
        transform.pushPose();
        transform.translate(leftPos, topPos, 0);
        BACKGROUND.blit(transform, 0, 0);
        renderConnections(transform);
        if (fixedEndOfConnecting != null) {
            renderWireAtMouse(transform, fixedEndOfConnecting, mouseX - this.leftPos, mouseY - this.topPos);
        }
        transform.popPose();
        super.render(transform, mouseX, mouseY, partialTick);
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
            var relativeX = (int) (mouseX - leftPos);
            var relativeY = (int) (mouseY - topPos);
            for (var region : RELATIVE_REGIONS) {
                if (region.getFirst().contains(relativeX, relativeY)) {
                    onClicked(region.getSecond());
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void onClicked(ConnectionPoint clicked) {
        if (fixedEndOfConnecting != null) {
            if (clicked.isAtColor != fixedEndOfConnecting.isAtColor) {
                var oldOtherEnd = getOtherEnd(clicked);
                var colorIndex = getIndexAtColor(clicked, fixedEndOfConnecting);
                var grayIndex = clicked.isAtColor ? fixedEndOfConnecting.index : clicked.index;
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
        var colorToGray = menu.getColorToGray();
        for (int color = 0; color < colorToGray.length; ++color) {
            var mappedTo = colorToGray[color];
            if (mappedTo == RSRemapperBlockEntity.NOT_MAPPED) {
                continue;
            }
            var colorCP = new ConnectionPoint(true, color);
            var grayCP = new ConnectionPoint(false, mappedTo);
            WRAPPED_WIRE.blit(transform, WRAP_X_COLOR, colorCP.getSpriteY());
            WRAPPED_WIRE.blit(transform, WRAP_X_GRAY, grayCP.getSpriteY());
            renderFullyConnectedWire(
                    transform, colorCP.getWireX(), colorCP.getWireY(), grayCP.getWireX(), grayCP.getWireY()
            );
        }
    }

    private void renderFullyConnectedWire(PoseStack transform, float xStart, float yStart, float xEnd, float yEnd) {
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
                xStart, yStart - halfWireHeight
        );
    }

    private void renderWireAtMouse(PoseStack transform, ConnectionPoint fixed, int mouseX, int mouseY) {
        var fixedY = fixed.getWireY();
        var fixedX = fixed.getWireX();
        Vec2 radius = new Vec2(fixedY - mouseY, mouseX - fixedX).normalized();

        renderWire(
                transform,
                fixedX + radius.x, fixedY + radius.y,
                mouseX + radius.x, mouseY + radius.y,
                mouseX - radius.x, mouseY - radius.y,
                fixedX - radius.x, fixedY - radius.y
        );
    }

    private void renderWire(
            PoseStack transform, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4
    ) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var matrix = transform.last().pose();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(matrix, x1, y1, 0.0F).color(WIRE_COLOR).endVertex();
        bufferbuilder.vertex(matrix, x2, y2, 0.0F).color(WIRE_COLOR).endVertex();
        bufferbuilder.vertex(matrix, x3, y3, 0.0F).color(WIRE_COLOR).endVertex();
        bufferbuilder.vertex(matrix, x4, y4, 0.0F).color(WIRE_COLOR).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    @Nonnull
    @Override
    public RSRemapperMenu getMenu() {
        return menu;
    }

    @Nullable
    private ConnectionPoint getOtherEnd(ConnectionPoint first) {
        int otherIndex;
        int invalid;
        var mapping = menu.getColorToGray();
        if (first.isAtColor) {
            otherIndex = mapping[first.index];
            invalid = RSRemapperBlockEntity.NOT_MAPPED;
        } else {
            otherIndex = ArrayUtils.indexOf(mapping, first.index);
            invalid = ArrayUtils.INDEX_NOT_FOUND;
        }
        if (otherIndex != invalid) {
            return new ConnectionPoint(!first.isAtColor, otherIndex);
        } else {
            return null;
        }
    }

    private int getIndexAtColor(ConnectionPoint first, ConnectionPoint second) {
        return first.isAtColor ? first.index : second.index;
    }

    private record ConnectionPoint(boolean isAtColor, int index) {
        float getWireY() {
            return getSpriteY() + WRAPPED_WIRE.getHeight() / 2f;
        }

        float getWireX() {
            if (isAtColor) {
                return WRAP_X_COLOR + WRAPPED_WIRE.getWidth();
            } else {
                return WRAP_X_GRAY;
            }
        }

        int getSpriteY() {
            return FIRST_WRAP_Y + index * COLOR_HEIGHT;
        }
    }
}
