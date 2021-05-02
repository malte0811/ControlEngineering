package malte0811.controlengineering.tiles.panels;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PanelDesignerTile extends TileEntity {
    private static final Codec<List<PlacedComponent>> COMPONENTS_CODEC = Codec.list(PlacedComponent.CODEC);

    private List<PlacedComponent> components = new ArrayList<>();

    public PanelDesignerTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
        super.read(state, nbt);
        components = new ArrayList<>(
                Codecs.read(COMPONENTS_CODEC, nbt.get("components")).result().orElse(ImmutableList.of())
        );
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        compound = super.write(compound);
        compound.put("components", Codecs.encode(COMPONENTS_CODEC, components));
        return compound;
    }

    public List<PlacedComponent> getComponents() {
        return components;
    }
}
