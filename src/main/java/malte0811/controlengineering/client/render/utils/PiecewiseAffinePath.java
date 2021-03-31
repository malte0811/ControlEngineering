package malte0811.controlengineering.client.render.utils;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.function.BinaryOperator;

public class PiecewiseAffinePath<Vec> {
    private final List<Node<Vec>> nodes;
    private final ScalarMultiplier<Vec> multiplier;
    private final BinaryOperator<Vec> adder;

    public PiecewiseAffinePath(List<Node<Vec>> nodes, ScalarMultiplier<Vec> multiplier, BinaryOperator<Vec> adder) {
        this.nodes = nodes;
        Preconditions.checkArgument(!nodes.isEmpty());
        this.multiplier = multiplier;
        this.adder = adder;
    }

    public Vec getPosAt(double time) {
        final Node<Vec> first = nodes.get(0);
        if (time < first.time) {
            return first.point;
        }
        for (int nextId = 1; nextId < nodes.size(); ++nextId) {
            final Node<Vec> nextNode = nodes.get(nextId);
            if (time < nextNode.time) {
                final Node<Vec> lastNode = nodes.get(nextId - 1);
                final double lambda = (time - lastNode.time) / (nextNode.time - lastNode.time);
                return adder.apply(
                        multiplier.scale(lastNode.point, 1 - lambda),
                        multiplier.scale(nextNode.point, lambda)
                );
            }
        }
        return nodes.get(nodes.size() - 1).point;
    }

    @FunctionalInterface
    public interface ScalarMultiplier<Vec> {
        Vec scale(Vec vector, double value);
    }

    public static class Node<Vec> {
        private final Vec point;
        private final double time;

        public Node(Vec point, double time) {
            this.point = point;
            this.time = time;
        }
    }
}
