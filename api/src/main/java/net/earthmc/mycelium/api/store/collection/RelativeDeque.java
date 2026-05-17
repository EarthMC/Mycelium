package net.earthmc.mycelium.api.store.collection;

import java.util.Deque;

/**
 * A deque that supports inserting elements relative to other elements in the deque.
 *
 * @param <T> the type of elements maintained by this deque
 */
public interface RelativeDeque<T> extends Deque<T> {
    /**
     * Inserts an element into this deque before the given pivot element.
     *
     * @param insert The element to insert.
     * @param pivot The element to use as the pivot.
     * @return {@code} true if the pivot was found, and this deque changed as an effect of this method.
     * @throws IllegalArgumentException if the pivot is not part of this deque.
     */
    boolean addBefore(final T insert, final T pivot);

    /**
     * Inserts an element into this deque after the given pivot element.
     *
     * @param insert The element to insert.
     * @param pivot The element to use as the pivot.
     * @return {@code} true if the pivot was found, and this deque changed as an effect of this method.
     * @throws IllegalArgumentException if the pivot is not part of this deque.
     */
    boolean addAfter(final T insert, final T pivot);

    /**
     * Returns the index of the element in this deque.
     *
     * @param element The element to find the index of.
     * @return The index of the element in this deque, or -1 if not found.
     */
    long indexOf(final T element);
}
