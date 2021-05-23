package malte0811.controlengineering.controlpanels.cnc;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.controlpanels.PanelComponentInstance;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.components.ColorAndSignal;
import malte0811.controlengineering.util.ServerFontWidth;
import malte0811.controlengineering.util.math.Vec2d;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CNCInstructionParserTest {
    private static final PanelComponentInstance<?, ?> BUTTON = PanelComponents.BUTTON.newInstance(
            new ColorAndSignal(0xffffff, new BusSignalRef(1, 2))
    );
    private static final PanelComponentInstance<?, ?> INDICATOR = PanelComponents.INDICATOR.newInstance(
            new ColorAndSignal(0xff00, new BusSignalRef(2, 1))
    );

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

    @BeforeClass
    public static void preload() {
        //Load all classes before the actual tests since some have slow static init
        CNCInstructionParser.parse(button(1, 2) + ";" + indicator(2, 3));
        ServerFontWidth.constantWidthForTesting(1);
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

    @Test
    public void testQuotedString() {
        CNCInstructionParser.ParserResult result = CNCInstructionParser.parse("label 1 1 \"This is a test\"");
        assertSuccess(
                result, new PlacedComponent(PanelComponents.LABEL.newInstance("This is a test"), new Vec2d(1, 1))
        );
    }

    @Test
    public void testUnquotedString() {
        CNCInstructionParser.ParserResult result = CNCInstructionParser.parse("label 1 1 test");
        assertSuccess(
                result, new PlacedComponent(PanelComponents.LABEL.newInstance("test"), new Vec2d(1, 1))
        );
    }

    private static String button(double x, double y) {
        return "button " + x + " " + y + " ffffff 1 2";
    }

    private static String indicator(double x, double y) {
        return "indicator " + x + " " + y + " ff00 2 1";
    }

}