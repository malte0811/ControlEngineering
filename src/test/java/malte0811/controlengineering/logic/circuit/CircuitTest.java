package malte0811.controlengineering.logic.circuit;

import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.logic.cells.Leafcells;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.nbt.CompoundTag;
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
        // Load classes now to prevent weird test runtimes
        new CompoundTag();
        CircuitBuilder.builder()
                .addInputNet(IN_A, SignalType.DIGITAL)
                .addCell(Leafcells.AND2.newInstance(), Vec2i.ZERO)
                .input("in1", IN_A)
                .input("in2", IN_A)
                .buildCell()
                .build();
    }

    private void assertTrue(Circuit c, NetReference net) {
        Assert.assertEquals(BusLine.MAX_VALID_VALUE, c.getNetValue(net));
    }

    private void assertFalse(Circuit c, NetReference net) {
        Assert.assertEquals(BusLine.MIN_VALID_VALUE, c.getNetValue(net));
    }

    @Test
    public void basic() {
        Circuit c = CircuitBuilder.builder()
                .addInputNet(IN_A, SignalType.DIGITAL)
                .addInputNet(IN_B, SignalType.DIGITAL)
                .addCell(Leafcells.AND2.newInstance(), Vec2i.ZERO)
                .input("in1", IN_A)
                .input("in2", IN_B)
                .output("out", OUT_A)
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
                .addCell(RS_LATCH.newInstance(), Vec2i.ZERO)
                .input("reset", IN_A)
                .input("set", IN_B)
                .output("q", OUT_A)
                .output("not_q", OUT_B)
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

        var serialized = Circuit.CODEC.toNBT(c);
        c = Circuit.CODEC.fromNBT(serialized);
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
                .addCell(Leafcells.NOT.newInstance(), Vec2i.ZERO)
                .input("in", OUT_A)
                .output("out", INTERNAL)
                .buildCell()
                .addCell(Leafcells.D_LATCH.newInstance(), Vec2i.ZERO)
                .input("in", INTERNAL)
                .output("out", OUT_A)
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