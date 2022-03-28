package malte0811.controlengineering.client.manual;

import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.SpecialManualElement;
import blusunrize.lib.manual.gui.ManualScreen;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.items.IEItemRefs;
import malte0811.controlengineering.logic.schematic.client.ClientSymbols;
import malte0811.controlengineering.logic.schematic.symbol.CellSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbols;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import java.util.List;

public class LeafcellElement<State> extends SpecialManualElement {
    private static final int SCALE = 5;
    private static final int ITEM_SIZE = 18;

    private final SchematicSymbol<State> type;

    public LeafcellElement(SchematicSymbol<State> type) {
        this.type = type;
    }

    public static LeafcellElement<?> from(ResourceLocation name) {
        return new LeafcellElement<>(SchematicSymbols.REGISTRY.get(name));
    }

    public static LeafcellElement<?> from(JsonObject obj) {
        return from(new ResourceLocation(obj.get("cell").getAsString()));
    }

    @Override
    public int getPixelsTaken() {
        return type.getDefaultYSize(Minecraft.getInstance().level) * SCALE + 4;
    }

    @Override
    public void onOpened(ManualScreen gui, int x, int y, List<Button> buttons) {
    }

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
            renderCost(transform, manual.fontRenderer(), x + manual.pageWidth, y, cost.numTubes(), IEItemRefs.TUBE);
            renderCost(
                    transform, manual.fontRenderer(),
                    x + manual.pageWidth, y + ITEM_SIZE,
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
            int x, int y, double clickX, double clickY, double mx, double my, double lastX, double lastY,
            int mouseButton
    ) {
    }

    @Override
    public boolean listForSearch(String searchTag) {
        return false;
    }

    @Override
    public void recalculateCraftingRecipes() {}
}
