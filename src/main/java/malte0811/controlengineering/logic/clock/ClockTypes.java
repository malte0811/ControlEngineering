package malte0811.controlengineering.logic.clock;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.util.typereg.TypedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;

import java.util.Map;

public class ClockTypes {
    public static final ClockGenerator<Unit> ALWAYS_ON = new FreeClock();
    public static final ClockGenerator<Boolean> RISING_EDGE = new EdgeClock();
    public static final ClockGenerator<Unit> WHILE_RS_ON = new StateClock();
    public static final ClockGenerator<Unit> NEVER = new NoneClock();

    public static final TypedRegistry<ClockGenerator<?>> REGISTRY = new TypedRegistry<>();

    public static <T extends ClockGenerator<?>> T register(ResourceLocation name, T generator) {
        return REGISTRY.register(name, generator);
    }

    public static Map<ResourceLocation, ClockGenerator<?>> getGenerators() {
        return REGISTRY.getEntries();
    }

    static {
        register(new ResourceLocation(ControlEngineering.MODID, "clock_free"), ALWAYS_ON);
        register(new ResourceLocation(ControlEngineering.MODID, "clock_edge"), RISING_EDGE);
        register(new ResourceLocation(ControlEngineering.MODID, "clock_state"), WHILE_RS_ON);
        register(new ResourceLocation(ControlEngineering.MODID, "clock_none"), NEVER);
    }
}
