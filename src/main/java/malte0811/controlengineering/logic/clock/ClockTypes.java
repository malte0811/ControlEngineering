package malte0811.controlengineering.logic.clock;

import malte0811.controlengineering.ControlEngineering;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;

public class ClockTypes {
    public static final ClockGenerator<Unit> ALWAYS_ON = new FreeClock();
    public static final ClockGenerator<Boolean> RISING_EDGE = new EdgeClock();
    public static final ClockGenerator<Unit> WHILE_RS_ON = new StateClock();
    public static final ClockGenerator<Unit> NEVER = new NoneClock();

    public static void init() {
        ClockGenerator.register(new ResourceLocation(ControlEngineering.MODID, "free"), ALWAYS_ON);
        ClockGenerator.register(new ResourceLocation(ControlEngineering.MODID, "edge"), RISING_EDGE);
        ClockGenerator.register(new ResourceLocation(ControlEngineering.MODID, "state"), WHILE_RS_ON);
        ClockGenerator.register(new ResourceLocation(ControlEngineering.MODID, "none"), NEVER);
    }
}
