package malte0811.controlengineering.tiles.panels;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.controlpanels.PanelTransform;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.components.Button;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.util.Vec2d;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PanelTileEntity extends TileEntity {
    private List<PlacedComponent> components = new ArrayList<>();
    private PanelTransform transform = new PanelTransform(0.25F, (float) Math.toDegrees(Math.atan(0.5)), Direction.DOWN);

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

    //TODO client update sync
    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
        super.read(state, nbt);
        this.components = read(Codec.list(PlacedComponent.CODEC), nbt, "components");
        this.transform = read(PanelTransform.CODEC, nbt, "transform");
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        CompoundNBT encoded = super.write(compound);
        add(Codec.list(PlacedComponent.CODEC), components, encoded, "components");
        add(PanelTransform.CODEC, transform, encoded, "transform");
        return encoded;
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    public Optional<PlacedComponent> getTargetedComponent(RayTraceContext ctx) {
        RayTraceContext topCtx = getTransform().toPanelRay(ctx.getStartVec(), ctx.getEndVec(), pos);
        Optional<PlacedComponent> closest = Optional.empty();
        double minDistanceSq = Double.POSITIVE_INFINITY;
        for (PlacedComponent comp : getComponents()) {
            final AxisAlignedBB selectionShape = comp.getSelectionShape();
            final Optional<Vector3d> result = selectionShape.rayTrace(topCtx.getStartVec(), topCtx.getEndVec());
            if (result.isPresent()) {
                final double distanceSq = result.get().squareDistanceTo(topCtx.getStartVec());
                if (distanceSq < minDistanceSq) {
                    minDistanceSq = distanceSq;
                    closest = Optional.of(comp);
                }
            }
        }
        return closest;
    }

    public List<PlacedComponent> getComponents() {
        return components;
    }

    public PanelTransform getTransform() {
        return transform;
    }

    private static <T> T read(Codec<T> codec, CompoundNBT in, String subName) {
        return codec.decode(NBTDynamicOps.INSTANCE, in.get(subName))
                .getOrThrow(false, s -> {})
                .getFirst();
    }

    private static <T> void add(Codec<T> codec, T value, CompoundNBT out, String subName) {
        INBT componentNBT = codec.encodeStart(NBTDynamicOps.INSTANCE, value)
                .getOrThrow(false, s -> {});
        out.put(subName, componentNBT);
    }
}
