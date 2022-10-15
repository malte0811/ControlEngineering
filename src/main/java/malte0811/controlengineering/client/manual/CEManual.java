package malte0811.controlengineering.client.manual;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualEntry.SpecialElementData;
import blusunrize.lib.manual.ManualInstance;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.cnc.CNCInstructionGenerator;
import malte0811.controlengineering.util.RLUtils;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodecBase;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;

import java.util.List;
import java.util.Map;

import static malte0811.controlengineering.ControlEngineering.MODID;

public class CEManual {
    public static final String NAME_KEY = makeKey("name");
    public static final String OPTIONS_KEY = makeKey("options");
    public static final String EXAMPLE_KEY = makeKey("example");
    public static final Map<MyCodec<?>, String> CODEC_NAMES = ImmutableMap.<MyCodec<?>, String>builder()
            .put(MyCodecs.INTEGER, makeKey("integer"))
            .put(MyCodecs.HEX_COLOR, makeKey("color"))
            .put(MyCodecs.STRING, makeKey("text"))
            .build();

    public static void initManual() {
        var ieManual = ManualHelper.getManual();
        ieManual.registerSpecialElement(RLUtils.ceLoc("panel_component"), PanelComponentElement::new);
        ieManual.registerSpecialElement(
                RLUtils.ceLoc("leafcell"), json -> LeafcellElement.from(json, ieManual)
        );
        ieManual.registerSpecialElement(
                RLUtils.ceLoc("leafcell_truth"), json -> LeafcellWithStatesElement.from(ieManual, json)
        );
        addComponentFormatEntry(ieManual);
    }

    private static void addComponentFormatEntry(ManualInstance ieManual) {
        var ceCategory = ieManual.getRoot().getOrCreateSubnode(RLUtils.ceLoc("main"), 100);
        var panelCategory = ceCategory.getOrCreateSubnode(RLUtils.ceLoc("panels"));
        ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
        builder.readFromFile(RLUtils.ceLoc("panels/panel_format"));
        builder.appendText(CEManual::makeComponentFormats);
        ieManual.addEntry(panelCategory, builder.create(), 1000);
    }

    private static Pair<String, List<SpecialElementData>> makeComponentFormats() {
        StringBuilder entry = new StringBuilder();
        for (var component : PanelComponents.REGISTRY.getValues()) {
            entry.append("<np>");
            appendBoldTranslated(entry, component.getTranslationKey()).append("\n");
            appendBoldTranslated(entry, NAME_KEY).append(": ")
                    .append(PanelComponents.getCreationKey(component)).append('\n');
            appendBoldTranslated(entry, OPTIONS_KEY).append(":\n");
            describeCodec(component.getConfigCodec(), entry, "");
            entry.append('\n');
            var exampleComponent = new PlacedComponent(component.newInstance(), new Vec2d(3, 4));
            appendBoldTranslated(entry, EXAMPLE_KEY)
                    .append(": ");
            CNCInstructionGenerator.toInstructions(entry, exampleComponent).append('\n');
        }
        return Pair.of(entry.toString(), List.of());
    }

    private static void describeCodec(RecordCodecBase<?> codec, StringBuilder entry, String prefix) {
        for (var field : codec.getFields()) {
            entry.append(prefix);
            if (field.codec() instanceof RecordCodecBase<?> innerRecordCodec) {
                entry.append(field.name()).append(":\n");
                // Use NBSP to stop the text splitter from removing it
                describeCodec(innerRecordCodec, entry, prefix + '\u00A0');
            } else {
                var codecName = CODEC_NAMES.get(field.codec());
                Preconditions.checkNotNull(codecName, "Unknown codec " + field.codec() + " for field " + field.name());
                entry.append(field.name()).append(": ").append(I18n.get(codecName)).append('\n');
            }
        }
    }

    private static StringBuilder appendBoldTranslated(StringBuilder in, String key) {
        return in.append(ChatFormatting.BOLD)
                .append(I18n.get(key))
                .append(ChatFormatting.RESET);
    }

    private static String makeKey(String name) {
        return MODID + ".manual.component." + name;
    }
}
