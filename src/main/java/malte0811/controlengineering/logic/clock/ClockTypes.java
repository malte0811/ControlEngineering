package malte0811.controlengineering.logic.clock;

import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.util.RLUtils;
import malte0811.controlengineering.util.typereg.TypedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Map;

public class ClockTypes {
    public static final String ITEM_KEY_PREFIX = "clock_";
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

    public static Item getItem(ClockGenerator<?> clock) {
        return CEItems.CLOCK_GENERATORS.get(clock.getRegistryName()).get();
    }

    @Nullable
    public static ClockGenerator<?> getClock(Item item) {
        final var itemName = BuiltInRegistries.ITEM.getKey(item);
        if (!itemName.getPath().startsWith(ITEM_KEY_PREFIX)) { return null; }
        final var clockPath = itemName.getPath().substring(ITEM_KEY_PREFIX.length());
        final var clockName = new ResourceLocation(itemName.getNamespace(), clockPath);
        return REGISTRY.get(clockName);
    }

    static {
        register(RLUtils.ceLoc("free"), ALWAYS_ON);
        register(RLUtils.ceLoc("edge"), RISING_EDGE);
        register(RLUtils.ceLoc("state"), WHILE_RS_ON);
        register(RLUtils.ceLoc("none"), NEVER);
    }
}
