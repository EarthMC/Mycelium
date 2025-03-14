package net.earthmc.mycelium.client.redis.collection;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import org.jspecify.annotations.NullMarked;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements a local set that is updated with redis, and updates when remote changes are detected.
 * <p>
 * Intended for cases where the number of reads to the set are greater than the number of writes.
 * <p>
 * Requires redis keyspace events to be enabled in order to be notified of changes to the remote set.
 */
@NullMarked
public class RedisMirroredSet<T> extends RedisRemoteSet<T> {
    private Set<T> backing = ConcurrentHashMap.newKeySet();

    private final StatefulRedisPubSubConnection<String, String> keyspaceConnection;

    public RedisMirroredSet(RedisClient client, String redisKey, JsonCodec<T> codec) {
        super(client, redisKey, codec);
        this.keyspaceConnection = client.connectPubSub();

        sync();

        // TODO: keyspace events don't seem functional even after changing the config
        this.keyspaceConnection.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channel, String data) {
                if (channel.equals("__keyspace@0__:" + redisKey)) {
                    sync();
                }
            }
        });

        this.keyspaceConnection.sync().subscribe("__keyspace@0__:" + redisKey);
    }

    @Override
    public Set<T> getRemote() {
        return this.backing;
    }

    public void sync() {
        Set<T> updatedSet = ConcurrentHashMap.newKeySet();
        updatedSet.addAll(super.commands.smembers(super.redisKey));

        this.backing = updatedSet;
    }

    @Override
    public boolean add(T element) {
        if (super.closed) {
            return false;
        }

        super.add(element);
        return this.backing.add(element);
    }

    @Override
    public boolean remove(Object element) {
        if (super.closed) {
            return false;
        }

        super.remove(element);
        return this.backing.remove(element);
    }

    @Override
    public Iterator<T> iterator() {
        return backing.iterator();
    }

    @Override
    public int size() {
        return backing.size();
    }

    @Override
    public void close() {
        super.close();
        backing.clear();
        this.keyspaceConnection.close();
    }
}
