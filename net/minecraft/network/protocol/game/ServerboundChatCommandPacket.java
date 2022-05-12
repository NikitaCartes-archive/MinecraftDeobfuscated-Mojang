/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.time.Instant;
import java.util.UUID;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.util.StringUtil;

public record ServerboundChatCommandPacket(String command, Instant timeStamp, ArgumentSignatures argumentSignatures) implements Packet<ServerGamePacketListener>
{
    private static final int MAX_MESSAGE_LENGTH = 256;

    public ServerboundChatCommandPacket(String string, Instant instant, ArgumentSignatures argumentSignatures) {
        this.command = StringUtil.trimChatMessage(string);
        this.timeStamp = instant;
        this.argumentSignatures = argumentSignatures;
    }

    public ServerboundChatCommandPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readUtf(256), friendlyByteBuf.readInstant(), new ArgumentSignatures(friendlyByteBuf));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(this.command);
        friendlyByteBuf.writeInstant(this.timeStamp);
        this.argumentSignatures.write(friendlyByteBuf);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleChatCommand(this);
    }

    private Instant getExpiresAt() {
        return this.timeStamp.plus(ServerboundChatPacket.MESSAGE_EXPIRES_AFTER);
    }

    public boolean hasExpired(Instant instant) {
        return instant.isAfter(this.getExpiresAt());
    }

    public CommandSigningContext signingContext(UUID uUID) {
        return new CommandSigningContext.PlainArguments(uUID, this.timeStamp, this.argumentSignatures);
    }
}

