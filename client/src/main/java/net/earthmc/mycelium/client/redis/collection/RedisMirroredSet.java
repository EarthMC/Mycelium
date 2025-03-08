package net.earthmc.mycelium.client.redis.collection;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import net.earthmc.mycelium.client.redis.codec.RedisSerializable;
import org.jspecify.annotations.NullMarked;

import java.io.Closeable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements a local set that is updated with redis, and updates when remote changes are detected.
 * <p>
 * Intended for cases where the number of reads to the set are greater than the number of writes.
 */
@NullMarked
public class RedisMirroredSet<T> extends AbstractSet<T> implements Closeable {
    private Set<T> backing = ConcurrentHashMap.newKeySet();

    private final String redisKey;
    private final StatefulRedisPubSubConnection<String, T> connection;
    private final RedisCommands<String, T> commands;

    public RedisMirroredSet(RedisClient client, String redisKey, RedisCodec<String, ? extends T> codec) {
        this.redisKey = redisKey;
        this.connection = (StatefulRedisPubSubConnection<String, T>) client.connectPubSub(codec);
        this.commands = connection.sync();

        sync();

        this.connection.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channel, T data) {
                if (channel.equals("__keyspace@0__:" + redisKey)) {
                    System.out.println("Detected change in Redis set: " + redisKey);
                    sync();
                }
            }
        });

        this.connection.sync().psubscribe("__keyspace@0__:" + redisKey);
    }

    public void sync() {
        Set<T> updatedSet = ConcurrentHashMap.newKeySet();
        updatedSet.addAll(this.commands.smembers(redisKey));

        this.backing = updatedSet;
    }

    public boolean add(T element) {
        this.commands.sadd(this.redisKey, element);
        return this.backing.add(element);
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
        this.connection.close();
    }
}
