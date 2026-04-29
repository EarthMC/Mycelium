package net.earthmc.mycelium.api.store.collection;

import java.util.concurrent.BlockingDeque;

/**
 * A deque that supports inserting elements relative to other elements in the deque.
 *
 * @param <T> the type of elements maintained by this deque
 */
public interface BlockingRelativeDeque<T> extends BlockingDeque<T>, RelativeDeque<T> {
}
