package malte0811.controlengineering.blockentity;

import malte0811.controlengineering.blockentity.CEBlockEntities.BEConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public record MultiblockBEType<T extends BlockEntity>(
        RegistryObject<BlockEntityType<T>> master,
        RegistryObject<BlockEntityType<T>> dummy,
        Predicate<BlockState> isMaster
) implements BiFunction<BlockPos, BlockState, T> {
    @Override
    public T apply(BlockPos pos, BlockState state) {
        if (isMaster.test(state))
            return master.get().create(pos, state);
        else
            return dummy.get().create(pos, state);
    }

    @Nullable
    public <A extends BlockEntity>
    BlockEntityTicker<A> makeMasterTicker(BlockEntityType<A> actual, Consumer<? super T> ticker) {
        if (actual == master.get()) {
            return ($, $1, $2, be) -> ticker.accept((T) be);
        } else {
            return null;
        }
    }

    public static <T extends BlockEntity>
    MultiblockBEType<T> makeType(
            DeferredRegister<BlockEntityType<?>> register,
            String name,
            BEConstructor<T> create,
            Supplier<? extends Block> valid,
            Predicate<BlockState> isMaster
    ) {
        var master = register.register(name + "_master", CEBlockEntities.createBEType(create, valid));
        var dummy = register.register(name + "_dummy", CEBlockEntities.createBEType(create, valid));
        return new MultiblockBEType<>(master, dummy, isMaster);
    }
}
