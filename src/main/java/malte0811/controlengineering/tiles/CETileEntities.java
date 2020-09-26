package malte0811.controlengineering.tiles;

import com.google.common.collect.ImmutableSet;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.tiles.bus.BusRelayTile;
import malte0811.controlengineering.tiles.bus.LineAccessTile;
import malte0811.controlengineering.tiles.panels.PanelTileEntity;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class CETileEntities {
    public static final DeferredRegister<TileEntityType<?>> REGISTER = DeferredRegister.create(
            ForgeRegistries.TILE_ENTITIES,
            ControlEngineering.MODID
    );

    public static RegistryObject<TileEntityType<BusRelayTile>> BUS_RELAY = REGISTER.register(
            "bus_relay",
            createTileType(BusRelayTile::new, CEBlocks.BUS_RELAY)
    );

    public static RegistryObject<TileEntityType<LineAccessTile>> LINE_ACCESS = REGISTER.register(
            "line_access",
            createTileType(LineAccessTile::new, CEBlocks.LINE_ACCESS)
    );

    public static RegistryObject<TileEntityType<PanelTileEntity>> CONTROL_PANEL = REGISTER.register(
            "control_panel",
            createTileType(PanelTileEntity::new, CEBlocks.CONTROL_PANEL)
    );

    private static <T extends TileEntity> Supplier<TileEntityType<T>> createTileType(
            Supplier<T> createTE, RegistryObject<? extends Block> valid
    ) {
        return () -> new TileEntityType<>(
                createTE,
                ImmutableSet.of(valid.get()),
                null
        );
    }
}
