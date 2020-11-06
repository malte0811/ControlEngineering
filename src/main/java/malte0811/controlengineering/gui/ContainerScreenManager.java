package malte0811.controlengineering.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerScreenManager {
    // IDEA considers the type arguments to be redundant, but the compiler disagrees, and that's the thing that
    // actually *needs* to like my code, so it wins
    @SuppressWarnings("RedundantTypeArguments")
    public static void registerScreens() {
        ScreenManager.<TeletypeContainer, TeletypeScreen>registerFactory(
                CEContainers.TELETYPE.get(),
                (container, inv, title) -> new TeletypeScreen(container, title)
        );
    }

    public static IWorldPosCallable readWorldPos(PacketBuffer buffer) {
        if (buffer == null) {
            return IWorldPosCallable.DUMMY;
        }
        BlockPos pos = buffer.readBlockPos();
        World world = Minecraft.getInstance().world;
        if (world == null) {
            return IWorldPosCallable.DUMMY;
        }
        return IWorldPosCallable.of(world, pos);
    }
}
