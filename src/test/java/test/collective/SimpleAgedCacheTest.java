package test.collective;

import io.collective.SimpleAgedCache;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static junit.framework.TestCase.*;
import static org.junit.Assert.fail;

public class SimpleAgedCacheTest {
    SimpleAgedCache empty = new SimpleAgedCache();
    SimpleAgedCache nonempty = new SimpleAgedCache();

    @Before
    public void before() {
        nonempty.put("aKey", "aValue", 2000);
        nonempty.put("anotherKey", "anotherValue", 4000);
    }

    @Test
    public void isEmpty() {
        assertTrue(empty.isEmpty());
        assertFalse(nonempty.isEmpty());
    }

    @Test
    public void size() {
        assertEquals(0, empty.size());
        assertEquals(2, nonempty.size());
    }

    @Test
    public void get() {
        assertNull(nonempty.get(null));
        assertNull(empty.get(null));

        assertNull(empty.get("aKey"));
        assertEquals("aValue", nonempty.get("aKey"));
        assertEquals("anotherValue", nonempty.get("anotherKey"));
    }
    @Test
    public void put() {
        try {
            empty.put(null, "", 1200);
            fail("Put:  Check null");
        } catch (NullPointerException e) {

        }
        empty.put("1", "A", 1200);
        assertEquals(1, empty.size());
        empty.put("2", "B", 1200);
        assertEquals(2, empty.size());
        // Test the same key overwrites value
        empty.put("2", "C", 1200);
        assertEquals("C", empty.get("2"));
    }

    @Test
    public void getExpired() {
        TestClock clock = new TestClock();

        SimpleAgedCache expired = new SimpleAgedCache(clock);
        expired.put("aKey", "aValue", 2000);
        expired.put("anotherKey", "anotherValue", 4000);

        clock.offset(Duration.ofMillis(3000));

        assertEquals(1, expired.size());
        assertEquals("anotherValue", expired.get("anotherKey"));
        clock.offset(Duration.ofMillis(4000));
        assertEquals(0, expired.size());
        assertNull( expired.get("anotherKey"));

        // Now try reverse order expiration
        clock = new TestClock();
        expired = new SimpleAgedCache(clock);
        expired.put("aKey", "aValue", 4000);
        expired.put("anotherKey", "anotherValue", 2000);
        clock.offset(Duration.ofMillis(3000));

        assertEquals(1, expired.size());
        assertEquals("aValue", expired.get("aKey"));
    }

    static class TestClock extends Clock {
        Duration offset = Duration.ZERO;

        @Override
        public ZoneId getZone() {
            return Clock.systemDefaultZone().getZone();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.offset(Clock.system(zone), offset);
        }

        @Override
        public Instant instant() {
            return Clock.offset(Clock.systemDefaultZone(), offset).instant();
        }

        public void offset(Duration offset) {
            this.offset = offset;
        }
    }
}