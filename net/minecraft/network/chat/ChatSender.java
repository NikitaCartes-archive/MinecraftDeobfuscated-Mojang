/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public record ChatSender(UUID profileId, Component name, @Nullable Component targetName) {
    public ChatSender(UUID uUID, Component component) {
        this(uUID, component, null);
    }

    public ChatSender(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readUUID(), friendlyByteBuf.readComponent(), (Component)friendlyByteBuf.readNullable(FriendlyByteBuf::readComponent));
    }

    public static ChatSender system(Component component) {
        return new ChatSender(Util.NIL_UUID, component);
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUUID(this.profileId);
        friendlyByteBuf.writeComponent(this.name);
        friendlyByteBuf.writeNullable(this.targetName, FriendlyByteBuf::writeComponent);
    }

    public ChatSender withTargetName(Component component) {
        return new ChatSender(this.profileId, this.name, component);
    }

    public ChatSender toSystem() {
        return new ChatSender(Util.NIL_UUID, this.name, this.targetName);
    }

    public boolean isPlayer() {
        return !this.profileId.equals(Util.NIL_UUID);
    }

    @Nullable
    public Component targetName() {
        return this.targetName;
    }
}

