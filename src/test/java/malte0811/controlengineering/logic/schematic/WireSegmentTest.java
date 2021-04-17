package malte0811.controlengineering.logic.schematic;

import malte0811.controlengineering.util.math.Vec2i;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class WireSegmentTest {
    private static final WireSegment TEST_SEGMENT = new WireSegment(new Vec2i(1, 2), 5, WireSegment.WireAxis.X);

    @Test
    public void testContains() {
        assertTrue(TEST_SEGMENT.containsOpen(new Vec2i(2, 2)));
        assertFalse(TEST_SEGMENT.containsOpen(new Vec2i(2, 3)));
        assertFalse(TEST_SEGMENT.containsOpen(new Vec2i(1, 2)));
        assertTrue(TEST_SEGMENT.containsClosed(new Vec2i(1, 2)));
    }

    @Test
    public void testSplit() {
        Vec2i center = new Vec2i(2, 2);
        List<WireSegment> splitResult = TEST_SEGMENT.splitAt(center);
        assertEquals(TEST_SEGMENT.getStart(), splitResult.get(0).getStart());
        assertEquals(center, splitResult.get(0).getEnd());
        assertEquals(center, splitResult.get(1).getStart());
        assertEquals(TEST_SEGMENT.getEnd(), splitResult.get(1).getEnd());
    }
}