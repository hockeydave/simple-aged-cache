package io.collective;

import java.time.Clock;

/**
 * Implement a simple aged cache where entries inserted expire after a time period.
 * The expired entries will not be returned when asked for (similar with size - size will not include
 * expired entries).
 * Given that the course provides little guidance on requirements like performance characteristic.  Their only
 * guidance was that it should take you about 2 hours to implement.  Implementing a Map class in 2 hours would
 * be challenging (i.e. load factor, collisions, rescaling).  I opted to go with a simple doubly linked list. The
 * instructors do mention LinkedList in the README.md file so makes sense to me.  Especially in the context of a
 * cache that expires where the LinkedList won't grow infinitely.  Its size is likely bounded by reasonable timeout
 * values.
 * Code Coverage via tests 88% (toString method is not covered).
 * LinkedList performance O(n) for get/put
 * HashMap performance O(1) for get/put (much better but harder to implement and near impossible in 2 hours)
 *
 * @author dcpeterson
 */
public class SimpleAgedCache<E> {
    private Clock clock;
    ExpirableEntry<E> head;
    ExpirableEntry<E> tail;
    int size;

    /**
     * Constructor with a passed in Clock which will likely be used in testing to force th expiration of entries.
     * Useful for debugging.
     *
     * @param clock Clock class for this aged cache w/ expiry to use instead of the default system clock.
     */
    public SimpleAgedCache(Clock clock) {
        this();
        this.clock = clock;
    }

    /**
     * Normal path constructor to create the simple aged cache, and we use the best system clock available.
     */
    public SimpleAgedCache() {
        head = null;
        tail = null;
        size = 0;
        this.clock = Clock.systemUTC();
    }

    /**
     * Put a value into the cache indexed by the key (think Map) and expire it when retention period expires.
     *
     * @param key               Cache key for the entry.
     * @param value             Cache value for the entry.
     * @param retentionInMillis Time to retain the entry (in milliseconds) in the cache before expiring out.
     */
    public void put(E key, E value, int retentionInMillis) {
        // Unclear if we should disallow a null value, but seems more likely than not.
        if (key == null || value == null) throw new NullPointerException("put key/value of null is disallowed");

        ExpirableEntry<E> node = new ExpirableEntry<>(key, value, retentionInMillis, clock);
        if (head == null) {
            head = node;
            tail = node;
            size++;
        } else {
            ExpirableEntry<E> n = find(key);
            if (n == null) {
                tail.next = node;
                node.prev = tail;
                tail = node;
                size++;
            } else {
                n.setValue(value);
                n.setExpireMillisecond(node.getExpireMillisecond());
            }
        }

    }

    /**
     * Determines if the AgedCache is empty or not
     *
     * @return true if cache is empty and false otherwise
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Determines size of this cache.  There is no remove method, but things will expire out via the clean method.
     *
     * @return number of the cache entries stored.
     */
    public int size() {
        clean();
        return size;
    }

    /**
     * Helper method to find a node within the LinkedList
     *
     * @param key The key to search for
     * @return the value that is associated with this cache key
     */
    private ExpirableEntry<E> find(Object key) {
        ExpirableEntry<E> curr = head;
        while (curr != null && !curr.getKey().equals(key) && !curr.equals(tail)) {
            curr = curr.next;
        }
        if (curr != null && curr.getKey().equals(key)) return curr;
        else return null;
    }

    /**
     * Get a cache entry from the LinkedList if present and not expired.
     *
     * @param key lookup key associated with the cache entry
     * @return Object associated with this key or null if not present
     */
    public Object get(Object key) {
        if (key == null) return null;

        clean();
        ExpirableEntry<E> curr = find(key);
        if (curr != null && curr.getKey().equals(key) && !curr.isExpired(clock)) return curr.getValue();
        return null;
    }


    /**
     * Utility method to clean out expired entries from the cache.
     */
    private void clean() {
        ExpirableEntry<E> node = head;
        while (node != null) {
            if (node.isExpired(clock)) {
                if (node.equals(head)) {
                    head = node.next;
                    node.prev = null;
                } else {
                    node.prev.next = node.next;
                }
                if (node.equals(tail)) {
                    node.next = null;
                } else {
                    node.next.prev = node.prev;
                }

                size--;
            }
            node = node.next;
        }
        if (size == 0) {
            head = null;
            tail = null;
        }
    }
}

/**
 * Cache entry that implements a doubly linked list node and has an expiration for this node.
 *
 * @param <E> Supports storing any class of object as key or value.
 */
class ExpirableEntry<E> {
    ExpirableEntry<E> prev;
    ExpirableEntry<E> next;
    E key;
    E value;
    long expireMillisecond;


    public ExpirableEntry() {
        this.prev = null;
        this.next = null;
    }

    public ExpirableEntry(E key, E value, int retentionInMillis, Clock clock) {
        this();
        this.key = key;
        this.value = value;
        this.expireMillisecond = clock.millis() + retentionInMillis;
    }

    public boolean isExpired(Clock clock) {
        return clock.millis() > expireMillisecond;
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(E value) {
        this.value = value;
    }

    public long getExpireMillisecond() {
        return expireMillisecond;
    }

    public void setExpireMillisecond(long expireMillisecond) {
        this.expireMillisecond = expireMillisecond;
    }

    /**
     * override the toString method of class.
     *
     * @return String representation of this node
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("this key/value ").append(key).append("/").append(value);
        if (this.prev != null) sb.append(" prev key/value").append(this.prev.key).append("/").append(this.prev.value);
        if (this.next != null) sb.append(" next key/value").append(this.next.key).append("/").append(this.next.value);

        return sb.toString();
    }
}