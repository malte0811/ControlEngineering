package malte0811.controlengineering.controlpanels.components;

import blusunrize.immersiveengineering.api.IETags;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.ItemWithKeyID;
import malte0811.controlengineering.util.ItemUtil;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.UUID;

public class KeySwitch extends PanelComponentType<BusSignalRef, KeySwitch.State> {
    public static final Vec2d SIZE = new Vec2d(1, 1);
    public static final double HEIGHT = 0.5;

    public KeySwitch() {
        super(
                BusSignalRef.DEFAULT, new State(BaseState.EMPTY, new UUID(0, 0)),
                BusSignalRef.CODEC, State.CODEC,
                SIZE, 0
        );
    }

    @Override
    public Pair<InteractionResult, State> click(BusSignalRef outSignal, State oldState, ComponentClickContext ctx) {
        final var heldStack = ctx.getHeldItem();
        switch (oldState.baseState) {
            case EMPTY -> {
                if (heldStack.is(CEItems.LOCK.get())) {
                    var newState = new State(BaseState.KEY_INSERTED, ItemWithKeyID.getUUID(heldStack));
                    heldStack.shrink(1);
                    return success(newState);
                }
            }
            case HAS_LOCK -> {
                if (heldStack.is(CEItems.KEY.get()) && oldState.lockId().equals(ItemWithKeyID.getUUID(heldStack))) {
                    heldStack.shrink(1);
                    return success(oldState.with(BaseState.KEY_INSERTED));
                }
            }
            case KEY_INSERTED -> {
                if (ctx.player() == null || !ctx.isSneaking()) {
                    return success(oldState.with(BaseState.KEY_TURNED));
                } else if (heldStack.is(IETags.screwdrivers)) {
                    ItemUtil.giveOrDrop(ctx.player(), ItemWithKeyID.create(CEItems.LOCK, oldState.lockId));
                    return success(oldState.with(BaseState.EMPTY));
                } else {
                    ItemUtil.giveOrDrop(ctx.player(), ItemWithKeyID.create(CEItems.KEY, oldState.lockId));
                    return success(oldState.with(BaseState.HAS_LOCK));
                }
            }
            case KEY_TURNED -> {
                return success(oldState.with(BaseState.KEY_INSERTED));
            }
        }
        return pass(oldState);
    }

    @Override
    public BusState getEmittedState(BusSignalRef busSignalRef, State state) {
        if (state.baseState() == BaseState.KEY_TURNED) {
            return BusState.EMPTY.with(busSignalRef, BusLine.MAX_VALID_VALUE);
        } else {
            return BusState.EMPTY;
        }
    }

    @Nullable
    @Override
    public AABB getSelectionShape(State state) {
        if (state.baseState() == BaseState.EMPTY) {
            return new AABB(0, 0, 0, SIZE.x(), 1e-3, SIZE.y());
        } else {
            return new AABB(0, 0, 0, SIZE.x(), HEIGHT, SIZE.y());
        }
    }

    public record State(BaseState baseState, UUID lockId) {
        public static final MyCodec<State> CODEC = new RecordCodec2<>(
                new CodecField<>("baseState", State::baseState, BaseState.CODEC),
                new CodecField<>("lockId", State::lockId, MyCodecs.UUID_CODEC),
                State::new
        );

        public State with(BaseState newBaseState) {
            return new State(newBaseState, lockId());
        }
    }

    public enum BaseState {
        EMPTY, HAS_LOCK, KEY_INSERTED, KEY_TURNED;

        public static final MyCodec<BaseState> CODEC = MyCodecs.forEnum(BaseState.values(), BaseState::ordinal);
    }
}
