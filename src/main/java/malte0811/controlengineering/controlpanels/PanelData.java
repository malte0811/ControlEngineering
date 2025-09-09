package malte0811.controlengineering.controlpanels;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.blockentity.panels.ControlPanelBlockEntity;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public record PanelData(List<PlacedComponent> components, PanelTransform transform) {

    public PanelData() {
        this(ImmutableList.of(), new PanelTransform());
    }

    public PanelData(CompoundTag nbt, PanelOrientation orientation) {
        this(
                PlacedComponent.readListFromNBT(nbt.getList("components", Tag.TAG_COMPOUND)),
                PanelTransform.from(nbt, orientation)
        );
    }

    public PanelData(List<PlacedComponent> components, PanelTransform.BETransformData transform, PanelOrientation orientation) {
        this(components, new PanelTransform(transform, orientation));
    }

    public PanelData(ControlPanelBlockEntity bEntity) {
        this(bEntity.getComponents(), bEntity.getTransform());
    }

    public CompoundTag toNBT() {
        CompoundTag result = new CompoundTag();
        result.put("components", PlacedComponent.writeListToNBT(components()));
        transform().addTo(result);
        return result;
    }

    public PanelData copy(boolean clearState) {
        List<PlacedComponent> copiedComponents = new ArrayList<>(components.size());
        for (PlacedComponent component : components) {
            copiedComponents.add(component.copy(clearState));
        }
        return new PanelData(copiedComponents, transform());
    }
}
