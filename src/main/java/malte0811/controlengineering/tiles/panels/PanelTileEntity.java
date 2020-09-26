package malte0811.controlengineering.tiles.panels;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.components.Button;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.util.Vec2d;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

public class PanelTileEntity extends TileEntity {
    private List<PlacedComponent> components = new ArrayList<>();
    public PanelTileEntity() {
        super(CETileEntities.CONTROL_PANEL.get());
        Button b = PanelComponents.BUTTON.empty();
        b.setColor(0xff0000);
        components.add(new PlacedComponent(b, new Vec2d(5, 6)));
        b = PanelComponents.BUTTON.empty();
        b.setColor(0xff00);
        components.add(new PlacedComponent(b, new Vec2d(5, 7)));
        b = PanelComponents.BUTTON.empty();
        b.setColor(0xff);
        components.add(new PlacedComponent(b, new Vec2d(6, 6)));
    }

    //TODO client sync
    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        this.components = Codec.list(PlacedComponent.CODEC)
                .decode(NBTDynamicOps.INSTANCE, nbt)
                .getOrThrow(false, s -> {})
                .getFirst();
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        CompoundNBT encoded = super.write(compound);
        INBT componentNBT = Codec.list(PlacedComponent.CODEC)
                .encodeStart(NBTDynamicOps.INSTANCE, components)
                .getOrThrow(false, s -> {});
        encoded.put("components", componentNBT);
        return encoded;
    }

    public List<PlacedComponent> getComponents() {
        return components;
    }
}
