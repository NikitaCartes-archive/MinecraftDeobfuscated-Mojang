package net.minecraft.network.protocol.login;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;

public record ServerboundHelloPacket(String name, RemoteChatSession.Data chatSession, Optional<UUID> profileId) implements Packet<ServerLoginPacketListener> {
	public ServerboundHelloPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readUtf(16), RemoteChatSession.Data.read(friendlyByteBuf), friendlyByteBuf.readOptional(FriendlyByteBuf::readUUID));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.name, 16);
		RemoteChatSession.Data.write(friendlyByteBuf, this.chatSession);
		friendlyByteBuf.writeOptional(this.profileId, FriendlyByteBuf::writeUUID);
	}

	public void handle(ServerLoginPacketListener serverLoginPacketListener) {
		serverLoginPacketListener.handleHello(this);
	}
}
