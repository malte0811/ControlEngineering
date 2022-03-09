package malte0811.controlengineering.controlpanels.cnc;

import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.controlpanels.PlacedComponent;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.stream.Collectors;

public class CNCInstructionGenerator {
    private static final NumberFormat FORMAT = new DecimalFormat("#.##");

    public static String toInstructions(List<PlacedComponent> components) {
        StringBuilder result = new StringBuilder();
        for (PlacedComponent comp : components) {
            if (result.length() > 0) {
                result.append(CNCInstructionParser.COMPONENT_SEPARATOR);
            }
            toInstructions(result, comp);
        }
        return result.toString();
    }

    public static StringBuilder toInstructions(StringBuilder out, PlacedComponent component) {
        out.append(PanelComponents.getCreationKey(component.getComponent().getType()))
                .append(' ')
                .append(FORMAT.format(component.getPosMin().x()))
                .append(' ')
                .append(FORMAT.format(component.getPosMin().y()));
        for (String field : component.getComponent().toCNCStrings()) {
            out.append(' ').append(escape(field));
        }
        return out;
    }

    private static String escape(String raw) {
        String escaped = raw.chars().mapToObj(i -> {
            if (i == '\\') {
                return "\\\\";
            } else if (i == '"') {
                return "\\\"";
            } else {
                return "" + (char) i;
            }
        }).collect(Collectors.joining());
        if (escaped.chars().anyMatch(Character::isWhitespace)) {
            return "\"" + escaped + "\"";
        } else {
            return escaped;
        }
    }
}
