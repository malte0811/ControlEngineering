package malte0811.controlengineering.util.serialization;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringSerializer<T> {
    private final MethodHandle constructor;
    private final List<StringSerializableField<T, ?>> fields;

    @SafeVarargs
    public StringSerializer(MethodHandle constructor, StringSerializableField<T, ?>... fields) {
        this.constructor = constructor;
        this.fields = Arrays.asList(fields);
    }

    @Nullable
    public T fromString(List<String> input) {
        try {
            List<Object> arguments = new ArrayList<>(fields.size());
            int inputIndex = 0;
            for (StringSerializableField<T, ?> field : fields) {
                int length = field.getCodec().numTokens();
                List<String> subArray = input.subList(inputIndex, inputIndex + length);
                arguments.add(field.getCodec().fromString(subArray));
                inputIndex += length;
            }
            Preconditions.checkState(inputIndex == input.size());
            return construct(arguments);
        } catch (Exception e) {
            return null;
        }
    }

    public T fromNBT(CompoundNBT nbt) {
        List<Object> arguments = new ArrayList<>(fields.size());
        for (StringSerializableField<T, ?> field : fields) {
            arguments.add(field.getCodec().fromNBT(nbt.get(field.getName())));
        }
        return construct(arguments);
    }

    public CompoundNBT toNBT(T input) {
        CompoundNBT nbt = new CompoundNBT();
        for (StringSerializableField<T, ?> field : fields) {
            nbt.put(field.getName(), field.toNBT(input));
        }
        return nbt;
    }

    private T construct(List<Object> args) {
        try {
            return (T) constructor.invokeWithArguments(args);
        } catch (Exception x) {
            return null;
        } catch (Error x) {
            throw x;
        } catch (Throwable last) {
            throw new RuntimeException(last);
        }
    }
}
