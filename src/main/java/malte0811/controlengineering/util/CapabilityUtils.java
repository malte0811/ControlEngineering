package malte0811.controlengineering.util;

import net.minecraft.core.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityUtils extends blusunrize.immersiveengineering.api.utils.CapabilityUtils {
    public static boolean isNullOr(@Nonnull Direction correct, @Nullable Direction actual) {
        return actual == null || actual == correct;
    }
}
