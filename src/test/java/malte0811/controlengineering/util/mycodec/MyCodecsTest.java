package malte0811.controlengineering.util.mycodec;

import junit.framework.TestCase;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;
import net.minecraft.nbt.CompoundTag;
import org.junit.Assert;

public class MyCodecsTest extends TestCase {
    public void testMissingField() {
        record Temp(int a, boolean b) { }
        final MyCodec<Temp> codec = new RecordCodec2<>(
                MyCodecs.INTEGER.fieldOf("a", Temp::a),
                MyCodecs.BOOL.fieldOf("b", Temp::b),
                Temp::new
        );
        CompoundTag tag = new CompoundTag();
        tag.putInt("a", 15);
        final var read = codec.fromNBT(tag);
        Assert.assertNotNull(read);
        Assert.assertEquals(read.a, 15);
        Assert.assertFalse(read.b);
    }
}