package malte0811.controlengineering.controlpanels.cnc;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.components.Button;
import malte0811.controlengineering.controlpanels.components.Indicator;
import malte0811.controlengineering.util.Vec2d;
import org.junit.Assert;
import org.junit.Test;

public class CNCInstructionParserTest {
    private static final Button BUTTON = new Button(0xffffff, false, new BusSignalRef(1, 2));
    private static final Indicator INDICATOR = new Indicator(0xff00, new BusSignalRef(2, 1), 0);

    private void assertSuccess(
            CNCInstructionParser.ParserResult result,
            PlacedComponent... expected
    ) {
        Assert.assertFalse(result.isError());
        Assert.assertEquals(ImmutableList.copyOf(expected), result.getComponents());
    }

    private void assertFailure(
            CNCInstructionParser.ParserResult result,
            PlacedComponent... expected
    ) {
        Assert.assertTrue(result.isError());
        Assert.assertEquals(ImmutableList.copyOf(expected), result.getComponents());
    }

    @Test
    public void testBasicPanel() {
        CNCInstructionParser.ParserResult result = CNCInstructionParser.parse(
                button(1, 2) + ";" + indicator(2, 3)
        );
        assertSuccess(
                result,
                new PlacedComponent(BUTTON, new Vec2d(1, 2)),
                new PlacedComponent(INDICATOR, new Vec2d(2, 3))
        );
    }

    @Test
    public void testInvalidComponent() {
        CNCInstructionParser.ParserResult result = CNCInstructionParser.parse("this is not a component");
        assertFailure(result);
    }

    @Test
    public void testMissingArgs() {
        CNCInstructionParser.ParserResult result = CNCInstructionParser.parse("button 1 2 ff");
        assertFailure(result);
    }

    @Test
    public void testNoPos() {
        CNCInstructionParser.ParserResult result = CNCInstructionParser.parse("button 1 ");
        assertFailure(result);
    }

    @Test
    public void testPosNumberFormatXCP() {
        CNCInstructionParser.ParserResult result = CNCInstructionParser.parse("button 1 foo");
        assertFailure(result);
    }

    @Test
    public void testDeserializerNumberFormatXCP() {
        CNCInstructionParser.ParserResult result = CNCInstructionParser.parse("button 1 2 foo");
        assertFailure(result);
    }

    @Test
    public void testOutOfPanel() {
        CNCInstructionParser.ParserResult result = CNCInstructionParser.parse(
                button(16.5, 1)
        );
        assertFailure(result);
    }

    @Test
    public void testDisjoint() {
        CNCInstructionParser.ParserResult result = CNCInstructionParser.parse(
                button(3, 1) + ";" + indicator(3, 1)
        );
        assertFailure(result, new PlacedComponent(BUTTON, new Vec2d(3, 1)));
    }

    private static String button(double x, double y) {
        return "button " + x + " " + y + " ffffff 1 2";
    }

    private static String indicator(double x, double y) {
        return "indicator " + x + " " + y + " ff00 2 1";
    }

}