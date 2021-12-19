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

public record MultiblockBEType<M extends BlockEntity, D extends BlockEntity>(
        RegistryObject<BlockEntityType<M>> master,
        RegistryObject<BlockEntityType<D>> dummy,
        Predicate<BlockState> isMaster
) implements BiFunction<BlockPos, BlockState, BlockEntity> {
    @Override
    public BlockEntity apply(BlockPos pos, BlockState state) {
        if (isMaster.test(state))
            return master.get().create(pos, state);
        else
            return dummy.get().create(pos, state);
    }

    @Nullable
    public <A extends BlockEntity>
    BlockEntityTicker<A> makeMasterTicker(BlockEntityType<A> actual, Consumer<? super M> ticker) {
        if (actual == master.get()) {
            return ($, $1, $2, be) -> ticker.accept((M) be);
        } else {
            return null;
        }
    }

    public static <T extends BlockEntity>
    MultiblockBEType<T, T> makeType(
            DeferredRegister<BlockEntityType<?>> register,
            String name,
            BEConstructor<T> make,
            Supplier<? extends Block> valid,
            Predicate<BlockState> isMaster
    ) {
        return makeType(register, name, make, make, valid, isMaster);
    }

    public static <T extends BlockEntity, D extends BlockEntity>
    MultiblockBEType<T, D> makeType(
            DeferredRegister<BlockEntityType<?>> register,
            String name,
            BEConstructor<T> makeMaster,
            BEConstructor<D> makeDummy,
            Supplier<? extends Block> valid,
            Predicate<BlockState> isMaster
    ) {
        var master = register.register(name + "_master", CEBlockEntities.createBEType(makeMaster, valid));
        var dummy = register.register(name + "_dummy", CEBlockEntities.createBEType(makeDummy, valid));
        return new MultiblockBEType<>(master, dummy, isMaster);
    }
}
