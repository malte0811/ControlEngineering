package malte0811.controlengineering.blocks;

import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.logic.LogicCabinetBlock;
import malte0811.controlengineering.blocks.logic.LogicWorkbenchBlock;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.blocks.panels.PanelCNCBlock;
import malte0811.controlengineering.blocks.panels.PanelDesignerBlock;
import malte0811.controlengineering.blocks.tape.TeletypeBlock;
import malte0811.controlengineering.tiles.CETileEntities;
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

    public static final RegistryObject<BusInterfaceBlock> BUS_INTERFACE = REGISTER.register(
            "bus_interface", BusInterfaceBlock::new
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

    public static final RegistryObject<PanelCNCBlock> PANEL_CNC = REGISTER.register("panel_cnc", PanelCNCBlock::new);

    public static final RegistryObject<PanelDesignerBlock> PANEL_DESIGNER = REGISTER.register(
            "panel_designer", PanelDesignerBlock::new
    );

    public static final RegistryObject<TeletypeBlock> TELETYPE = REGISTER.register("teletype", TeletypeBlock::new);

    public static final RegistryObject<LogicCabinetBlock> LOGIC_CABINET = REGISTER.register(
            "logic_cabinet",
            LogicCabinetBlock::new
    );

    public static final RegistryObject<LogicWorkbenchBlock> LOGIC_WORKBENCH = REGISTER.register(
            "logic_workbench", LogicWorkbenchBlock::new
    );

    private static <T extends TileEntity & IImmersiveConnectable>
    Supplier<BasicTileBlock<T>> createConnector(Supplier<TileEntityType<T>> tile) {
        return () -> new BasicTileBlock<>(
                AbstractBlock.Properties.create(Material.IRON),
                tile
        );
    }
}
