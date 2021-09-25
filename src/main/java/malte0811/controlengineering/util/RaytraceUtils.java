package malte0811.controlengineering.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

public class RaytraceUtils {
    public static ClipContext create(Player e, float partialTicks) {
        return create(e, partialTicks, Vec3.ZERO);
    }

    public static ClipContext create(Player e, float partialTicks, Vec3 offset) {
        double rayTraceDistance = e.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
        Vec3 startVec = e.getEyePosition(partialTicks).subtract(offset);
        Vec3 lookDirection = e.getViewVector(partialTicks);
        Vec3 endVec = startVec.add(
                lookDirection.x * rayTraceDistance,
                lookDirection.y * rayTraceDistance,
                lookDirection.z * rayTraceDistance
        );
        return new ClipContext(startVec, endVec, Block.VISUAL, Fluid.NONE, null);
    }
}
