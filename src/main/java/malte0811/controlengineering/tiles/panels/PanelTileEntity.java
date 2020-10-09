package malte0811.controlengineering.tiles.panels;

import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.bus.BusEmitterCombiner;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.controlpanels.PanelTransform;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.components.Button;
import malte0811.controlengineering.controlpanels.components.Indicator;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.util.Codecs;
import malte0811.controlengineering.util.RaytraceUtils;
import malte0811.controlengineering.util.ShapeUtils;
import malte0811.controlengineering.util.Vec2d;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PanelTileEntity extends TileEntity {
    private List<PlacedComponent> components = new ArrayList<>();
    private PanelTransform transform = new PanelTransform(
            0.25F,
            (float) Math.toDegrees(Math.atan(0.5)),
            PanelOrientation.DOWN_NORTH
    );
    private final ResettableLazy<VoxelShape> shape = new ResettableLazy<>(
            () -> {
                List<AxisAlignedBB> parts = new ArrayList<>();
                final double frontHeight = Math.max(transform.getCenterHeight(), transform.getFrontHeight());
                final double backHeight = Math.max(transform.getCenterHeight(), transform.getBackHeight());
                parts.add(new AxisAlignedBB(0, 0, 0, 0.5, frontHeight, 1));
                parts.add(new AxisAlignedBB(0.5, 0, 0, 1, backHeight, 1));
                return ShapeUtils.or(
                        parts.stream().map(ShapeUtils.transformFunc(transform.getPanelBottomToWorld()))
                );
            }
    );
    private final BusEmitterCombiner<Integer> stateHandler = new BusEmitterCombiner<>(
            i -> components.get(i).getComponent().getEmittedState(),
            i -> components.get(i).getComponent().updateTotalState(getTotalState())
    );

    public PanelTileEntity() {
        super(CETileEntities.CONTROL_PANEL.get());
        Button b = PanelComponents.BUTTON.empty();
        b.setColor(0xff0000);
        b.setOutputSignal(new BusSignalRef(0, 0));
        components.add(new PlacedComponent(b, new Vec2d(5, 6)));
        b = PanelComponents.BUTTON.empty();
        b.setColor(0xff00);
        b.setOutputSignal(new BusSignalRef(0, 1));
        components.add(new PlacedComponent(b, new Vec2d(5, 7)));
        b = PanelComponents.BUTTON.empty();
        b.setColor(0xff);
        b.setOutputSignal(new BusSignalRef(0, 2));
        components.add(new PlacedComponent(b, new Vec2d(6, 6)));
        Indicator ind = PanelComponents.INDICATOR.empty();
        components.add(new PlacedComponent(ind, new Vec2d(6, 7)));
        resetStateHandler();
    }

    private void resetStateHandler() {
        stateHandler.clear();
        for (int i = 0; i < components.size(); ++i) {
            stateHandler.addEmitter(i);
        }
        updateBusState();
    }

    private void updateBusState() {
        BusState oldState = stateHandler.getTotalEmittedState();
        //TODO include input state
        stateHandler.updateState(new BusState());
        if (!oldState.equals(stateHandler.getTotalEmittedState())) {
            //TODO update output state
        }
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
        super.read(state, nbt);
        this.components = Codecs.read(Codec.list(PlacedComponent.CODEC), nbt, "components");
        this.transform = PanelTransform.from(nbt, state.get(PanelOrientation.PROPERTY));
        if (world != null && !world.isRemote) {
            resetStateHandler();
        }
        shape.reset();
    }

    @Override
    public void setWorldAndPos(@Nonnull World world, @Nonnull BlockPos pos) {
        super.setWorldAndPos(world, pos);
        if (!world.isRemote) {
            resetStateHandler();
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        CompoundNBT encoded = super.write(compound);
        Codecs.add(Codec.list(PlacedComponent.CODEC), components, encoded, "components");
        transform.addTo(compound);
        return encoded;
    }

    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbttagcompound = new CompoundNBT();
        write(nbttagcompound);
        return new SUpdateTileEntityPacket(this.pos, 3, nbttagcompound);
    }

    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(getBlockState(), pkt.getNbtCompound());
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    public Optional<PlacedComponent> getTargetedComponent(RayTraceContext ctx) {
        BlockPos topPos = pos.offset(getBlockState().get(PanelOrientation.PROPERTY).top);
        RayTraceContext topCtx = getTransform().toPanelRay(ctx.getStartVec(), ctx.getEndVec(), topPos);
        Optional<PlacedComponent> closest = Optional.empty();
        double minDistanceSq = Double.POSITIVE_INFINITY;
        for (PlacedComponent comp : getComponents()) {
            final AxisAlignedBB selectionShape = comp.getSelectionShape();
            if (selectionShape != null) {
                final Optional<Vector3d> result = selectionShape.rayTrace(topCtx.getStartVec(), topCtx.getEndVec());
                if (result.isPresent()) {
                    final double distanceSq = result.get().squareDistanceTo(topCtx.getStartVec());
                    if (distanceSq < minDistanceSq) {
                        minDistanceSq = distanceSq;
                        closest = Optional.of(comp);
                    }
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

    private BusState getTotalState() {
        return stateHandler.getTotalState();
    }

    public ActionResultType onRightClick(PlayerEntity player, BlockState state) {
        RayTraceContext raytraceCtx = RaytraceUtils.create(player, 0);
        Optional<PlacedComponent> targeted = getTargetedComponent(raytraceCtx);
        ActionResultType result = targeted.map(PlacedComponent::onClick).orElse(ActionResultType.PASS);
        updateBusState();
        markDirty();
        world.notifyBlockUpdate(pos, state, state, 0);
        return result;
    }

    public VoxelShape getShape() {
        return shape.get();
    }
}
