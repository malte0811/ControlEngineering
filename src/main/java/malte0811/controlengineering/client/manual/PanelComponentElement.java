package malte0811.controlengineering.client.manual;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.SpecialManualElement;
import blusunrize.lib.manual.gui.ManualScreen;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.gui.panel.ComponentSelector;
import malte0811.controlengineering.util.ScreenUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PanelComponentElement extends SpecialManualElement {
    private static final int ITEM_SIZE = 18;
    public static final String INGREDIENTS_KEY = ControlEngineering.MODID + ".gui.component_ingredients";
    private static final Component INGREDIENTS_LABEL = new TranslatableComponent(INGREDIENTS_KEY);

    private final PanelComponentType<?, ?> type;
    private List<List<ItemStack>> ingredients = List.of();

    public PanelComponentElement(PanelComponentType<?, ?> type) {
        this.type = type;
    }

    public PanelComponentElement(ResourceLocation name) {
        this(PanelComponents.REGISTRY.get(name));
    }

    public PanelComponentElement(JsonObject obj) {
        this(new ResourceLocation(obj.get("component").getAsString()));
    }

    @Override
    public int getPixelsTaken() {
        return getComponentDemoHeight() + getFont().lineHeight;
    }

    private int getComponentDemoHeight() {
        return Math.max(ingredients.size(), 3) * ITEM_SIZE;
    }

    @Override
    public void onOpened(ManualScreen gui, int x, int y, List<Button> buttons) {
    }

    @Override
    public void render(PoseStack transform, ManualScreen gui, int x, int y, int mouseX, int mouseY) {
        var manual = gui.getManual();
        transform.pushPose();
        transform.translate(x, y, 0);
        var font = getFont();
        var labelWidth = font.width(INGREDIENTS_LABEL);
        font.draw(transform, INGREDIENTS_LABEL, manual.pageWidth - labelWidth, 0, 0);
        transform.translate(0, font.lineHeight, 0);
        var shownIngredients = getShownIngredients();
        var tooltipStack = renderIngredients(
                transform, shownIngredients, manual.pageWidth, mouseX, mouseY - font.lineHeight
        );
        ComponentSelector.renderComponentInGui(transform, type, manual.pageWidth - ITEM_SIZE, getComponentDemoHeight());
        transform.popPose();
        if (!tooltipStack.isEmpty()) {
            gui.renderTooltip(transform, tooltipStack, mouseX, mouseY);
        }
    }

    private ItemStack renderIngredients(
            PoseStack transform, List<ItemStack> shownIngredients, int width, int mouseX, int mouseY
    ) {
        ItemStack highlighted = ItemStack.EMPTY;
        for (int i = 0; i < shownIngredients.size(); ++i) {
            var x = width - ITEM_SIZE;
            var y = i * ITEM_SIZE;
            var item = shownIngredients.get(i);
            ManualUtils.renderItemStack(transform, item, x, y, false);
            if (highlighted.isEmpty() && ScreenUtils.isInRect(x, y, ITEM_SIZE, ITEM_SIZE, mouseX, mouseY)) {
                highlighted = item;
            }
        }
        return highlighted;
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
    public void recalculateCraftingRecipes() {
        this.ingredients = type.getCost().stream()
                .map(IngredientWithSize::getMatchingStackList)
                .toList();
    }

    private List<ItemStack> getShownIngredients() {
        final long second = System.currentTimeMillis() / 1000;
        List<ItemStack> result = new ArrayList<>();
        for (var candidates : ingredients) {
            result.add(candidates.get((int) (second % candidates.size())));
        }
        return result;
    }

    private Font getFont() {
        return ManualHelper.getManual().fontRenderer();
    }
}
