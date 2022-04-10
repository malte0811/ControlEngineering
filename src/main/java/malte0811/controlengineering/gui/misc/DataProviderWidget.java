package malte0811.controlengineering.gui.misc;

import malte0811.controlengineering.gui.widget.NestedWidget;

public abstract class DataProviderWidget<T> extends NestedWidget implements IDataProviderWidget<T> {
    public DataProviderWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
}
