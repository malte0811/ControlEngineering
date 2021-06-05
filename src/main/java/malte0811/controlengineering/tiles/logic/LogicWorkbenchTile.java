package malte0811.controlengineering.tiles.logic;

import blusunrize.immersiveengineering.api.IETags;
import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.logic.LogicWorkbenchBlock;
import malte0811.controlengineering.blocks.shapes.ListShapes;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.items.IEItemRefs;
import malte0811.controlengineering.items.PCBStackItem;
import malte0811.controlengineering.logic.circuit.BusConnectedCircuit;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.SchematicCircuitConverter;
import malte0811.controlengineering.tiles.base.CETileEntity;
import malte0811.controlengineering.util.CachedValue;
import malte0811.controlengineering.util.ItemUtil;
import malte0811.controlengineering.util.math.Matrix4;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LogicWorkbenchTile extends CETileEntity implements SelectionShapeOwner, ISchematicTile {
    private static final ITag<Item> TUBES = IETags.circuitLogic;
    //TODO solder?
    //TODO add utility tag
    private static final ITag<Item> WIRE = IETags.copperWire;

    private Schematic schematic = new Schematic();
    private final CircuitIngredientDrawer tubeStorage = new CircuitIngredientDrawer(TUBES);
    private final CircuitIngredientDrawer wireStorage = new CircuitIngredientDrawer(WIRE);
    private final CachedValue<BlockState, SelectionShapes> shapes = new CachedValue<>(
            this::getBlockState,
            state -> {
                LogicWorkbenchBlock.Offset offset = state.get(LogicWorkbenchBlock.OFFSET);
                Direction facing = state.get(LogicWorkbenchBlock.FACING);
                VoxelShape baseShape = LogicWorkbenchBlock.SHAPE.apply(offset, facing);
                Function<ItemUseContext, ActionResultType> wireDrawer = makeInteraction(
                        state, makeDrawerClick(t -> t.wireStorage)
                );
                Function<ItemUseContext, ActionResultType> tubeDrawer = makeInteraction(
                        state, makeDrawerClick(t -> t.tubeStorage)
                );
                if (offset == LogicWorkbenchBlock.Offset.TOP_RIGHT) {
                    Function<ItemUseContext, ActionResultType> create = makeInteraction(
                            state, LogicWorkbenchTile::handleCreationClick
                    );
                    return new ListShapes(
                            baseShape,
                            Matrix4.inverseFacing(facing),
                            ImmutableList.of(
                                    new SingleShape(LogicWorkbenchBlock.BURNER, create),
                                    new SingleShape(LogicWorkbenchBlock.WIRE_DRAWER_TOP_RIGHT, wireDrawer)
                            ),
                            $ -> ActionResultType.PASS
                    );
                } else if (offset == LogicWorkbenchBlock.Offset.TOP_LEFT) {
                    return new ListShapes(
                            baseShape,
                            Matrix4.inverseFacing(facing),
                            ImmutableList.of(
                                    new SingleShape(LogicWorkbenchBlock.TUBE_DRAWER, tubeDrawer),
                                    new SingleShape(LogicWorkbenchBlock.WIRE_DRAWER, wireDrawer)
                            ),
                            $ -> ActionResultType.PASS
                    );
                } else {
                    return new SingleShape(baseShape, makeInteraction(state, LogicWorkbenchTile::handleMainClick));
                }
            }
    );

    public LogicWorkbenchTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    private Function<ItemUseContext, ActionResultType> makeInteraction(
            BlockState state, BiFunction<LogicWorkbenchTile, ItemUseContext, ActionResultType> handler
    ) {
        return ctx -> {
            BlockPos origin = CEBlocks.LOGIC_WORKBENCH.get().getMainBlock(state, this);
            TileEntity atOrigin = world.getTileEntity(origin);
            if (atOrigin instanceof LogicWorkbenchTile) {
                return handler.apply((LogicWorkbenchTile) atOrigin, ctx);
            } else {
                return ActionResultType.FAIL;
            }
        };
    }

    @Override
    public SelectionShapes getShape() {
        return shapes.get();
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        compound.put("schematic", Codecs.encode(Schematic.CODEC, schematic));
        compound.put("tubes", tubeStorage.write());
        compound.put("wires", wireStorage.write());
        return super.write(compound);
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
        super.read(state, nbt);
        schematic = Codecs.readOrNull(Schematic.CODEC, nbt.get("schematic"));
        tubeStorage.read(nbt.getCompound("tubes"));
        wireStorage.read(nbt.getCompound("wires"));
        if (schematic == null) {
            schematic = new Schematic();
        }
    }

    private ActionResultType handleMainClick(ItemUseContext ctx) {
        if (!world.isRemote) {
            CEBlocks.LOGIC_WORKBENCH.get().openContainer(
                    ctx.getPlayer(), getBlockState(), ctx.getWorld(), pos
            );
        }
        return ActionResultType.SUCCESS;
    }

    private ActionResultType handleCreationClick(ItemUseContext ctx) {
        if (ctx.getPlayer() == null || ctx.getItem().getItem() != IEItemRefs.CIRCUIT_BOARD.get()) {
            return ActionResultType.PASS;
        }
        Optional<BusConnectedCircuit> circuit = SchematicCircuitConverter.toCircuit(schematic);
        if (!circuit.isPresent()) {
            return ActionResultType.FAIL;
        }
        final int numTubes = circuit.get().getNumTubes();
        final int numBoards = (numTubes + 15) / 16;
        if (numBoards > ctx.getItem().getCount()) {
            return ActionResultType.FAIL;
        }
        final int numWires = circuit.get().getWireLength();
        if (!world.isRemote) {
            if (tubeStorage.canConsume(numTubes) && wireStorage.canConsume(numWires)) {
                tubeStorage.consume(numTubes);
                wireStorage.consume(numWires);
                ctx.getItem().shrink(numBoards);
                ItemUtil.giveOrDrop(ctx.getPlayer(), PCBStackItem.forSchematic(schematic));
            } else {
                //TODO show message
            }
        }
        return ActionResultType.SUCCESS;
    }

    private static BiFunction<LogicWorkbenchTile, ItemUseContext, ActionResultType> makeDrawerClick(
            Function<LogicWorkbenchTile, CircuitIngredientDrawer> getDrawer
    ) {
        return (tile, ctx) -> getDrawer.apply(tile).interact(ctx);
    }

    @Override
    public Schematic getSchematic() {
        return schematic;
    }
}
