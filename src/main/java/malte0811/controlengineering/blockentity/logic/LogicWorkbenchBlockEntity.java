package malte0811.controlengineering.blockentity.logic;

import blusunrize.immersiveengineering.api.IETags;
import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.base.CEBlockEntity;
import malte0811.controlengineering.blockentity.base.IHasMaster;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.logic.LogicWorkbenchBlock;
import malte0811.controlengineering.blocks.shapes.ListShapes;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.gui.misc.SyncContainer;
import malte0811.controlengineering.items.IEItemRefs;
import malte0811.controlengineering.items.PCBStackItem;
import malte0811.controlengineering.logic.circuit.BusConnectedCircuit;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.SchematicCircuitConverter;
import malte0811.controlengineering.util.CachedValue;
import malte0811.controlengineering.util.ItemUtil;
import malte0811.controlengineering.util.math.MatrixUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LogicWorkbenchBlockEntity extends CEBlockEntity implements SelectionShapeOwner, ISchematicBE, MenuProvider,
        IHasMaster<LogicWorkbenchBlockEntity> {
    public static final String TUBES_EMPTY_KEY = ControlEngineering.MODID + ".gui.tubesEmpty";
    public static final String WIRES_EMPTY_KEY = ControlEngineering.MODID + ".gui.wiresEmpty";
    public static final String MORE_BOARDS_THAN_MAX = ControlEngineering.MODID + ".gui.moreThanMaxBoards";
    public static final String TOO_FEW_BOARDS_HELD = ControlEngineering.MODID + ".gui.needMoreBoards";
    public static final String TOO_FEW_WIRES = ControlEngineering.MODID + ".gui.needMoreWires";
    public static final String TOO_FEW_TUBES = ControlEngineering.MODID + ".gui.needMoreTubes";

    private static final TagKey<Item> TUBES = IETags.circuitLogic;
    //TODO solder?
    //TODO add utility tag
    private static final TagKey<Item> WIRE = IETags.copperWire;

    private Schematic schematic = new Schematic();
    private final CircuitIngredientDrawer tubeStorage = new CircuitIngredientDrawer(TUBES, TUBES_EMPTY_KEY);
    private final CircuitIngredientDrawer wireStorage = new CircuitIngredientDrawer(WIRE, WIRES_EMPTY_KEY);
    private final CachedValue<BlockState, SelectionShapes> shapes = new CachedValue<>(
            this::getBlockState,
            state -> {
                LogicWorkbenchBlock.Offset offset = state.getValue(LogicWorkbenchBlock.OFFSET);
                Direction facing = state.getValue(LogicWorkbenchBlock.FACING);
                VoxelShape baseShape = LogicWorkbenchBlock.SHAPE.apply(offset, facing);
                if (offset == LogicWorkbenchBlock.Offset.TOP_RIGHT) {
                    Function<UseOnContext, InteractionResult> create = makeInteraction(
                            state, LogicWorkbenchBlockEntity::handleCreationClick
                    );
                    SelectionShapes wireDrawer = makeDrawerShape(
                            state, LogicWorkbenchBlock.WIRE_DRAWER_TOP_RIGHT, be -> be.wireStorage
                    );
                    return new ListShapes(
                            baseShape,
                            MatrixUtils.inverseFacing(facing),
                            ImmutableList.of(new SingleShape(LogicWorkbenchBlock.BURNER, create), wireDrawer),
                            $ -> InteractionResult.PASS
                    );
                } else if (offset == LogicWorkbenchBlock.Offset.TOP_LEFT) {
                    SelectionShapes wireDrawer = makeDrawerShape(
                            state, LogicWorkbenchBlock.WIRE_DRAWER, be -> be.wireStorage
                    );
                    SelectionShapes tubeDrawer = makeDrawerShape(
                            state, LogicWorkbenchBlock.TUBE_DRAWER, be -> be.tubeStorage
                    );
                    return new ListShapes(
                            baseShape,
                            MatrixUtils.inverseFacing(facing),
                            ImmutableList.of(tubeDrawer, wireDrawer),
                            $ -> InteractionResult.PASS
                    );
                } else {
                    return new SingleShape(baseShape, makeInteraction(state, LogicWorkbenchBlockEntity::handleMainClick));
                }
            }
    );

    public LogicWorkbenchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private Function<UseOnContext, InteractionResult> makeInteraction(
            BlockState state, BiFunction<LogicWorkbenchBlockEntity, UseOnContext, InteractionResult> handler
    ) {
        return ctx -> {
            LogicWorkbenchBlockEntity atOrigin = getOrComputeMasterBE(state);
            if (atOrigin != null) {
                return handler.apply(atOrigin, ctx);
            } else {
                return InteractionResult.FAIL;
            }
        };
    }

    @Nullable
    @Override
    public LogicWorkbenchBlockEntity computeMasterBE(BlockState state) {
        if (level == null) {
            return null;
        }
        BlockPos origin = CEBlocks.LOGIC_WORKBENCH.get().getMainBlock(state, this);
        BlockEntity atOrigin = level.getBlockEntity(origin);
        if (atOrigin instanceof LogicWorkbenchBlockEntity originWB) {
            return originWB;
        } else {
            return null;
        }
    }

    @Override
    public SelectionShapes getShape() {
        return shapes.get();
    }

    @Override
    protected void writeSyncedData(CompoundTag out) {
        out.put("tubes", tubeStorage.write());
        out.put("wires", wireStorage.write());
    }

    @Override
    protected void readSyncedData(CompoundTag in) {
        tubeStorage.read(in.getCompound("tubes"));
        wireStorage.read(in.getCompound("wires"));
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        super.saveAdditional(compound);
        compound.put("schematic", Schematic.CODEC.toNBT(schematic));
        writeSyncedData(compound);
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        schematic = Schematic.CODEC.fromNBT(nbt.get("schematic"));
        readSyncedData(nbt);
        if (schematic == null) {
            schematic = new Schematic();
        }
    }

    private InteractionResult handleMainClick(UseOnContext ctx) {
        if (!level.isClientSide) {
            CEBlocks.LOGIC_WORKBENCH.get().openContainer(ctx.getPlayer(), getBlockState(), ctx.getLevel(), worldPosition);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleCreationClick(UseOnContext ctx) {
        if (ctx.getPlayer() == null || ctx.getItemInHand().getItem() != IEItemRefs.CIRCUIT_BOARD.asItem()) {
            return InteractionResult.PASS;
        }
        Optional<BusConnectedCircuit> circuit = SchematicCircuitConverter.toCircuit(schematic);
        if (!circuit.isPresent()) {
            return InteractionResult.FAIL;
        }
        final int numTubes = circuit.get().getNumTubes();
        final int numBoards = LogicCabinetBlockEntity.getNumBoardsFor(numTubes);
        if (numBoards > LogicCabinetBlockEntity.MAX_NUM_BOARDS) {
            ctx.getPlayer().displayClientMessage(
                    new TranslatableComponent(MORE_BOARDS_THAN_MAX, numBoards, LogicCabinetBlockEntity.MAX_NUM_BOARDS),
                    true
            );
            return InteractionResult.FAIL;
        } else if (numBoards > ctx.getItemInHand().getCount()) {
            ctx.getPlayer().displayClientMessage(new TranslatableComponent(TOO_FEW_BOARDS_HELD, numBoards), true);
            return InteractionResult.FAIL;
        }
        final int numWires = circuit.get().getWireLength();
        if (!level.isClientSide) {
            final boolean enoughTubes = tubeStorage.canConsume(numTubes);
            final boolean enoughWires = wireStorage.canConsume(numWires);
            if (enoughTubes && enoughWires) {
                tubeStorage.consume(numTubes);
                wireStorage.consume(numWires);
                ctx.getItemInHand().shrink(numBoards);
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
                ItemUtil.giveOrDrop(ctx.getPlayer(), PCBStackItem.forSchematic(schematic));
            } else if (!enoughTubes) {
                ctx.getPlayer().displayClientMessage(new TranslatableComponent(TOO_FEW_TUBES, numTubes), true);
            } else {
                ctx.getPlayer().displayClientMessage(new TranslatableComponent(TOO_FEW_WIRES, numWires), true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    public CircuitIngredientDrawer getTubeStorage() {
        return tubeStorage;
    }

    public CircuitIngredientDrawer getWireStorage() {
        return wireStorage;
    }

    private SelectionShapes makeDrawerShape(
            BlockState state, VoxelShape shape, Function<LogicWorkbenchBlockEntity, CircuitIngredientDrawer> getDrawer
    ) {
        Function<UseOnContext, InteractionResult> onClick = makeInteraction(
                state,
                (bEntity, ctx) -> {
                    InteractionResult ret = getDrawer.apply(bEntity).interact(ctx);
                    bEntity.level.sendBlockUpdated(
                            bEntity.worldPosition, bEntity.getBlockState(), bEntity.getBlockState(), Block.UPDATE_ALL
                    );
                    return ret;
                }
        );
        return new SingleShape(shape, onClick).setTextGetter(() -> {
            final LogicWorkbenchBlockEntity main = getOrComputeMasterBE(state);
            if (main == null) {
                return null;
            }
            final CircuitIngredientDrawer drawer = getDrawer.apply(main);
            if (drawer.getStored().isEmpty()) {
                return new TranslatableComponent(drawer.getEmptyKey());
            } else {
                return new TextComponent(drawer.getStored().getCount() + " x ")
                        .append(drawer.getStored().getHoverName().getString());
            }
        });
    }

    @Override
    public Schematic getSchematic() {
        return schematic;
    }

    public AvailableIngredients getCosts() {
        return new AvailableIngredients(this);
    }

    @Nonnull
    @Override
    public Component getDisplayName() {
        return TextComponent.EMPTY;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, @Nonnull Inventory pInventory, @Nonnull Player pPlayer) {
        return CEContainers.LOGIC_DESIGN_EDIT.makeNew(pContainerId, this);
    }

    public static class AvailableIngredients {
        private ItemStack availableTubes;
        private ItemStack availableWires;

        public AvailableIngredients(LogicWorkbenchBlockEntity bEntity) {
            this.availableTubes = bEntity.getTubeStorage().getStored();
            this.availableWires = bEntity.getWireStorage().getStored();
        }

        public AvailableIngredients() {
            this.availableTubes = ItemStack.EMPTY;
            this.availableWires = ItemStack.EMPTY;
        }

        public ItemStack getAvailableTubes() {
            return availableTubes;
        }

        public ItemStack getAvailableWires() {
            return availableWires;
        }

        public Slot makeTubeSlot(int id) {
            return SyncContainer.makeSyncSlot(id, s -> availableTubes = s, () -> availableTubes);
        }

        public Slot makeWireSlot(int id) {
            return SyncContainer.makeSyncSlot(id, s -> availableWires = s, () -> availableWires);
        }
    }
}
