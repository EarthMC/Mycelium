package net.earthmc.mycelium.client.redis.collection;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.impl.serialization.RedisCodecs;
import org.jspecify.annotations.NullMarked;

import java.io.Closeable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A wrapper around a redis set that converts the remote data to a usable class using the supplied codec. The remote set will be (de)serialized on each access, guaranteeing that the returned data is up-to-date.
 * <p>
 * After calling {@link #close()}, the underlying connection will be closed and this set will act like an empty set.
 */
@NullMarked
public class RedisRemoteSet<T> extends AbstractSet<T> implements Closeable {
    protected boolean closed;
    protected final String redisKey;
    private final StatefulRedisPubSubConnection<String, T> connection;
    protected final RedisCommands<String, T> commands;

    public RedisRemoteSet(RedisClient client, String redisKey, JsonCodec<T> codec) {
        this.redisKey = redisKey;

        final RedisCodec<String, T> redisCodec = RedisCodecs.codecFor(codec);
        this.connection = client.connectPubSub(redisCodec);

        this.commands = connection.sync();
    }

    public Set<T> getRemote() {
        if (this.closed) {
            return Set.of();
        }

        return this.commands.smembers(this.redisKey);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean add(T element) {
        if (this.closed) {
            return false;
        }

        return this.commands.sadd(this.redisKey, element) == 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object element) {
        if (closed) {
            return false;
        }

        return this.commands.srem(this.redisKey, (T) element) == 1;
    }

    @Override
    public Iterator<T> iterator() {
        return this.getRemote().iterator();
    }

    @Override
    public int size() {
        return this.getRemote().size();
    }

    @Override
    public void close() {
        this.closed = true;
        this.connection.close();
    }
}
