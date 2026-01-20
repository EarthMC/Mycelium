package net.earthmc.mycelium.client.impl.messaging;

import net.earthmc.mycelium.api.messaging.MessageSender;

public class MessageSenderImpl implements MessageSender {
    private final boolean isSelf;

    public MessageSenderImpl(boolean isSelf) {
        this.isSelf = isSelf;
    }

    @Override
    public boolean isSelf() {
        return this.isSelf;
    }
}
