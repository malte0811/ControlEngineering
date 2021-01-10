package malte0811.controlengineering.util.serialization;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        List<Object> arguments = new ArrayList<>(fields.size());
        int inputIndex = 0;
        for (StringSerializableField<T, ?> field : fields) {
            int length = field.getCodec().numTokens();
            if (inputIndex + length > input.size()) {
                return null;
            }
            List<String> subArray = input.subList(inputIndex, inputIndex + length);
            Optional<?> nextOpt = field.getCodec().fromString(subArray);
            if (!nextOpt.isPresent()) {
                return null;
            }
            arguments.add(nextOpt.get());
            inputIndex += length;
        }
        Preconditions.checkState(inputIndex == input.size());
        return construct(arguments);
    }

    public Optional<T> fromNBT(CompoundNBT nbt) {
        List<Object> arguments = new ArrayList<>(fields.size());
        for (StringSerializableField<T, ?> field : fields) {
            Optional<?> fieldValue = field.getCodec().fromNBT(nbt.get(field.getName()));
            if (fieldValue.isPresent()) {
                arguments.add(fieldValue.get());
            } else {
                return Optional.empty();
            }
        }
        return Optional.ofNullable(construct(arguments));
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
            //TODO is this exception ever good?
            x.printStackTrace();
            return null;
        } catch (Error x) {
            throw x;
        } catch (Throwable last) {
            throw new RuntimeException(last);
        }
    }
}
