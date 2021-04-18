package malte0811.controlengineering.items;

import blusunrize.immersiveengineering.api.wires.IWireCoil;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.utils.WirecoilUtils;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusWireType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;

public class BusCoilItem extends Item implements IWireCoil {
    public BusCoilItem() {
        super(new Properties().group(ControlEngineering.ITEM_GROUP));
    }

    @Override
    public WireType getWireType(ItemStack itemStack) {
        return BusWireType.INSTANCE;
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        Vector3d hitVec = context.getHitVec();
        return WirecoilUtils.doCoilUse(
                this,
                context.getPlayer(), context.getWorld(), context.getPos(), context.getHand(), context.getFace(),
                (float) hitVec.x, (float) hitVec.y, (float) hitVec.z
        );
    }
}
