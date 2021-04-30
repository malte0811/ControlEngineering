package malte0811.controlengineering.gui.misc;

import malte0811.controlengineering.gui.widget.NestedWidget;

import javax.annotation.Nullable;

public abstract class DataProviderWidget<T> extends NestedWidget {

    public DataProviderWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Nullable
    public abstract T getData();

    public interface Factory<T> {
        DataProviderWidget<T> create(@Nullable T currentValue, int x, int y);
    }
}
