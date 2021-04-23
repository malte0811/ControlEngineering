package malte0811.controlengineering.logic.circuit;

import malte0811.controlengineering.logic.cells.Leafcells;
import malte0811.controlengineering.logic.cells.SignalType;
import net.minecraft.nbt.CompoundNBT;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static malte0811.controlengineering.logic.cells.Leafcells.RS_LATCH;

public class CircuitTest {
    private static final NetReference IN_A = new NetReference("inA");
    private static final NetReference IN_B = new NetReference("inB");
    private static final NetReference INTERNAL = new NetReference("internal");
    private static final NetReference OUT_A = new NetReference("outA");
    private static final NetReference OUT_B = new NetReference("outB");

    @BeforeClass
    public static void setup() {
        Leafcells.init();
        // Load classes now to prevent weird test runtimes
        new CompoundNBT();
        CircuitBuilder.builder()
                .addInputNet(IN_A, SignalType.DIGITAL)
                .addCell(Leafcells.AND2)
                .input(0, IN_A)
                .input(1, IN_A)
                .buildCell()
                .build();
    }

    private void assertTrue(Circuit c, NetReference net) {
        Assert.assertEquals(1, c.getNetValue(net), 0);
    }

    private void assertFalse(Circuit c, NetReference net) {
        Assert.assertEquals(0, c.getNetValue(net), 0);
    }

    @Test
    public void basic() {
        Circuit c = CircuitBuilder.builder()
                .addInputNet(IN_A, SignalType.DIGITAL)
                .addInputNet(IN_B, SignalType.DIGITAL)
                .addCell(Leafcells.AND2)
                .input(0, IN_A)
                .input(1, IN_B)
                .output(0, OUT_A)
                .buildCell()
                .build();
        c.tick();
        assertFalse(c, OUT_A);
        c.updateInputValue(IN_A, 1);
        c.tick();
        assertFalse(c, OUT_A);
        c.updateInputValue(IN_B, 1);
        assertFalse(c, OUT_A);
        c.tick();
        assertTrue(c, OUT_A);
    }

    @Test
    public void stateful() {
        Circuit c = CircuitBuilder.builder()
                .addInputNet(IN_A, SignalType.DIGITAL)
                .addInputNet(IN_B, SignalType.DIGITAL)
                .addCell(RS_LATCH)
                .input(0, IN_A)
                .input(1, IN_B)
                .output(0, OUT_A)
                .output(1, OUT_B)
                .buildCell()
                .build();
        c.tick();
        assertFalse(c, OUT_A);
        assertTrue(c, OUT_B);
        c.updateInputValue(IN_B, 1);
        c.tick();
        assertFalse(c, OUT_A);
        c.updateInputValue(IN_B, 0);
        c.tick();
        assertTrue(c, OUT_A);
        assertFalse(c, OUT_B);

        CompoundNBT serialized = c.toNBT();
        c = Circuit.fromNBT(serialized);
        assertTrue(c, OUT_A);
        c.updateInputValue(IN_A, 1);
        assertTrue(c, OUT_A);
        c.tick();
        c.tick();
        assertFalse(c, OUT_A);
    }

    @Test
    public void delayedNet() {
        Circuit c = CircuitBuilder.builder()
                .addDelayedNet(OUT_A, SignalType.DIGITAL)
                .addCell(Leafcells.NOT)
                .input(0, OUT_A)
                .output(0, INTERNAL)
                .buildCell()
                .addCell(Leafcells.D_LATCH)
                .input(0, INTERNAL)
                .output(0, OUT_A)
                .buildCell()
                .build();
        c.tick();
        assertFalse(c, OUT_A);
        c.tick();
        assertTrue(c, OUT_A);
        c.tick();
        assertFalse(c, OUT_A);
    }
}