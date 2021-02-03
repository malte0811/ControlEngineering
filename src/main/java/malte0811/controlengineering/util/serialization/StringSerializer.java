package malte0811.controlengineering.util.serialization;

import com.google.common.base.Preconditions;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundNBT;

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

    public DataResult<T> fromString(List<String> input) {
        List<Object> arguments = new ArrayList<>(fields.size());
        int inputIndex = 0;
        for (StringSerializableField<T, ?> field : fields) {
            int length = field.getCodec().numTokens();
            if (inputIndex + length > input.size()) {
                return DataResult.error("Not enough tokens");
            }
            List<String> subArray = input.subList(inputIndex, inputIndex + length);
            DataResult<?> nextOpt = field.getCodec().fromString(subArray);
            Optional<? extends DataResult.PartialResult<?>> nextError = nextOpt.error();
            if (nextError.isPresent()) {
                return DataResult.error("While reading field: " + field.getName() + ": " + nextError.get().message());
            }
            arguments.add(nextOpt.result().get());
            inputIndex += length;
        }
        Preconditions.checkState(inputIndex == input.size());
        return construct(arguments);
    }

    public DataResult<T> fromNBT(CompoundNBT nbt) {
        List<Object> arguments = new ArrayList<>(fields.size());
        for (StringSerializableField<T, ?> field : fields) {
            DataResult<?> fieldValue = field.getCodec().fromNBT(nbt.get(field.getName()));
            if (fieldValue.result().isPresent()) {
                arguments.add(fieldValue.result().get());
            } else {
                return DataResult.error("While parsing " + field.getName() + ": " + fieldValue.error().get().message());
            }
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

    private DataResult<T> construct(List<Object> args) {
        try {
            return DataResult.success((T) constructor.invokeWithArguments(args));
        } catch (Exception x) {
            //TODO is this exception ever good?
            x.printStackTrace();
            return DataResult.error(x.getMessage());
        } catch (Error x) {
            throw x;
        } catch (Throwable last) {
            throw new RuntimeException(last);
        }
    }
}
