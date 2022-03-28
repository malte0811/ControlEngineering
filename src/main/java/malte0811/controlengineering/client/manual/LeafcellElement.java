package malte0811.controlengineering.client.manual;

import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.SpecialManualElement;
import blusunrize.lib.manual.gui.ManualScreen;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.items.IEItemRefs;
import malte0811.controlengineering.logic.schematic.client.ClientSymbols;
import malte0811.controlengineering.logic.schematic.symbol.CellSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbols;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import java.util.List;

public class LeafcellElement<State> extends SpecialManualElement {
    public static final String COST_KEY = ControlEngineering.MODID + ".gui.leafcell.cost";
    private static final int SCALE = 5;
    private static final int ITEM_SIZE = 18;

    private final SchematicSymbol<State> type;
    private final ManualInstance manual;

    public LeafcellElement(SchematicSymbol<State> type, ManualInstance manual) {
        this.type = type;
        this.manual = manual;
    }

    public static LeafcellElement<?> from(ResourceLocation name, ManualInstance manual) {
        return new LeafcellElement<>(SchematicSymbols.REGISTRY.get(name), manual);
    }

    public static LeafcellElement<?> from(JsonObject obj, ManualInstance manual) {
        return from(new ResourceLocation(obj.get("cell").getAsString()), manual);
    }

    @Override
    public int getPixelsTaken() {
        return Math.max(
                type.getDefaultYSize(Minecraft.getInstance().level) * SCALE,
                manual.fontRenderer().lineHeight + 2 * ITEM_SIZE
        ) + 4;
    }

    @Override
    public void onOpened(ManualScreen gui, int x, int y, List<Button> buttons) {}

    @Override
    public void render(PoseStack transform, ManualScreen gui, int x, int y, int mouseX, int mouseY) {
        var manual = gui.getManual();
        transform.pushPose();
        var offsetX = (manual.pageWidth * (2 / 3.) - type.getDefaultXSize(Minecraft.getInstance().level) * SCALE) / 2.;
        transform.translate(x + offsetX, y, 0);
        transform.scale(SCALE, SCALE, SCALE);
        ClientSymbols.render(type, transform, 0, 0, type.getInitialState());
        transform.popPose();
        if (type instanceof CellSymbol cell) {
            var cost = cell.getCellType().getCost();
            var font = manual.fontRenderer();
            final var costStartY = y + font.lineHeight;
            final var xRight = x + manual.pageWidth;
            var costLabel = I18n.get(COST_KEY);
            font.draw(transform, costLabel, xRight - font.width(costLabel) - 2, y, 0);
            renderCost(transform, font, xRight, costStartY, cost.numTubes(), IEItemRefs.TUBE);
            renderCost(
                    transform, font,
                    xRight, costStartY + ITEM_SIZE,
                    cost.wireLength(), IEItemRefs.WIRE
            );
        }
    }

    private void renderCost(PoseStack transform, Font font, int xRight, int y, double amount, ItemLike item) {
        var text = amount + " x ";
        var textWidth = font.width(text);
        var xLeft = xRight - ITEM_SIZE - textWidth;
        ManualUtils.renderItemStack(transform, item.asItem().getDefaultInstance(), xRight - ITEM_SIZE, y, false);
        font.draw(transform, text, xLeft, y + (ITEM_SIZE - font.lineHeight) / 2f, 0);
    }

    @Override
    public void mouseDragged(
            int x, int y,
            double clickX, double clickY,
            double mx, double my,
            double lastX, double lastY,
            int mouseButton
    ) {}

    @Override
    public boolean listForSearch(String searchTag) {
        return false;
    }

    @Override
    public void recalculateCraftingRecipes() {}
}
