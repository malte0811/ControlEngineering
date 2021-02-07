package malte0811.controlengineering.logic.circuit;

import java.util.Objects;

public class NetReference {
    private final String id;

    public NetReference(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetReference net = (NetReference) o;
        return id.equals(net.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
