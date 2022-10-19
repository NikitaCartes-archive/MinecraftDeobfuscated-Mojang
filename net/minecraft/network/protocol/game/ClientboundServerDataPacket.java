/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.jetbrains.annotations.Nullable;

public class ClientboundServerDataPacket
implements Packet<ClientGamePacketListener> {
    private final Optional<Component> motd;
    private final Optional<String> iconBase64;
    private final boolean enforcesSecureChat;

    public ClientboundServerDataPacket(@Nullable Component component, @Nullable String string, boolean bl) {
        this.motd = Optional.ofNullable(component);
        this.iconBase64 = Optional.ofNullable(string);
        this.enforcesSecureChat = bl;
    }

    public ClientboundServerDataPacket(FriendlyByteBuf friendlyByteBuf) {
        this.motd = friendlyByteBuf.readOptional(FriendlyByteBuf::readComponent);
        this.iconBase64 = friendlyByteBuf.readOptional(FriendlyByteBuf::readUtf);
        this.enforcesSecureChat = friendlyByteBuf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeOptional(this.motd, FriendlyByteBuf::writeComponent);
        friendlyByteBuf.writeOptional(this.iconBase64, FriendlyByteBuf::writeUtf);
        friendlyByteBuf.writeBoolean(this.enforcesSecureChat);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleServerData(this);
    }

    public Optional<Component> getMotd() {
        return this.motd;
    }

    public Optional<String> getIconBase64() {
        return this.iconBase64;
    }

    public boolean enforcesSecureChat() {
        return this.enforcesSecureChat;
    }
}

