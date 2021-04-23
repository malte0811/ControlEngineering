package malte0811.controlengineering.tiles.panels;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.blocks.panels.PanelCNCBlock;
import malte0811.controlengineering.blocks.shapes.ListShapes;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.cnc.CNCInstructionParser;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.PanelTopItem;
import malte0811.controlengineering.items.PunchedTapeItem;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.util.BitUtils;
import malte0811.controlengineering.util.CachedValue;
import malte0811.controlengineering.util.math.Matrix4;
import malte0811.controlengineering.util.serialization.NBTIO;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.util.Constants.BlockFlags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static malte0811.controlengineering.util.ShapeUtils.createPixelRelative;

public class PanelCNCTile extends TileEntity implements SelectionShapeOwner, ITickableTileEntity {
    @Nonnull
    private byte[] insertedTape = new byte[0];
    private final CachedValue<byte[], CNCJob> currentJob = new CachedValue<>(
            () -> insertedTape,
            tape -> {
                if (tape.length > 0) {
                    return CNCJob.createFor(CNCInstructionParser.parse(BitUtils.toString(tape)));
                } else {
                    return null;
                }
            },
            Arrays::equals
    );
    private int currentTicksInJob;
    private boolean hasPanel;
    private final List<PlacedComponent> currentPlacedComponents = new ArrayList<>();

    public PanelCNCTile() {
        super(CETileEntities.PANEL_CNC.get());
    }

    private final CachedValue<Direction, SelectionShapes> selectionShapes = new CachedValue<>(
            () -> getBlockState().get(PanelCNCBlock.FACING),
            facing -> new ListShapes(
                    PanelCNCBlock.SHAPE,
                    new Matrix4(facing),
                    ImmutableList.of(
                            new SingleShape(
                                    createPixelRelative(1, 0, 1, 15, 2, 15),
                                    this::bottomClick
                            ),
                            new SingleShape(
                                    createPixelRelative(2, 14, 4, 14, 16, 12),
                                    this::topClick
                            )
                    ),
                    ctx -> ActionResultType.PASS
            )
    );

    private ActionResultType bottomClick(ItemUseContext ctx) {
        if (world == null) {
            return ActionResultType.PASS;
        }
        if (hasPanel()) {
            if (!hasFinishedJob()) {
                return ActionResultType.FAIL;
            }
            if (!world.isRemote) {
                ItemStack result = PanelTopItem.createWithComponents(currentPlacedComponents);
                Vector3d dropPos = Vector3d.copyCentered(pos);
                InventoryHelper.spawnItemStack(world, dropPos.x, dropPos.y, dropPos.z, result);
                hasPanel = false;
                currentPlacedComponents.clear();
                currentTicksInJob = 0;
                world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), BlockFlags.DEFAULT);
            }
            return ActionResultType.SUCCESS;
        } else {
            ItemStack heldItem = ctx.getItem();
            if (PanelTopItem.isEmptyPanelTop(heldItem)) {
                if (!world.isRemote) {
                    hasPanel = true;
                    world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), BlockFlags.DEFAULT);
                    PlayerEntity player = ctx.getPlayer();
                    if (player == null || !player.abilities.isCreativeMode) {
                        heldItem.shrink(1);
                    }
                }
                return ActionResultType.SUCCESS;
            } else {
                return ActionResultType.FAIL;
            }
        }
    }

    private ActionResultType topClick(ItemUseContext ctx) {
        if (isJobRunning()) {
            return ActionResultType.FAIL;
        }
        final ItemStack held = ctx.getItem();
        if (insertedTape.length == 0) {
            if (CEItems.PUNCHED_TAPE.get() == held.getItem()) {
                if (!world.isRemote) {
                    insertedTape = PunchedTapeItem.getBytes(held);
                    held.shrink(1);
                    world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), BlockFlags.DEFAULT);
                }
                return ActionResultType.SUCCESS;
            }
        } else if (!hasPanel()) {
            if (!world.isRemote) {
                ItemStack result = PunchedTapeItem.withBytes(insertedTape);
                insertedTape = new byte[0];
                Vector3d dropPos = Vector3d.copyCentered(pos);
                InventoryHelper.spawnItemStack(world, dropPos.x, dropPos.y, dropPos.z, result);
                world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), BlockFlags.DEFAULT);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    @Override
    public void tick() {
        if (isJobRunning()) {
            ++currentTicksInJob;
            int nextComponent = currentPlacedComponents.size();
            CNCJob job = currentJob.get();
            if (nextComponent < job.getTotalComponents()) {
                if (currentTicksInJob >= job.getTickPlacingComponent().getInt(nextComponent)) {
                    currentPlacedComponents.add(job.getComponents().get(nextComponent));
                }
            }
        }
    }

    private boolean hasFinishedJob() {
        CNCJob job = currentJob.get();
        return job != null && currentTicksInJob >= job.getTotalTicks();
    }

    private boolean isJobRunning() {
        CNCJob job = currentJob.get();
        return hasPanel && job != null && currentTicksInJob < job.getTotalTicks();
    }

    @Override
    public SelectionShapes getShape() {
        return selectionShapes.get();
    }

    @Nullable
    public CNCJob getCurrentJob() {
        return currentJob.get();
    }

    public int getTapeLength() {
        return insertedTape.length;
    }

    public int getCurrentTicksInJob() {
        return currentTicksInJob;
    }

    public List<PlacedComponent> getCurrentPlacedComponents() {
        return currentPlacedComponents;
    }

    public boolean hasPanel() {
        return hasPanel;
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        super.write(compound);
        serialize(NBTIO.writer(compound));
        return compound;
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
        super.read(state, nbt);
        serialize(NBTIO.reader(nbt));
        currentPlacedComponents.clear();
        CNCJob job = currentJob.get();
        if (job != null) {
            int numComponents = 0;
            while (numComponents < job.getTotalComponents() &&
                    job.getTickPlacingComponent().getInt(numComponents) < currentTicksInJob) {
                currentPlacedComponents.add(job.getComponents().get(numComponents));
                ++numComponents;
            }
        }
    }

    private void serialize(NBTIO io) {
        insertedTape = io.handle("tape", insertedTape);
        currentTicksInJob = io.handle("currentTick", currentTicksInJob);
        hasPanel = io.handle("hasPanel", hasPanel);
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(pos, -1, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(getBlockState(), pkt.getNbtCompound());
    }
}
