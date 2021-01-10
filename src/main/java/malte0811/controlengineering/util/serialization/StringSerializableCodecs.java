package malte0811.controlengineering.util.serialization;

import com.mojang.serialization.Codec;

import java.util.function.Function;

public class StringSerializableCodecs {
    public static final StringSerializableCodec<Integer> INT = StringSerializableCodec.fromCodecXcpError(
            Codec.INT, s -> Integer.parseInt(s)
    );
    public static final StringSerializableCodec<Integer> HEX_INT = StringSerializableCodec.fromCodecXcpError(
            Codec.INT, s -> Integer.parseInt(s, 16)
    );
    public static final StringSerializableCodec<Boolean> BOOLEAN = StringSerializableCodec.fromCodecXcpError(
            Codec.BOOL, Boolean::parseBoolean
    );
    public static final StringSerializableCodec<String> STRING = StringSerializableCodec.fromCodecXcpError(
            Codec.STRING, Function.identity()
    );
}
