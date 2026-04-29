package net.earthmc.mycelium.client.redis.collection;

import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.api.store.collection.BlockingRelativeDeque;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.serialization.RedisCodec;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import redis.clients.jedis.args.ListPosition;
import redis.clients.jedis.util.KeyValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

/**
 * A deque implementation that's backed by a redis list.
 *
 * @param <T> the type of elements maintained by this deque
 */
@NullMarked
@SuppressWarnings("unchecked")
public class RedisRemoteDeque<T> implements BlockingRelativeDeque<T> {
    private final MyceliumClient client;
    private final String redisKey;
    private final RedisCodec<T> codec;

    public RedisRemoteDeque(final MyceliumClient client, final String redisKey, final JsonCodec<T> codec) {
        this.client = client;
        this.redisKey = redisKey;
        this.codec = RedisCodec.codecFor(codec);
    }

    // Queue/deque methods

    @Override
    public void addFirst(T t) {
        this.client.redis().lpush(this.redisKey, this.codec.serialize(t));
    }

    @Override
    public void addLast(T t) {
        this.client.redis().rpush(this.redisKey, this.codec.serialize(t));
    }

    @Override
    public boolean offerFirst(T t) {
        addFirst(t);
        return true;
    }

    @Override
    public boolean offerLast(T t) {
        addLast(t);
        return true;
    }

    @Override
    public T removeFirst() {
        return throwIfNull(pollFirst());
    }

    @Override
    public T removeLast() {
        return throwIfNull(pollLast());
    }

    @Override
    public @Nullable T pollFirst() {
        return deserializeOrNull(this.client.redis().lpop(this.redisKey));
    }

    @Override
    public @Nullable T pollLast() {
        return deserializeOrNull(this.client.redis().rpop(this.redisKey));
    }

    @Override
    public T getFirst() {
        return throwIfNull(peekFirst());
    }

    @Override
    public T getLast() {
        return throwIfNull(peekLast());
    }

    @Override
    public @Nullable T peekFirst() {
        return deserializeOrNull(this.client.redis().lindex(this.redisKey, 0));
    }

    @Override
    public @Nullable T peekLast() {
        return deserializeOrNull(this.client.redis().lindex(this.redisKey, -1));
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return this.client.redis().lrem(this.redisKey, 1, this.codec.serialize((T) o)) > 0;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return this.client.redis().lrem(this.redisKey, -1, this.codec.serialize((T) o)) > 0;
    }

    @Override
    public boolean add(T t) {
        addLast(t);
        return true;
    }

    @Override
    public boolean offer(T t) {
        return offerLast(t);
    }

    @Override
    public T remove() {
        return removeFirst();
    }

    @Override
    public @Nullable T poll() {
        return pollFirst();
    }

    @Override
    public int remainingCapacity() {
        return 0;
    }

    @Override
    public T element() {
        return getFirst();
    }

    @Override
    public @Nullable T peek() {
        return peekFirst();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        final String[] serialized = c.stream().map(this.codec::serialize).toArray(String[]::new);
        this.client.redis().rpush(this.redisKey, serialized);
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;

        for (final Object element : c) {
            modified |= remove(element);
        }

        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;

        for (final T element : getRemote(0, -1)) {
            if (!c.contains(element)) {
                modified |= remove(element);
            }
        }

        return modified;
    }

    @Override
    public void clear() {
        this.client.redis().del(this.redisKey);
    }

    @Override
    public void push(T t) {
        addFirst(t);
    }

    @Override
    public T pop() {
        return removeFirst();
    }

    @Override
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (final Object element : c) {
            if (!contains(element)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean contains(Object o) {
        return this.client.redis().lpos(this.redisKey, this.codec.serialize((T) o)) != null;
    }

    @Override
    public int size() {
        return Math.toIntExact(this.client.redis().llen(this.redisKey));
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    // Blocking deque methods

    @Override
    public void putFirst(T t) {
        addFirst(t);
    }

    @Override
    public void putLast(T t) {
        addLast(t);
    }

    @Override
    public boolean offerFirst(T t, long timeout, TimeUnit unit) {
        return offerFirst(t);
    }

    @Override
    public boolean offerLast(T t, long timeout, TimeUnit unit) {
        return offerLast(t);
    }

    @Override
    public T take() {
        return takeFirst();
    }

    @Override
    public @Nullable T poll(long timeout, TimeUnit unit) {
        return pollFirst(timeout, unit);
    }

    @Override
    public T takeFirst() {
        return this.codec.deserialize(this.client.redis().blpop(0, this.redisKey).get(1));
    }

    @Override
    public T takeLast() {
        return this.codec.deserialize(this.client.redis().brpop(0, this.redisKey).get(1));
    }

    @Override
    public @Nullable T pollFirst(long timeout, TimeUnit unit) {
        final KeyValue<String, String> response = this.client.redis().blpop(unit.toSeconds(timeout), this.redisKey);
        return response != null ? this.codec.deserialize(response.getValue()) : null;
    }

    @Override
    public @Nullable T pollLast(long timeout, TimeUnit unit) {
        final KeyValue<String, String> response = this.client.redis().brpop(unit.toSeconds(timeout), this.redisKey);
        return response != null ? this.codec.deserialize(response.getValue()) : null;
    }

    @Override
    public void put(T t) {
        putLast(t);
    }

    @Override
    public boolean offer(T t, long timeout, TimeUnit unit) {
        return offerLast(t);
    }

    @Override
    public int drainTo(Collection<? super T> c) {
        throw new UnsupportedOperationException(); // could possibly be implemented if draining to another instance of this class
    }

    @Override
    public int drainTo(Collection<? super T> c, int maxElements) {
        throw new UnsupportedOperationException();
    }

    // iterators/arrays

    @Override
    public Iterator<T> iterator() {
        return getRemote(0, -1).iterator();
    }

    @Override
    public Object[] toArray() {
        return getRemote(0, -1).toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return getRemote(0, -1).toArray(a);
    }

    @Override
    public Iterator<T> descendingIterator() {
        return getRemote(-1, 0).iterator();
    }

    private List<T> getRemote(int start, int stop) {
        return this.client.redis().lrange(this.redisKey, start, stop).stream().map(this.codec::deserialize).toList();
    }

    private @Nullable T deserializeOrNull(final @Nullable String string) {
        return string != null ? this.codec.deserialize(string) : null;
    }

    private T throwIfNull(final @Nullable T t) {
        if (t == null) {
            throw new NoSuchElementException();
        }

        return t;
    }

    @Override
    public boolean addBefore(final T insert, final T pivot) {
        return linsert(insert, pivot, ListPosition.BEFORE);
    }

    @Override
    public boolean addAfter(final T insert, final T pivot) {
        return linsert(insert, pivot, ListPosition.AFTER);
    }

    private boolean linsert(final T insert, final T pivot, final ListPosition listPosition) {
        final long response = this.client.redis().linsert(this.redisKey, listPosition, this.codec.serialize(pivot), this.codec.serialize(insert));
        if (response == -1) {
            // pivot not found
            throw new IllegalArgumentException("Provided pivot '" + pivot + "' is not an element in this in deque.");
        }

        return response > 0;
    }
}
