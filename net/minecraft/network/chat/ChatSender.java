/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public record ChatSender(UUID uuid, Component name) {
    public ChatSender(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readUUID(), friendlyByteBuf.readComponent());
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(this.uuid);
        friendlyByteBuf.writeComponent(this.name);
    }
}

