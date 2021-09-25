package malte0811.controlengineering.controlpanels;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.tiles.panels.ControlPanelTile;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PanelData {
    private final List<PlacedComponent> components;
    private final PanelTransform transform;

    public PanelData(List<PlacedComponent> components, PanelTransform transform) {
        this.components = components;
        this.transform = transform;
    }

    public PanelData() {
        this(ImmutableList.of(), new PanelTransform());
    }

    public PanelData(CompoundTag nbt, PanelOrientation orientation) {
        this(
                PlacedComponent.readListFromNBT(nbt.getList("components", Constants.NBT.TAG_COMPOUND)),
                PanelTransform.from(nbt, orientation)
        );
    }

    public PanelData(ControlPanelTile tile) {
        this(tile.getComponents(), tile.getTransform());
    }

    public CompoundTag toNBT() {
        CompoundTag result = new CompoundTag();
        result.put("components", PlacedComponent.writeListToNBT(getComponents()));
        getTransform().addTo(result);
        return result;
    }

    public List<PlacedComponent> getComponents() {
        return components;
    }

    public PanelData copy(boolean clearState) {
        List<PlacedComponent> copiedComponents = new ArrayList<>(components.size());
        for (PlacedComponent component : components) {
            copiedComponents.add(component.copy(clearState));
        }
        return new PanelData(copiedComponents, getTransform());
    }

    public PanelTransform getTransform() {
        return transform;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PanelData panelData = (PanelData) o;
        return Objects.equals(components, panelData.components) && Objects.equals(transform, panelData.transform);
    }

    @Override
    public int hashCode() {
        return Objects.hash(components, transform);
    }
}
