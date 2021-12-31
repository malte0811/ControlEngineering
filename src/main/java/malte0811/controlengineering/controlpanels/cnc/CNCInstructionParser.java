package malte0811.controlengineering.controlpanels.cnc;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import malte0811.controlengineering.controlpanels.PanelComponentInstance;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.util.FastDataResult;
import malte0811.controlengineering.util.math.Vec2d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CNCInstructionParser {
    public static final char ESCAPE = '\\';
    public static final char COMPONENT_SEPARATOR = ';';
    public static final char QUOTATION_MARK = '"';

    public static ParserResult parse(String input) {
        int pos = 0;
        String error = null;
        List<PlacedComponent> components = new ArrayList<>();
        IntList componentEnds = new IntArrayList();
        while (pos < input.length()) {
            final int componentStart = pos;
            String componentString;
            {
                Pair<String, Integer> next = nextComponent(input, pos);
                componentString = next.getFirst();
                pos = next.getSecond();
            }
            FastDataResult<List<String>> tokens = tokenize(componentString);
            if (tokens.isError()) {
                error = tokens.getErrorMessage();
                break;
            }
            if (tokens.get().size() < 3) {
                error = "Too few tokens in component starting at " + componentStart;
                break;
            }
            FastDataResult<PlacedComponent> nextRes = parseComponent(tokens.get());
            if (nextRes.isError()) {
                error = "Component starting at " + componentStart + ": " + nextRes.getErrorMessage();
                break;
            }
            PlacedComponent next = nextRes.get();
            for (PlacedComponent existing : components) {
                if (!existing.disjoint(next)) {
                    error = "Component starting at " + componentStart + " intersects previous component";
                }
            }
            if (error != null) {
                break;
            }
            components.add(next);
            componentEnds.add(pos);
        }
        if (error != null) {
            return ParserResult.failure(components, componentEnds, error);
        } else {
            return ParserResult.success(components, componentEnds);
        }
    }

    private static FastDataResult<PlacedComponent> parseComponent(List<String> tokens) {
        String typeName = tokens.get(0);
        PanelComponentType<?, ?> type = PanelComponents.getType(typeName);
        if (type == null) {
            return FastDataResult.error("Unknown type: " + typeName);
        }
        double x;
        double y;
        try {
            x = Double.parseDouble(tokens.get(1));
            y = Double.parseDouble(tokens.get(2));
        } catch (NumberFormatException xcp) {
            return FastDataResult.error("Invalid position");
        }
        FastDataResult<? extends PanelComponentInstance<?, ?>> component = type.newInstance(tokens.subList(
                3, tokens.size()
        ));
        if (component.isError()) {
            return component.propagateError();
        }
        PlacedComponent placed = new PlacedComponent(component.get(), new Vec2d(x, y));
        if (!placed.isWithinPanel()) {
            return FastDataResult.error("Not within panel bounds");
        }
        return FastDataResult.success(placed);
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

    private static FastDataResult<List<String>> tokenize(String input) {
        int pos = 0;
        List<String> result = new ArrayList<>();
        while (pos < input.length()) {
            FastDataResult<Pair<String, Integer>> p = nextToken(input, pos);
            if (p.isError()) {
                return p.propagateError();
            }
            pos = p.get().getSecond();
            result.add(p.get().getFirst());
        }
        return FastDataResult.success(result);
    }

    private static FastDataResult<Pair<String, Integer>> nextToken(String input, final int start) {
        int currentPos = start;
        while (currentPos < input.length() && Character.isWhitespace(input.charAt(currentPos))) {
            ++currentPos;
        }
        StringBuilder result = new StringBuilder();
        final boolean untilQuotationMarks = input.charAt(currentPos) == QUOTATION_MARK;
        if (untilQuotationMarks) {
            ++currentPos;
        }
        boolean isEscaped = false;
        while (currentPos < input.length() && (isEscaped || !shouldStop(
                input.charAt(currentPos),
                untilQuotationMarks
        ))) {
            final char next = input.charAt(currentPos);
            if (isEscaped || next != ESCAPE) {
                result.append(next);
                isEscaped = false;
            } else {
                isEscaped = true;
            }
            ++currentPos;
        }
        //TODO localize?
        if (isEscaped) {
            return FastDataResult.error("Tape ends in escape sequence");
        }
        if (untilQuotationMarks && currentPos >= input.length()) {
            // Ended before proper token end
            return FastDataResult.error("Unterminated quoted parameter starting at " + start);
        }
        if (!untilQuotationMarks && currentPos < input.length() && !Character.isWhitespace(input.charAt(currentPos))) {
            result.append(input.charAt(currentPos));
        }
        ++currentPos;
        return FastDataResult.success(Pair.of(result.toString(), currentPos));
    }

    private static boolean shouldStop(char c, boolean untilQuot) {
        if (untilQuot) {
            return c == QUOTATION_MARK;
        } else {
            return Character.isWhitespace(c);
        }
    }

    public record ParserResult(
            ImmutableList<PlacedComponent> components, IntList componentEnds, @Nullable String error
    ) {

        private ParserResult(
                List<PlacedComponent> components,
                IntList componentEnds,
                @Nullable String error
        ) {
            this(ImmutableList.copyOf(components), IntLists.unmodifiable(componentEnds), error);
        }

        public static ParserResult success(List<PlacedComponent> components, IntList componentEnds) {
            return new ParserResult(components, componentEnds, null);
        }

        public static ParserResult failure(List<PlacedComponent> components, IntList componentEnds, String error) {
            Preconditions.checkNotNull(error);
            return new ParserResult(components, componentEnds, error);
        }

        public boolean isError() {
            return error != null;
        }
    }

}
