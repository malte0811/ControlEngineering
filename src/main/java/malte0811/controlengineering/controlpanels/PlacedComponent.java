package malte0811.controlengineering.controlpanels;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.util.math.RectangleD;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Lazy;
import org.joml.Matrix4fc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class PlacedComponent extends SelectionShapes {
    public static final MyCodec<PlacedComponent> CODEC = new RecordCodec2<>(
            new CodecField<>("component", pc -> pc.component, PanelComponentInstance.CODEC),
            new CodecField<>("position", pc -> pc.pos, Vec2d.CODEC),
            PlacedComponent::new
    );
    public static final MyCodec<List<PlacedComponent>> LIST_CODEC = MyCodecs.list(CODEC);

    @Nonnull
    private final PanelComponentInstance<?, ?> component;
    @Nonnull
    private final Vec2d pos;
    private final Lazy<AABB> shape;

    public PlacedComponent(@Nonnull PanelComponentInstance<?, ?> component, @Nonnull Vec2d pos) {
        this.component = component;
        this.pos = pos;
        shape = Lazy.of(() -> {
            AABB compShape = component.getSelectionShape();
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
    public Vec2d getPosMax(Level level) {
        return pos.add(component.getSize(level));
    }

    public RectangleD getOutline(Level level) {
        return new RectangleD(getPosMin(), getPosMax(level));
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
    public Matrix4fc outerToInnerPosition() {
        return IDENTITY;
    }

    @Nonnull
    @Override
    public List<? extends SelectionShapes> innerShapes() {
        return ImmutableList.of();
    }

    @Override
    public InteractionResult onUse(UseOnContext ctx, InteractionResult defaultType, Vec3 relativeHit) {
        return component.onClick(
                new PanelComponentType.ComponentClickContext(
                        relativeHit.scale(16).subtract(pos.x(), 0, pos.y()),
                        ctx.getPlayer(),
                        ctx.getHand()
                ),
                ctx.getLevel().isClientSide()
        );
    }

    public PanelComponentInstance.TickResult tick() {
        return component.tick();
    }

    public boolean disjoint(Level level, PlacedComponent other) {
        return getOutline(level).disjoint(other.getOutline(level));
    }

    public boolean isWithinPanel(Level level) {
        return new RectangleD(0, 0, 16, 16).contains(getOutline(level));
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
        return LIST_CODEC.fromNBT(list, List::of);
    }

    public static Tag writeListToNBT(List<PlacedComponent> components) {
        return LIST_CODEC.toNBT(components);
    }

    public static int getIndexAt(Level level, List<PlacedComponent> components, double x, double y) {
        for (int i = 0; i < components.size(); i++) {
            PlacedComponent p = components.get(i);
            if (p.getOutline(level).containsClosed(x, y)) {
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
