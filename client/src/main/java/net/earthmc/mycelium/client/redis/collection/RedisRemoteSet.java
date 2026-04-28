package net.earthmc.mycelium.client.redis.collection;

import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.serialization.RedisCodec;
import org.jspecify.annotations.NullMarked;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A wrapper around a redis set that converts the remote data to a usable class using the supplied codec. The remote set will be (de)serialized on each access, guaranteeing that the returned data is up-to-date.
 */
@NullMarked
public class RedisRemoteSet<T> extends AbstractSet<T> {
    private final String redisKey;
    private final MyceliumClient client;
    private final RedisCodec<T> codec;

    public RedisRemoteSet(MyceliumClient client, String redisKey, JsonCodec<T> codec) {
        this.client = client;
        this.redisKey = redisKey;

        this.codec = RedisCodec.codecFor(codec);
    }

    public Set<T> getRemote() {
        return this.client.redis().smembers(this.redisKey).stream().map(codec::deserialize).collect(Collectors.toSet());
    }

    @Override
    public boolean add(T element) {
        return this.client.redis().sadd(this.redisKey, this.codec.serialize(element)) == 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object element) {
        return this.client.redis().srem(this.redisKey, this.codec.serialize((T) element)) == 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object element) {
        return this.client.redis().sismember(this.redisKey, this.codec.serialize((T) element));
    }

    @Override
    public Iterator<T> iterator() {
        return this.getRemote().iterator();
    }

    @Override
    public int size() {
        return this.client.redis().smembers(this.redisKey).size();
    }

    @Override
    public void clear() {
        this.client.redis().del(this.redisKey);
    }

    @Override
    public String toString() {
        return "RedisRemoteSet{" +
                "redisKey='" + redisKey + '\'' +
                ", remote=" + this.getRemote() +
                '}';
    }
}
