package malte0811.controlengineering.blocks.panels;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

import javax.annotation.Nonnull;
import java.util.Locale;

public enum PanelOrientation implements StringRepresentable {
    //Placed on the ceiling
    DOWN_NORTH(Direction.DOWN, Direction.NORTH),
    DOWN_EAST(Direction.DOWN, Direction.EAST),
    DOWN_SOUTH(Direction.DOWN, Direction.SOUTH),
    DOWN_WEST(Direction.DOWN, Direction.WEST),
    //Placed on the floor
    UP_NORTH(Direction.UP, Direction.NORTH),
    UP_EAST(Direction.UP, Direction.EAST),
    UP_SOUTH(Direction.UP, Direction.SOUTH),
    UP_WEST(Direction.UP, Direction.WEST),
    //Placed on the wall
    NORTH(Direction.NORTH, Direction.SOUTH),
    EAST(Direction.EAST, NORTH.front),
    SOUTH(Direction.SOUTH, NORTH.front),
    WEST(Direction.WEST, NORTH.front),
    ;

    public static final Property<PanelOrientation> PROPERTY = EnumProperty.create("orientation", PanelOrientation.class);
    public static final Direction HORIZONTAL_FRONT = NORTH.front;

    public final Direction top;
    public final Direction front;

    PanelOrientation(Direction top, Direction front) {
        this.top = top;
        this.front = front;
    }

    @Override
    public String toString() {
        return getSerializedName();
    }

    @Nonnull
    @Override
    public String getSerializedName() {
        return (top + "_" + front).toLowerCase(Locale.US);
    }

    public static PanelOrientation get(Direction top, Direction front) {
        for (PanelOrientation po : PanelOrientation.values()) {
            if (po.top == top && po.front == front) {
                return po;
            }
        }
        throw new RuntimeException("No panel orientation for front=" + front + ", top=" + top);
    }
}
