package net.minecraft.network.protocol.game;

import java.time.Instant;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundChatPacket(String message, Instant timeStamp, long salt, @Nullable MessageSignature signature, LastSeenMessages.Update lastSeenMessages)
	implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundChatPacket> STREAM_CODEC = Packet.codec(ServerboundChatPacket::write, ServerboundChatPacket::new);

	private ServerboundChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readUtf(256),
			friendlyByteBuf.readInstant(),
			friendlyByteBuf.readLong(),
			friendlyByteBuf.readNullable(MessageSignature::read),
			new LastSeenMessages.Update(friendlyByteBuf)
		);
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.message, 256);
		friendlyByteBuf.writeInstant(this.timeStamp);
		friendlyByteBuf.writeLong(this.salt);
		friendlyByteBuf.writeNullable(this.signature, MessageSignature::write);
		this.lastSeenMessages.write(friendlyByteBuf);
	}

	@Override
	public PacketType<ServerboundChatPacket> type() {
		return GamePacketTypes.SERVERBOUND_CHAT;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleChat(this);
	}
}
