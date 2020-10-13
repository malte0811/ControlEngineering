package malte0811.controlengineering.bus;

import malte0811.controlengineering.util.Clearable;

import java.util.ArrayList;
import java.util.List;

public class MarkDirtyHandler implements Runnable {
    private final List<Clearable<Runnable>> activeCallbacks = new ArrayList<>();

    @Override
    public void run() {
        cleanupList();
        activeCallbacks.forEach(c -> c.ifPresent(Runnable::run));
    }

    public void addCallback(Clearable<Runnable> newRunner) {
        cleanupList();
        this.activeCallbacks.add(newRunner);
    }

    private void cleanupList() {
        activeCallbacks.removeIf(c -> !c.isPresent());
    }
}
