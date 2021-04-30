package malte0811.controlengineering.tiles;

import com.google.common.collect.ImmutableSet;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.tiles.bus.BusInterfaceTile;
import malte0811.controlengineering.tiles.bus.BusRelayTile;
import malte0811.controlengineering.tiles.bus.LineAccessTile;
import malte0811.controlengineering.tiles.logic.LogicCabinetTile;
import malte0811.controlengineering.tiles.logic.LogicWorkbenchTile;
import malte0811.controlengineering.tiles.panels.ControlPanelTile;
import malte0811.controlengineering.tiles.panels.PanelCNCTile;
import malte0811.controlengineering.tiles.panels.PanelDesignerTile;
import malte0811.controlengineering.tiles.tape.TeletypeTile;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.function.Function;
import java.util.function.Supplier;

public class CETileEntities {
    public static final DeferredRegister<TileEntityType<?>> REGISTER = DeferredRegister.create(
            ForgeRegistries.TILE_ENTITIES, ControlEngineering.MODID
    );

    public static RegistryObject<TileEntityType<BusRelayTile>> BUS_RELAY = REGISTER.register(
            "bus_relay",
            createTileType(BusRelayTile::new, CEBlocks.BUS_RELAY)
    );

    public static RegistryObject<TileEntityType<BusInterfaceTile>> BUS_INTERFACE = REGISTER.register(
            "bus_interface",
            createTileType(BusInterfaceTile::new, CEBlocks.BUS_INTERFACE)
    );

    public static RegistryObject<TileEntityType<LineAccessTile>> LINE_ACCESS = REGISTER.register(
            "line_access",
            createTileType(LineAccessTile::new, CEBlocks.LINE_ACCESS)
    );

    public static RegistryObject<TileEntityType<ControlPanelTile>> CONTROL_PANEL = REGISTER.register(
            "control_panel",
            createTileType(ControlPanelTile::new, CEBlocks.CONTROL_PANEL)
    );

    public static RegistryObject<TileEntityType<PanelCNCTile>> PANEL_CNC = REGISTER.register(
            "panel_cnc",
            createTileType(PanelCNCTile::new, CEBlocks.PANEL_CNC)
    );

    public static RegistryObject<TileEntityType<PanelDesignerTile>> PANEL_DESIGNER = REGISTER.register(
            "panel_designer",
            createTileType(PanelDesignerTile::new, CEBlocks.PANEL_DESIGNER)
    );

    public static RegistryObject<TileEntityType<TeletypeTile>> TELETYPE = REGISTER.register(
            "teletype",
            createTileType(TeletypeTile::new, CEBlocks.TELETYPE)
    );

    public static RegistryObject<TileEntityType<LogicCabinetTile>> LOGIC_CABINET = REGISTER.register(
            "logic_cabinet",
            createTileType(LogicCabinetTile::new, CEBlocks.LOGIC_CABINET)
    );

    public static RegistryObject<TileEntityType<LogicWorkbenchTile>> LOGIC_WORKBENCH = REGISTER.register(
            "logic_workbench",
            createTileType(LogicWorkbenchTile::new, CEBlocks.LOGIC_WORKBENCH)
    );

    private static <T extends TileEntity> Supplier<TileEntityType<T>> createTileType(
            Function<TileEntityType<?>, T> createTE, RegistryObject<? extends Block> valid
    ) {
        return () -> {
            Mutable<TileEntityType<T>> type = new MutableObject<>();
            type.setValue(new TileEntityType<>(
                    () -> createTE.apply(type.getValue()),
                    ImmutableSet.of(valid.get()),
                    null
            ));
            return type.getValue();
        };
    }
}
