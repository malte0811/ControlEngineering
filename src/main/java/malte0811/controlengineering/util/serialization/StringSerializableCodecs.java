package malte0811.controlengineering.util.serialization;

import com.mojang.serialization.Codec;

import java.util.function.Function;

public class StringSerializableCodecs {
    public static final StringSerializableCodec<Integer> INT = StringSerializableCodec.fromCodec(
            Codec.INT, s -> Integer.parseInt(s)
    );
    public static final StringSerializableCodec<Integer> HEX_INT = StringSerializableCodec.fromCodec(
            Codec.INT, s -> Integer.parseInt(s, 16)
    );
    public static final StringSerializableCodec<Boolean> BOOLEAN = StringSerializableCodec.fromCodec(
            Codec.BOOL, Boolean::parseBoolean
    );
    public static final StringSerializableCodec<String> STRING = StringSerializableCodec.fromCodec(
            Codec.STRING, Function.identity()
    );
}
