package malte0811.controlengineering.controlpanels;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.util.math.Matrix4;
import malte0811.controlengineering.util.math.RectangleD;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class PlacedComponent extends SelectionShapes {
    public static final Codec<PlacedComponent> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    PanelComponentInstance.CODEC.fieldOf("component").forGetter(pc -> pc.component),
                    Vec2d.CODEC.fieldOf("position").forGetter(pc -> pc.pos)
            ).apply(inst, PlacedComponent::new)
    );

    @Nonnull
    private final PanelComponentInstance<?, ?> component;
    @Nonnull
    private final Vec2d pos;
    private final Lazy<AxisAlignedBB> shape;

    public PlacedComponent(@Nonnull PanelComponentInstance<?, ?> component, @Nonnull Vec2d pos) {
        this.component = component;
        this.pos = pos;
        shape = Lazy.of(() -> {
            AxisAlignedBB compShape = component.getType().getSelectionShape();
            if (compShape == null) {
                return null;
            } else {
                return scale(compShape.offset(pos.x, 0, pos.y), 1 / 16d);
            }
        });
    }

    @Nonnull
    public PanelComponentInstance<?, ?> getComponent() {
        return component;
    }

    @Nonnull
    public Vec2d getPosMin() {
        return pos;
    }

    @Nonnull
    public Vec2d getPosMax() {
        return pos.add(component.getType().getSize());
    }

    public RectangleD getOutline() {
        return new RectangleD(getPosMin(), getPosMax());
    }

    @Nullable
    public AxisAlignedBB getSelectionShape() {
        return shape.get();
    }

    public ActionResultType onClick() {
        return component.onClick();
    }

    private static AxisAlignedBB scale(AxisAlignedBB in, double scale) {
        return new AxisAlignedBB(
                in.minX * scale,
                in.minY * scale,
                in.minZ * scale,
                in.maxX * scale,
                in.maxY * scale,
                in.maxZ * scale
        );
    }

    @Override
    public @Nullable
    VoxelShape mainShape() {
        AxisAlignedBB selectionShape = getSelectionShape();
        if (selectionShape != null) {
            return VoxelShapes.create(selectionShape);
        } else {
            return null;
        }
    }

    @Nonnull
    @Override
    public Matrix4 outerToInnerPosition() {
        return Matrix4.IDENTITY;
    }

    @Nonnull
    @Override
    public List<? extends SelectionShapes> innerShapes() {
        return ImmutableList.of();
    }

    @Override
    public ActionResultType onUse(ItemUseContext ctx, ActionResultType defaultType) {
        return component.onClick();
    }

    public boolean disjoint(PlacedComponent other) {
        return getOutline().disjoint(other.getOutline());
    }

    public boolean isWithinPanel() {
        return new RectangleD(0, 0, 16, 16).contains(getOutline());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlacedComponent that = (PlacedComponent) o;
        return component.equals(that.component) && pos.equals(that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(component, pos);
    }

    public PlacedComponent copy() {
        return new PlacedComponent(component.copy(), pos);
    }

    public static List<PlacedComponent> readListFromNBT(INBT list) {
        return Codecs.read(Codec.list(CODEC), list).result().orElseGet(ImmutableList::of);
    }

    public static INBT writeListToNBT(List<PlacedComponent> components) {
        return Codecs.encode(Codec.list(CODEC), components);
    }
}
