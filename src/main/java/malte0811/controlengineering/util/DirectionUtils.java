package malte0811.controlengineering.util;

import net.minecraft.util.Direction;

import java.util.Arrays;
import java.util.Comparator;

public class DirectionUtils {
    public static final Direction[] VALUES = Direction.values();

    public static final Direction[] BY_HORIZONTAL_INDEX = Arrays.stream(VALUES)
            .filter(direction -> direction.getAxis().isHorizontal())
            .sorted(Comparator.comparingInt(Direction::getHorizontalIndex))
            .toArray(Direction[]::new);
}
