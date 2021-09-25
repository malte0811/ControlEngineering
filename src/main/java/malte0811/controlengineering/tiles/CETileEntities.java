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
import malte0811.controlengineering.tiles.tape.KeypunchTile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.function.Function;
import java.util.function.Supplier;

public class CETileEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(
            ForgeRegistries.TILE_ENTITIES, ControlEngineering.MODID
    );

    public static RegistryObject<BlockEntityType<BusRelayTile>> BUS_RELAY = REGISTER.register(
            "bus_relay", createTileType(BusRelayTile::new, CEBlocks.BUS_RELAY)
    );

    public static RegistryObject<BlockEntityType<BusInterfaceTile>> BUS_INTERFACE = REGISTER.register(
            "bus_interface", createTileType(BusInterfaceTile::new, CEBlocks.BUS_INTERFACE)
    );

    public static RegistryObject<BlockEntityType<LineAccessTile>> LINE_ACCESS = REGISTER.register(
            "line_access", createTileType(LineAccessTile::new, CEBlocks.LINE_ACCESS)
    );

    public static RegistryObject<BlockEntityType<ControlPanelTile>> CONTROL_PANEL = REGISTER.register(
            "control_panel", createTileType(ControlPanelTile::new, CEBlocks.CONTROL_PANEL)
    );

    public static RegistryObject<BlockEntityType<PanelCNCTile>> PANEL_CNC = REGISTER.register(
            "panel_cnc", createTileType(PanelCNCTile::new, CEBlocks.PANEL_CNC)
    );

    public static RegistryObject<BlockEntityType<PanelDesignerTile>> PANEL_DESIGNER = REGISTER.register(
            "panel_designer", createTileType(PanelDesignerTile::new, CEBlocks.PANEL_DESIGNER)
    );

    public static RegistryObject<BlockEntityType<KeypunchTile>> KEYPUNCH = REGISTER.register(
            "keypunch", createTileType(KeypunchTile::new, CEBlocks.KEYPUNCH)
    );

    public static RegistryObject<BlockEntityType<LogicCabinetTile>> LOGIC_CABINET = REGISTER.register(
            "logic_cabinet", createTileType(LogicCabinetTile::new, CEBlocks.LOGIC_CABINET)
    );

    public static RegistryObject<BlockEntityType<LogicWorkbenchTile>> LOGIC_WORKBENCH = REGISTER.register(
            "logic_workbench", createTileType(LogicWorkbenchTile::new, CEBlocks.LOGIC_WORKBENCH)
    );

    private static <T extends BlockEntity> Supplier<BlockEntityType<T>> createTileType(
            Function<BlockEntityType<?>, T> createTE, RegistryObject<? extends Block> valid
    ) {
        return () -> {
            Mutable<BlockEntityType<T>> type = new MutableObject<>();
            type.setValue(new BlockEntityType<>(
                    () -> createTE.apply(type.getValue()),
                    ImmutableSet.of(valid.get()),
                    null
            ));
            return type.getValue();
        };
    }
}
