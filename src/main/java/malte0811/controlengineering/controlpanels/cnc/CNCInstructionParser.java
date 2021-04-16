package malte0811.controlengineering.controlpanels.cnc;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import malte0811.controlengineering.controlpanels.PanelComponentInstance;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.util.Vec2d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CNCInstructionParser {
    private static final char ESCAPE = '\\';
    private static final char COMPONENT_SEPARATOR = ';';
    private static final char QUOTATION_MARK = '"';

    public static ParserResult parse(String input) {
        int pos = 0;
        boolean error = false;
        List<PlacedComponent> components = new ArrayList<>();
        IntList componentEnds = new IntArrayList();
        while (pos < input.length()) {
            String componentString;
            {
                Pair<String, Integer> next = nextComponent(input, pos);
                componentString = next.getFirst();
                pos = next.getSecond();
            }
            List<String> tokens = tokenize(componentString);
            if (tokens == null || tokens.size() < 3) {
                error = true;
                break;
            }
            DataResult<PlacedComponent> nextRes = parseComponent(tokens);
            if (nextRes.result().isPresent()) {
                PlacedComponent next = nextRes.result().get();
                for (PlacedComponent existing : components) {
                    if (!existing.disjoint(next)) {
                        error = true;
                        break;
                    }
                }
                if (!error) {
                    components.add(next);
                    componentEnds.add(pos);
                }
            } else {
                error = true;
                break;
            }
        }
        if (error) {
            return ParserResult.failure(components, componentEnds, pos);
        } else {
            return ParserResult.success(components, componentEnds);
        }
    }

    private static DataResult<PlacedComponent> parseComponent(List<String> tokens) {
        String typeName = tokens.get(0);
        PanelComponentType<?, ?> type = PanelComponents.getType(typeName);
        if (type == null) {
            return DataResult.error("Unknown type: " + typeName);
        }
        double x;
        double y;
        try {
            x = Double.parseDouble(tokens.get(1));
            y = Double.parseDouble(tokens.get(2));
        } catch (NumberFormatException xcp) {
            return DataResult.error("Invalid position: " + xcp.getMessage());
        }
        DataResult<? extends PanelComponentInstance<?, ?>> component = type.newInstance(tokens.subList(
                3,
                tokens.size()
        ));
        if (component.error().isPresent()) {
            return DataResult.error(component.error().get().message());
        }
        Preconditions.checkState(component.result().isPresent());
        PlacedComponent placed = new PlacedComponent(component.result().get(), new Vec2d(x, y));
        if (!placed.isWithinPanel()) {
            return DataResult.error("Not within panel bounds");
        }
        return DataResult.success(placed);
    }

    private static Pair<String, Integer> nextComponent(String input, int start) {
        int nextSep = -1;
        boolean inQuotes = false;
        for (int i = start; nextSep < 0 && i < input.length(); ++i) {
            char next = input.charAt(i);
            switch (next) {
                case QUOTATION_MARK:
                    inQuotes = !inQuotes;
                    break;
                case ESCAPE:
                    ++i;
                    break;
                case COMPONENT_SEPARATOR:
                    if (!inQuotes) {
                        nextSep = i;
                    }
                    break;
            }
        }
        if (nextSep >= 0) {
            return Pair.of(input.substring(start, nextSep), nextSep + 1);
        } else {
            return Pair.of(input.substring(start), input.length());
        }
    }

    @Nullable
    // Null = error
    private static List<String> tokenize(String input) {
        int pos = 0;
        List<String> result = new ArrayList<>();
        while (pos < input.length()) {
            Pair<String, Integer> p = nextToken(input, pos);
            if (p == null) {
                return null;
            }
            pos = p.getSecond();
            result.add(p.getFirst());
        }
        return result;
    }

    @Nullable
    //Null = error
    private static Pair<String, Integer> nextToken(String input, int start) {
        while (start < input.length() && Character.isWhitespace(input.charAt(start))) {
            ++start;
        }
        StringBuilder result = new StringBuilder();
        final boolean untilQuotationMarks = input.charAt(start) == QUOTATION_MARK;
        boolean isEscaped = false;
        while (start < input.length() && !shouldStop(input.charAt(start), untilQuotationMarks)) {
            final char next = input.charAt(start);
            if (isEscaped || next != ESCAPE) {
                result.append(next);
            } else {
                isEscaped = true;
            }
            ++start;
        }
        if (isEscaped || (untilQuotationMarks && start >= input.length())) {
            // Ended before proper token end
            return null;
        }
        if (untilQuotationMarks) {
            result.append(input.charAt(start));
        }
        ++start;
        return Pair.of(result.toString(), start);
    }

    private static boolean shouldStop(char c, boolean untilQuot) {
        if (untilQuot) {
            return c == QUOTATION_MARK;
        } else {
            return Character.isWhitespace(c);
        }
    }

    public static class ParserResult {
        private final ImmutableList<PlacedComponent> components;
        private final IntList componentEnds;
        private final int errorAt;

        private ParserResult(
                List<PlacedComponent> components,
                IntList componentEnds,
                int errorAt
        ) {
            this.components = ImmutableList.copyOf(components);
            this.componentEnds = IntLists.unmodifiable(componentEnds);
            this.errorAt = errorAt;
        }

        public static ParserResult success(List<PlacedComponent> components, IntList componentEnds) {
            return new ParserResult(components, componentEnds, -1);
        }

        public static ParserResult failure(List<PlacedComponent> components, IntList componentEnds, int errorAt) {
            Preconditions.checkArgument(errorAt >= 0);
            return new ParserResult(components, componentEnds, errorAt);
        }

        public boolean isError() {
            return errorAt >= 0;
        }

        public int getErrorPosition() {
            Preconditions.checkState(isError());
            return errorAt;
        }

        public IntList getComponentEnds() {
            return componentEnds;
        }

        public ImmutableList<PlacedComponent> getComponents() {
            return components;
        }
    }
}
