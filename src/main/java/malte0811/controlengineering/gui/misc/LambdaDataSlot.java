package malte0811.controlengineering.gui.misc;

import net.minecraft.world.inventory.DataSlot;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class LambdaDataSlot extends DataSlot {
    private final IntSupplier get;
    private final IntConsumer set;

    public LambdaDataSlot(IntSupplier get, IntConsumer set) {
        this.get = get;
        this.set = set;
    }

    public static LambdaDataSlot serverSide(IntSupplier get) {
        return new LambdaDataSlot(get, i -> {throw new RuntimeException();});
    }

    @Override
    public int get() {
        return get.getAsInt();
    }

    @Override
    public void set(int pValue) {
        set.accept(pValue);
    }
}
