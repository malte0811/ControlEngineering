package malte0811.controlengineering.tiles.panels;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import malte0811.controlengineering.blocks.panels.PanelCNCBlock;
import malte0811.controlengineering.blocks.shapes.ListShapes;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.cnc.CNCInstructionParser;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.util.BitUtils;
import malte0811.controlengineering.util.CachedValue;
import malte0811.controlengineering.util.Matrix4;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;
import java.util.List;

import static malte0811.controlengineering.util.ShapeUtils.createPixelRelative;

public class PanelCNCTile extends TileEntity implements SelectionShapeOwner, ITickableTileEntity {
    @Nullable
    private byte[] insertedTape = BitUtils.toBytesWithParity(
            "button 0 0 ff 0 0;button 15 0 ff00 0 0;button 15 15 ff0000 0 0;button 0 15 ffff 0 0"
    );
    private CNCJob currentJob = CNCJob.createFor(CNCInstructionParser.parse(BitUtils.toString(insertedTape)));
    private int currentTicksInJob;

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
        return ActionResultType.PASS;
    }

    private ActionResultType topClick(ItemUseContext ctx) {
        return ActionResultType.PASS;
    }

    @Override
    public void tick() {
        ++currentTicksInJob;
        currentTicksInJob %= currentJob.totalTicks;
    }

    @Override
    public SelectionShapes getShape() {
        return selectionShapes.get();
    }

    @Nullable
    public CNCJob getCurrentJob() {
        return currentJob;
    }

    public int getTapeLength() {
        if (insertedTape != null) {
            return insertedTape.length;
        } else {
            return 0;
        }
    }

    public int getCurrentTicksInJob() {
        return currentTicksInJob;
    }

    public static class CNCJob {
        private final ImmutableList<PlacedComponent> components;
        private final IntList tickPlacingComponent;
        private final IntList tapeProgressAfterComponent;
        private final int totalTicks;

        public static CNCJob createFor(CNCInstructionParser.ParserResult parserData) {
            final int timePerComponent = 40;
            IntList tickEnds = new IntArrayList(parserData.getComponents().size());
            for (int i = 0; i < parserData.getComponentEnds().size(); ++i) {
                tickEnds.add(timePerComponent * (i + 1));
            }
            return new CNCJob(
                    parserData.getComponents(),
                    tickEnds,
                    parserData.getComponentEnds(),
                    timePerComponent * parserData.getComponentEnds().size() + timePerComponent / 2
            );
        }

        public CNCJob(
                ImmutableList<PlacedComponent> components,
                IntList tickPlacingComponent,
                IntList tapeProgressAfterComponent,
                int totalTicks
        ) {
            Preconditions.checkArgument(components.size() == tickPlacingComponent.size());
            Preconditions.checkArgument(components.size() == tapeProgressAfterComponent.size());
            this.components = components;
            this.tickPlacingComponent = tickPlacingComponent;
            this.tapeProgressAfterComponent = tapeProgressAfterComponent;
            this.totalTicks = totalTicks;
        }

        public List<PlacedComponent> getComponentsAtTime(double tick) {
            return components.subList(0, getNumComponentsAt(tick));
        }

        public double getTapeProgressAtTime(double tick) {
            return interpolate(tick, tapeProgressAfterComponent::getInt);
        }

        public double getComponentProgress(double tick) {
            return interpolate(tick, i -> i);
        }

        @Nullable
        public PlacedComponent getNextComponent(double tick) {
            final int alreadyPlaced = getNumComponentsAt(tick);
            if (alreadyPlaced == getTotalComponents()) {
                return null;
            } else {
                return components.get(alreadyPlaced);
            }
        }

        public int getTotalComponents() {
            return components.size();
        }

        public int getTotalTicks() {
            return totalTicks;
        }

        private double interpolate(double tick, Int2IntFunction getValue) {
            if (components.isEmpty()) {
                return 0;
            }
            final int nextToPlace = getNumComponentsAt(tick);
            if (nextToPlace >= components.size()) {
                return getValue.applyAsInt(getTotalComponents() - 1);
            }
            final int nextValue = getValue.applyAsInt(nextToPlace);
            final int nextTick = tickPlacingComponent.getInt(nextToPlace);
            final int lastValue;
            final double lastTick;
            if (nextToPlace > 0) {
                lastValue = getValue.applyAsInt(nextToPlace - 1);
                lastTick = tickPlacingComponent.getInt(nextToPlace - 1);
            } else {
                lastValue = 0;
                lastTick = 0;
            }
            final double deltaT = nextTick - lastTick;
            final double deltaValue = nextValue - lastValue;
            return lastValue + (tick - lastTick) / deltaT * deltaValue;
        }

        private int getNumComponentsAt(double tick) {
            for (int end = 0; end < getTotalComponents(); ++end) {
                if (tickPlacingComponent.getInt(end) > tick) {
                    return end;
                }
            }
            return getTotalComponents();
        }
    }
}
