package malte0811.controlengineering.client.manual;

import blusunrize.lib.manual.ManualElementTable;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.SpecialManualElement;
import blusunrize.lib.manual.gui.ManualScreen;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbols;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class LeafcellWithStatesElement extends SpecialManualElement {
    private final ManualInstance manual;
    private final SchematicSymbol<?> symbol;
    private final LeafcellElement<?> display;
    private final ManualElementTable truthTable;

    private LeafcellWithStatesElement(ManualInstance manual, ResourceLocation cellName) {
        this.manual = manual;
        this.symbol = SchematicSymbols.REGISTRY.get(cellName);
        this.display = new LeafcellElement<>(symbol, manual);

        var cell = Objects.requireNonNull(LeafcellType.REGISTRY.get(cellName));
        List<List<Component>> table = new ArrayList<>();
        List<String> inputNames = cell.getInputPins().keySet().stream().sorted().toList();
        List<String> outputNames = cell.getOutputPins().keySet().stream().sorted().toList();
        {
            List<Component> namesRow = new ArrayList<>();
            for (var pinList : List.of(inputNames, outputNames)) {
                for (var pin : pinList) {
                    namesRow.add(new TextComponent(pin.toUpperCase(Locale.ROOT)));
                }
            }
            table.add(namesRow);
        }
        Object2DoubleMap<String> inputs = new Object2DoubleOpenHashMap<>();
        for (int packedInputs = 0; packedInputs < 1 << inputNames.size(); ++packedInputs) {
            List<Component> line = new ArrayList<>();
            for (int i = 0; i < inputNames.size(); ++i) {
                var value = (packedInputs >> i) & 1;
                inputs.put(inputNames.get(i), value);
                line.add(new TextComponent(Integer.toString(value)));
            }
            var outputs = cell.getOutputSignals(inputs, null);
            for (var output : outputNames) {
                line.add(new TextComponent(Integer.toString((int) outputs.getDouble(output))));
            }
            table.add(line);
        }
        this.truthTable = new ManualElementTable(
                manual, table.stream().map(l -> l.toArray(Component[]::new)).toArray(Component[][]::new), false
        );
    }

    public static LeafcellWithStatesElement from(ManualInstance manual, JsonObject obj) {
        return new LeafcellWithStatesElement(manual, new ResourceLocation(obj.get("cell").getAsString()));
    }

    @Override
    public int getPixelsTaken() {
        return getPixelsBeforeTable() + truthTable.getPixelsTaken();
    }

    private int getPixelsBeforeTable() {
        return display.getPixelsTaken() + manual.fontRenderer().lineHeight;
    }

    @Override
    public void onOpened(ManualScreen gui, int x, int y, List<Button> buttons) {}

    @Override
    public void render(PoseStack transform, ManualScreen gui, int x, int y, int mouseX, int mouseY) {
        display.render(transform, gui, x, y, mouseX, mouseY);
        var nameWidth = manual.fontRenderer().width(symbol.getName());
        manual.fontRenderer().draw(
                transform, symbol.getName(),
                x + (manual.pageWidth - nameWidth) / 2f, y + display.getPixelsTaken() - 4, 0
        );
        truthTable.render(transform, gui, x, y + getPixelsBeforeTable(), mouseX, mouseY);
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
