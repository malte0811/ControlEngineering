package malte0811.controlengineering.tiles.panels;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.panels.PanelDesignerBlock;
import malte0811.controlengineering.blocks.panels.PanelDesignerBlock.Offset;
import malte0811.controlengineering.blocks.shapes.ListShapes;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.cnc.CNCInstructionGenerator;
import malte0811.controlengineering.tiles.base.CETileEntity;
import malte0811.controlengineering.tiles.tape.TeletypeState;
import malte0811.controlengineering.tiles.tape.TeletypeTile;
import malte0811.controlengineering.util.BitUtils;
import malte0811.controlengineering.util.CachedValue;
import malte0811.controlengineering.util.math.Matrix4;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PanelDesignerTile extends CETileEntity implements SelectionShapeOwner {
    private static final Codec<List<PlacedComponent>> COMPONENTS_CODEC = Codec.list(PlacedComponent.CODEC);

    private final CachedValue<BlockState, SelectionShapes> shapes = new CachedValue<>(
            this::getBlockState,
            state -> {
                Offset offset = state.get(PanelDesignerBlock.OFFSET);
                Direction facing = state.get(PanelDesignerBlock.FACING);
                VoxelShape baseShape = PanelDesignerBlock.SHAPE.apply(offset, facing);
                if (offset == Offset.ORIGIN) {
                    Function<ItemUseContext, ActionResultType> openUI = makeInteraction(
                            state, PanelDesignerTile::openUI
                    );
                    return new ListShapes(
                            baseShape,
                            Matrix4.inverseFacing(facing),
                            ImmutableList.of(
                                    new SingleShape(PanelDesignerBlock.TABLE_TOP, openUI)
                            ),
                            $ -> ActionResultType.PASS
                    );
                } else if (offset == Offset.BACK_TOP) {
                    Function<ItemUseContext, ActionResultType> writeTape = makeInteraction(
                            state, PanelDesignerTile::writeTape
                    );
                    Function<ItemUseContext, ActionResultType> addTape = makeInteraction(
                            state, (t, ctx) -> t.state.removeOrAddClearTape(ctx.getPlayer(), ctx.getItem())
                    );
                    Function<ItemUseContext, ActionResultType> takeTape = makeInteraction(
                            state, (t, ctx) -> t.state.removeWrittenTape(ctx.getPlayer())
                    );
                    return new ListShapes(
                            baseShape,
                            Matrix4.inverseFacing(facing),
                            ImmutableList.of(
                                    new SingleShape(PanelDesignerBlock.BUTTON, writeTape),
                                    new SingleShape(TeletypeTile.INPUT_SHAPE, addTape),
                                    new SingleShape(TeletypeTile.OUTPUT_SHAPE, takeTape)
                            ),
                            $ -> ActionResultType.PASS
                    );
                } else {
                    return new SingleShape(baseShape, $ -> ActionResultType.PASS);
                }
            }
    );
    private final CachedValue<List<PlacedComponent>, Integer> requiredLength;

    private List<PlacedComponent> components = new ArrayList<>();
    private TeletypeState state = new TeletypeState();

    public PanelDesignerTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        requiredLength = new CachedValue<>(
                () -> components,
                l -> CNCInstructionGenerator.toInstructions(l).length(),
                l -> l.stream().map(c -> c.copy(false)).collect(Collectors.toList())
        );
    }

    private Function<ItemUseContext, ActionResultType> makeInteraction(
            BlockState state, BiFunction<PanelDesignerTile, ItemUseContext, ActionResultType> handler
    ) {
        return ctx -> {
            BlockPos origin = CEBlocks.PANEL_DESIGNER.get().getMainBlock(state, this);
            TileEntity atOrigin = world.getTileEntity(origin);
            if (atOrigin instanceof PanelDesignerTile) {
                return handler.apply((PanelDesignerTile) atOrigin, ctx);
            } else {
                return ActionResultType.FAIL;
            }
        };
    }

    @Override
    public SelectionShapes getShape() {
        return shapes.get();
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
        super.read(state, nbt);
        components = new ArrayList<>(
                Codecs.readOptional(COMPONENTS_CODEC, nbt.get("components")).orElse(ImmutableList.of())
        );
        this.state = Codecs.readOptional(TeletypeState.CODEC, nbt.get("state")).orElseGet(TeletypeState::new);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        compound = super.write(compound);
        compound.put("components", Codecs.encode(COMPONENTS_CODEC, components));
        compound.put("state", Codecs.encode(TeletypeState.CODEC, state));
        return compound;
    }

    public List<PlacedComponent> getComponents() {
        return components;
    }

    private ActionResultType openUI(ItemUseContext ctx) {
        if (!world.isRemote) {
            CEBlocks.PANEL_DESIGNER.get().openContainer(ctx.getPlayer(), getBlockState(), world, pos);
        }
        return ActionResultType.SUCCESS;
    }

    public int getLengthRequired() {
        return requiredLength.get();
    }

    private ActionResultType writeTape(ItemUseContext ctx) {
        final String instructions = CNCInstructionGenerator.toInstructions(components);
        final byte[] bytes = BitUtils.toBytesWithParity(instructions);
        state.tryTypeAll(new ByteArrayList(bytes, 0, bytes.length));
        return ActionResultType.SUCCESS;
    }

    public TeletypeState getTTY() {
        return state;
    }
}
