package malte0811.controlengineering.logic.circuit;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Leafcells;
import malte0811.controlengineering.logic.cells.SignalType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.shapes.IBooleanFunction;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Objects;

import static malte0811.controlengineering.logic.cells.Leafcells.RS_LATCH;

public class CircuitTest {
    private static final NetReference IN_A = new NetReference("inA");
    private static final NetReference IN_B = new NetReference("inB");
    private static final NetReference INTERNAL = new NetReference("internal");
    private static final NetReference OUT_A = new NetReference("outA");
    private static final NetReference OUT_B = new NetReference("outB");
    private static LeafcellType<Unit> AND2;

    @BeforeClass
    public static void setup() {
        Leafcells.init();
        AND2 = Objects.requireNonNull(Leafcells.BASIC_LOGIC.get(Pair.of(IBooleanFunction.AND, 2)));
        // Load classes now to prevent weird test runtimes
        new CompoundNBT();
        CircuitBuilder.builder().addStage().buildStage().build();
    }

    @Test
    public void basic() {
        Circuit c = CircuitBuilder.builder()
                .addInputNet(IN_A, SignalType.DIGITAL)
                .addInputNet(IN_B, SignalType.DIGITAL)
                .addStage()
                .addCell(AND2.newInstance())
                .input(0, IN_A)
                .input(1, IN_B)
                .output(0, OUT_A)
                .buildCell()
                .buildStage()
                .build();
        c.tick();
        Assert.assertEquals(0, c.getNetValue(OUT_A), 0);
        c.updateInputValue(IN_A, 1);
        c.tick();
        Assert.assertEquals(0, c.getNetValue(OUT_A), 0);
        c.updateInputValue(IN_B, 1);
        Assert.assertEquals(0, c.getNetValue(OUT_A), 0);
        c.tick();
        Assert.assertEquals(1, c.getNetValue(OUT_A), 0);
    }

    @Test
    public void stateful() {
        Circuit c = CircuitBuilder.builder()
                .addInputNet(IN_A, SignalType.DIGITAL)
                .addInputNet(IN_B, SignalType.DIGITAL)
                .addStage()
                .addCell(RS_LATCH.newInstance())
                .input(0, IN_A)
                .input(1, IN_B)
                .output(0, OUT_A)
                .output(1, OUT_B)
                .buildCell()
                .buildStage()
                .build();
        c.tick();
        Assert.assertEquals(0, c.getNetValue(OUT_A), 0);
        Assert.assertEquals(1, c.getNetValue(OUT_B), 0);
        c.updateInputValue(IN_B, 1);
        c.tick();
        Assert.assertEquals(1, c.getNetValue(OUT_A), 0);
        c.updateInputValue(IN_B, 0);
        c.tick();
        Assert.assertEquals(1, c.getNetValue(OUT_A), 0);
        Assert.assertEquals(0, c.getNetValue(OUT_B), 0);

        CompoundNBT serialized = c.toNBT();
        c = new Circuit(serialized);
        Assert.assertEquals(1, c.getNetValue(OUT_A), 0);
        c.updateInputValue(IN_A, 1);
        Assert.assertEquals(1, c.getNetValue(OUT_A), 0);
        c.tick();
        Assert.assertEquals(0, c.getNetValue(OUT_A), 0);
    }
}