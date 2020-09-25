package malte0811.controlengineering.tiles;

import com.google.common.collect.ImmutableSet;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.passive.fish.CodEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
