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
            result.append(PanelComponents.getCreationKey(comp.getComponent().getType()))
                    .append(' ')
                    .append(FORMAT.format(comp.getPosMin().x))
                    .append(' ')
                    .append(FORMAT.format(comp.getPosMin().y));
            for (String field : comp.getComponent().toCNCStrings()) {
                result.append(' ').append(escape(field));
            }
        }
        return result.toString();
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
