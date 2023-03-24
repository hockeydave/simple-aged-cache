# Simple aged cache

An exercise to create a simple aged cache utilizing an `ExpirableEntry` inner class with 
TDD (test drive development).


```java
package io.collective;

import java.time.Clock;

public class SimpleAgedCache {

    public SimpleAgedCache(Clock clock) {
    }

    public SimpleAgedCache() {
    }

    public void put(Object key, Object value, int retentionInMillis) {
    }

    public boolean isEmpty() {
        return false;
    }

    public int size() {
        return 0;
    }

    public Object get(Object key) {
        return null;
    }
}
```


