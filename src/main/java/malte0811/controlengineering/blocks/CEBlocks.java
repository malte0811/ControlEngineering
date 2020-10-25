package malte0811.controlengineering.blocks;

import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.tiles.bus.BusInterfaceTile;
import malte0811.controlengineering.tiles.bus.BusRelayTile;
import malte0811.controlengineering.tiles.bus.LineAccessTile;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class CEBlocks {
    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(
            ForgeRegistries.BLOCKS,
            ControlEngineering.MODID
    );

    public static final RegistryObject<BasicTileBlock<BusInterfaceTile>> BUS_INTERFACE = REGISTER.register(
            "bus_interface",
            createConnector(() -> CETileEntities.BUS_INTERFACE.get())
    );

    public static final RegistryObject<BasicTileBlock<BusRelayTile>> BUS_RELAY = REGISTER.register(
            "bus_relay",
            createConnector(() -> CETileEntities.BUS_RELAY.get())
    );

    public static final RegistryObject<BasicTileBlock<LineAccessTile>> LINE_ACCESS = REGISTER.register(
            "line_access",
            createConnector(() -> CETileEntities.LINE_ACCESS.get())
    );

    public static final RegistryObject<PanelBlock> CONTROL_PANEL = REGISTER.register("control_panel", PanelBlock::new);

    private static <T extends TileEntity & IImmersiveConnectable>
    Supplier<BasicTileBlock<T>> createConnector(Supplier<TileEntityType<T>> tile) {
        return () -> new BasicTileBlock<>(
                AbstractBlock.Properties.create(Material.IRON),
                tile
        );
    }
}
