package net.earthmc.mycelium.api.event;

import java.util.function.Consumer;

public interface Events {
    <T extends Event> RegisteredEvent registerEvent(Class<T> eventClass, Consumer<T> listener);

    void broadcast(final Event event);
}
