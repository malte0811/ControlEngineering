package malte0811.controlengineering.items;

import blusunrize.immersiveengineering.api.wires.IWireCoil;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.utils.WirecoilUtils;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusWireType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nonnull;

public class BusCoilItem extends Item implements IWireCoil {
    public BusCoilItem() {
        super(new Properties().tab(ControlEngineering.ITEM_GROUP));
    }

    @Override
    public WireType getWireType(ItemStack itemStack) {
        return BusWireType.INSTANCE;
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Vec3 hitVec = context.getClickLocation();
        return WirecoilUtils.doCoilUse(
                this,
                context.getPlayer(), context.getLevel(), context.getClickedPos(), context.getHand(), context.getClickedFace(),
                (float) hitVec.x, (float) hitVec.y, (float) hitVec.z
        );
    }
}
