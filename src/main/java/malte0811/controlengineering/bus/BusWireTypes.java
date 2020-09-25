package malte0811.controlengineering.bus;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.WireType;
import com.google.common.base.Preconditions;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.items.CEItems;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

//TODO config for a lot of this
public class BusWireTypes {
    public static final int MAX_BUS_WIDTH = 4;
    public static final int MIN_BUS_WIDTH = 1;
    private static final WireType[] WIRE_TYPES = new WireType[MAX_BUS_WIDTH + 1 - MIN_BUS_WIDTH];
    static {
        for (int w = MIN_BUS_WIDTH; w <= MAX_BUS_WIDTH; ++w) {
            WIRE_TYPES[w - MIN_BUS_WIDTH] = new BusWireType(w);
        }
    }

    public static WireType getBusWireType(int width) {
        Preconditions.checkArgument(
                width >= BusWireTypes.MIN_BUS_WIDTH && width <= BusWireTypes.MAX_BUS_WIDTH,
                "Unexpected bus width %s",
                width
        );
        return WIRE_TYPES[width - BusWireTypes.MIN_BUS_WIDTH];
    }

    public static class BusWireType extends WireType {
        private final int width;

        private BusWireType(int width) {
            this.width = width;
        }

        @Override
        public String getUniqueName() {
            return ControlEngineering.MODID+":bus_wire_"+width;
        }

        @Override
        public int getColour(Connection connection) {
            return -1;
        }

        @Override
        public double getSlack() {
            return 1.005;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public TextureAtlasSprite getIcon(Connection connection) {
            return WireType.iconDefaultWire;
        }

        @Override
        public int getMaxLength() {
            return 16;
        }

        @Override
        public ItemStack getWireCoil(Connection connection) {
            return new ItemStack(CEItems.getBusCoil(width));
        }

        @Override
        public double getRenderDiameter() {
            return 1 / 16.;
        }

        @Nonnull
        @Override
        public String getCategory() {
            return "CONTROLENGINEERING_BUS_WIRE_"+width;
        }

        public int getWidth() {
            return width;
        }
    }
}
