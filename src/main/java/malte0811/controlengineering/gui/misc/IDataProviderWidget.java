package malte0811.controlengineering.gui.misc;

import net.minecraft.client.gui.components.AbstractWidget;

import javax.annotation.Nullable;

public interface IDataProviderWidget<T> {
    @Nullable
    T getData();

    interface Factory<T, W extends AbstractWidget & IDataProviderWidget<T>> {
        W create(@Nullable T currentValue, int x, int y);
    }
}
