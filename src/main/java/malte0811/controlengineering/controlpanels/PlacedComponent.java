package malte0811.controlengineering.controlpanels;

import com.google.common.collect.ImmutableList;
import com.mojang.math.Matrix4f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.util.math.RectangleD;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

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
    private final Lazy<AABB> shape;

    public PlacedComponent(@Nonnull PanelComponentInstance<?, ?> component, @Nonnull Vec2d pos) {
        this.component = component;
        this.pos = pos;
        shape = Lazy.of(() -> {
            AABB compShape = component.getType().getSelectionShape();
            if (compShape == null) {
                return null;
            } else {
                return scale(compShape.move(pos.x(), 0, pos.y()), 1 / 16d);
            }
        });
    }

    @Nullable
    public static PlacedComponent readWithoutState(FriendlyByteBuf from) {
        Vec2d pos = new Vec2d(from);
        PanelComponentInstance<?, ?> instance = PanelComponentInstance.readFrom(from);
        if (instance == null) {
            return null;
        }
        return new PlacedComponent(instance, pos);
    }

    public void writeToWithoutState(FriendlyByteBuf buffer) {
        pos.write(buffer);
        getComponent().writeToWithoutState(buffer);
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
        return pos.add(component.getSize());
    }

    public RectangleD getOutline() {
        return new RectangleD(getPosMin(), getPosMax());
    }

    @Nullable
    public AABB getSelectionShape() {
        return shape.get();
    }

    private static AABB scale(AABB in, double scale) {
        return new AABB(
                in.minX * scale,
                in.minY * scale,
                in.minZ * scale,
                in.maxX * scale,
                in.maxY * scale,
                in.maxZ * scale
        );
    }

    @Override
    @Nullable
    public VoxelShape mainShape() {
        AABB selectionShape = getSelectionShape();
        if (selectionShape != null) {
            return Shapes.create(selectionShape);
        } else {
            return null;
        }
    }

    @Nonnull
    @Override
    public Matrix4f outerToInnerPosition() {
        final var id = new Matrix4f();
        id.setIdentity();
        return id;
    }

    @Nonnull
    @Override
    public List<? extends SelectionShapes> innerShapes() {
        return ImmutableList.of();
    }

    @Override
    public InteractionResult onUse(UseOnContext ctx, InteractionResult defaultType) {
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

    public PlacedComponent copy(boolean clearState) {
        return new PlacedComponent(component.copy(clearState), pos);
    }

    public static List<PlacedComponent> readListFromNBT(Tag list) {
        return Codecs.read(Codec.list(CODEC), list).result().orElseGet(ImmutableList::of);
    }

    public static Tag writeListToNBT(List<PlacedComponent> components) {
        return Codecs.encode(Codec.list(CODEC), components);
    }

    public static int getIndexAt(List<PlacedComponent> components, double x, double y) {
        for (int i = 0; i < components.size(); i++) {
            PlacedComponent p = components.get(i);
            if (p.getOutline().containsClosed(x, y)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PlacedComponent.class.getSimpleName() + "[", "]")
                .add("component=" + component)
                .add("pos=" + pos)
                .toString();
    }
}
