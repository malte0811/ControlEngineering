package malte0811.controlengineering.controlpanels.cnc;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import malte0811.controlengineering.controlpanels.components.config.ColorAndText;
import malte0811.controlengineering.crafting.noncrafting.ServerFontRecipe;
import malte0811.controlengineering.util.math.Vec2d;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.Locale;

@RunWith(Parameterized.class)
public class CNCInstructionGeneratorTest {

    @Parameterized.Parameters
    public static Iterable<Locale> getTestLocales() {
        return List.of(Locale.US, Locale.GERMAN);
    }

    public CNCInstructionGeneratorTest(Locale l) {
        Locale.setDefault(l);
    }

    @BeforeClass
    public static void preload() {
        ServerFontRecipe.IN_UNIT_TEST = true;
    }

    @Test
    public void test() {
        List<PlacedComponent> comps = ImmutableList.of(
                new PlacedComponent(
                        PanelComponents.BUTTON.newInstanceFromCfg(new ColorAndSignal(0xff0000, new BusSignalRef(0, 2))),
                        new Vec2d(1, 1)
                ),
                new PlacedComponent(
                        PanelComponents.BUTTON.newInstanceFromCfg(new ColorAndSignal(0xff00, new BusSignalRef(0, 3))),
                        new Vec2d(3, 1)
                ),
                new PlacedComponent(
                        PanelComponents.INDICATOR.newInstanceFromCfg(new ColorAndSignal(
                                0xffff00,
                                new BusSignalRef(0, 4)
                        )),
                        new Vec2d(2, 1.5)
                ),
                new PlacedComponent(
                        PanelComponents.LABEL.newInstanceFromCfg(new ColorAndText(0xff00, "a test\\?\"''")),
                        new Vec2d(1, 5)
                )
        );
        final String generated = CNCInstructionGenerator.toInstructions(comps);
        Assert.assertEquals(
                "button 1 1 ff0000 0 2;button 3 1 ff00 0 3;indicator 2 1.5 ffff00 0 4;label 1 5 ff00 \"a test\\\\?\\\"''\"",
                generated
        );
        CNCInstructionParser.ParserResult parsed = CNCInstructionParser.parse(null, generated);
        Assert.assertFalse(parsed.isError());
        Assert.assertEquals(comps, parsed.components());
    }
}