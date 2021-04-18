package malte0811.controlengineering.bus;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.WireApi;
import blusunrize.immersiveengineering.api.wires.WireType;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.items.CEItems;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class BusWireType extends WireType {
    public static final int NUM_LINES = 4;
    public static final WireType INSTANCE = new BusWireType();

    public static void init() {
        WireApi.registerWireType(INSTANCE);
    }

    private BusWireType() {
    }

    @Override
    public String getUniqueName() {
        return ControlEngineering.MODID + ":bus_wire";
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
        return CEItems.BUS_WIRE_COIL.get().getDefaultInstance();
    }

    @Override
    public double getRenderDiameter() {
        return 1 / 16.;
    }

    @Nonnull
    @Override
    public String getCategory() {
        return "CONTROLENGINEERING_BUS_WIRE";
    }
}
