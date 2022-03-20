package malte0811.controlengineering.client.manual;

import blusunrize.lib.manual.SpecialManualElement;
import blusunrize.lib.manual.gui.ManualScreen;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.logic.schematic.client.ClientSymbols;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbols;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class LeafcellElement<State> extends SpecialManualElement {
    private static final int SCALE = 5;

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
        return type.getYSize() * SCALE;
    }

    @Override
    public void onOpened(ManualScreen gui, int x, int y, List<Button> buttons) {
    }

    @Override
    public void render(PoseStack transform, ManualScreen gui, int x, int y, int mouseX, int mouseY) {
        var manual = gui.getManual();
        transform.pushPose();
        var offsetX = (manual.pageWidth - type.getXSize() * SCALE) / 2.;
        transform.translate(x + offsetX, y, 0);
        transform.scale(SCALE, SCALE, SCALE);
        ClientSymbols.render(type, transform, 0, 0, type.getInitialState());
        // TODO render costs?
        transform.popPose();
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
