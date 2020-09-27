package malte0811.controlengineering.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeMod;

public class RaytraceUtils {
    public static RayTraceContext create(PlayerEntity e, float partialTicks) {
        double rayTraceDistance = e.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
        Vector3d startVec = e.getEyePosition(partialTicks);
        Vector3d lookDirection = e.getLook(partialTicks);
        Vector3d endVec = startVec.add(
                lookDirection.x * rayTraceDistance,
                lookDirection.y * rayTraceDistance,
                lookDirection.z * rayTraceDistance
        );
        return new RayTraceContext(startVec, endVec, BlockMode.VISUAL, FluidMode.NONE, null);
    }
}
