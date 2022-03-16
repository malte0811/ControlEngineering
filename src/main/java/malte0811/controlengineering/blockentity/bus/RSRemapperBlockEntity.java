package malte0811.controlengineering.blockentity.bus;

import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.LocalBusHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class RSRemapperBlockEntity extends DualConnectorBlockEntity implements IRedstoneConnector {
    private static final int COLOR_ID = MIN_ID;
    public static final int NOT_MAPPED = BusLine.LINE_SIZE + 1;

    private int[] colorToGray = makeInitialMapping();
    private int[] grayToColor = makeInverseMapping(colorToGray);
    private final byte[][] lastInputByPoint = new byte[2][BusLine.LINE_SIZE];
    private final boolean[] needsUpdate = {false, false};

    public RSRemapperBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        var newColorToGray = nbt.getIntArray("colorToGray");
        if (newColorToGray.length != BusLine.LINE_SIZE) {
            newColorToGray = makeInitialMapping();
        }
        setColorToGray(newColorToGray);
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putIntArray("colorToGray", colorToGray);
    }

    @Override
    public LocalWireNetwork getLocalNet(int cpIndex) {
        return super.getLocalNet(cpIndex);
    }

    /*GENERAL IIC*/
    @Override
    public boolean canConnectCable(WireType wireType, ConnectionPoint connectionPoint, Vec3i offset) {
        return countRealWiresAt(connectionPoint) == 0 && wireType.getCategory().equals(WireType.REDSTONE_CATEGORY);
    }

    /*REDSTONE*/
    @Override
    public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler) {
        var netHere = getNet(cp);
        if (netHere == null) {
            return;
        }
        var netOther = getOtherNet(cp);
        if (netOther == null || (netOther == netHere && cp.index() != COLOR_ID)) {
            return;
        }
        var inputSignals = netHere.getValuesExcluding(cp);
        if (Arrays.equals(inputSignals, lastInputByPoint[cp.index()])) {
            return;
        }
        System.arraycopy(inputSignals, 0, lastInputByPoint[cp.index()], 0, BusLine.LINE_SIZE);
        needsUpdate[1 - cp.index()] = true;
        Objects.requireNonNull(level).scheduleTick(worldPosition, getBlockState().getBlock(), 1);
    }

    @Override
    public void updateInput(byte[] signals, ConnectionPoint cp) {
        var otherNet = getOtherNet(cp);
        var thisNet = getNet(cp);
        if (otherNet == null || thisNet == null) {
            return;
        } else if (otherNet == thisNet) {
            updateInputsShorted(signals, cp);
            return;
        }
        var otherToThis = cp.index() == COLOR_ID ? grayToColor : colorToGray;
        var inputs = lastInputByPoint[1 - cp.index()];
        for (int otherIndex = 0; otherIndex < inputs.length; ++otherIndex) {
            var thisIndex = otherToThis[otherIndex];
            if (thisIndex != NOT_MAPPED) {
                signals[thisIndex] = (byte) Math.max(inputs[otherIndex], signals[thisIndex]);
            }
        }
    }

    public void onBlockTick() {
        for (int i = 0; i < 2; ++i) {
            if (!needsUpdate[i]) {
                continue;
            }
            var net = getNet(new ConnectionPoint(worldPosition, i));
            if (net != null) {
                net.updateValues();
            }
            needsUpdate[i] = false;
        }
    }

    private void updateInputsShorted(byte[] out, ConnectionPoint cp) {
        if (cp.index() != COLOR_ID) {
            return;
        }
        byte[] totalSignal = Arrays.copyOf(lastInputByPoint[COLOR_ID], BusLine.LINE_SIZE);
        boolean changed;
        do {
            changed = false;
            for (var mapping : List.of(colorToGray, grayToColor)) {
                for (int i = 0; i < BusLine.LINE_SIZE; ++i) {
                    var outIndex = mapping[i];
                    if (outIndex == NOT_MAPPED) {
                        continue;
                    }
                    if (totalSignal[i] > out[outIndex]) {
                        totalSignal[outIndex] = out[outIndex] = totalSignal[i];
                        changed = true;
                    }
                }
            }
        } while (changed);
    }

    @Nullable
    private RedstoneNetworkHandler getOtherNet(ConnectionPoint cp) {
        return getNet(getOtherPoint(cp));
    }

    @Nullable
    private RedstoneNetworkHandler getNet(ConnectionPoint cp) {
        return getLocalNet(cp)
                .getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class);
    }

    @Override
    public Collection<ResourceLocation> getRequestedHandlers() {
        return ImmutableList.of(LocalBusHandler.NAME, RedstoneNetworkHandler.ID);
    }

    public int[] getColorToGray() {
        return colorToGray;
    }

    public void setColorToGray(int[] newColorToGray) {
        this.colorToGray = newColorToGray;
        this.grayToColor = makeInverseMapping(colorToGray);
    }

    private static int[] makeInitialMapping() {
        int[] result = new int[BusLine.LINE_SIZE];
        for (int i = 0; i < BusLine.LINE_SIZE; ++i) {
            result[i] = i;
        }
        return result;
    }

    private static int[] makeInverseMapping(int[] mapping) {
        int[] result = new int[mapping.length];
        Arrays.fill(result, NOT_MAPPED);
        for (int i = 0; i < mapping.length; ++i) {
            if (mapping[i] < result.length) {
                result[mapping[i]] = i;
            }
        }
        return result;
    }
}
