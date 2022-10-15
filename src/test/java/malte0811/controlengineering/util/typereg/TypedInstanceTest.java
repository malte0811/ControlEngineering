package malte0811.controlengineering.util.typereg;

import com.mojang.datafixers.util.Pair;
import junit.framework.TestCase;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.nbt.Tag;
import org.junit.Assert;

public class TypedInstanceTest extends TestCase {

    public void testCodecResets() {
        final var entryName = ControlEngineering.ceLoc("test");
        Tag nbt;
        {
            TypedRegistry<Type<?>> oldReg = new TypedRegistry<>();
            final var oldCodec = Instance.makeCodec(oldReg);
            Type<Integer> oldType = new Type<>(13, MyCodecs.INTEGER);
            oldReg.register(entryName, oldType);
            nbt = oldCodec.toNBT(oldType.newInstance());
        }
        {
            TypedRegistry<Type<?>> newReg = new TypedRegistry<>();
            final var newCodec = Instance.makeCodec(newReg);
            final var defaultState = Pair.of(17, "foo");
            Type<Pair<Integer, String>> newType = new Type<>(
                    defaultState, MyCodecs.pair(MyCodecs.INTEGER, MyCodecs.STRING)
            );
            newReg.register(entryName, newType);
            final var instance = newCodec.fromNBT(nbt);
            Assert.assertNotNull(instance);
            Assert.assertEquals(instance.getType(), newType);
            Assert.assertEquals(instance.getCurrentState(), defaultState);
        }
    }

    private static class Type<State> extends TypedRegistryEntry<State, Instance<State>> {
        protected Type(State initialState, MyCodec<State> stateCodec) {
            super(initialState, stateCodec);
        }

        @Override
        public Instance<State> newInstance(State state) {
            return new Instance<>(this, state);
        }
    }

    private static class Instance<State> extends TypedInstance<State, Type<State>> {
        public Instance(Type<State> stateType, State currentState) {
            super(stateType, currentState);
        }
    }
}