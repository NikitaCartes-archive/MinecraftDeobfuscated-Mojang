package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundChatCommandPacket(
	String command, Instant timeStamp, long salt, ArgumentSignatures argumentSignatures, LastSeenMessages.Update lastSeenMessages
) implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundChatCommandPacket> STREAM_CODEC = Packet.codec(
		ServerboundChatCommandPacket::write, ServerboundChatCommandPacket::new
	);

	private ServerboundChatCommandPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readUtf(256),
			friendlyByteBuf.readInstant(),
			friendlyByteBuf.readLong(),
			new ArgumentSignatures(friendlyByteBuf),
			new LastSeenMessages.Update(friendlyByteBuf)
		);
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.command, 256);
		friendlyByteBuf.writeInstant(this.timeStamp);
		friendlyByteBuf.writeLong(this.salt);
		this.argumentSignatures.write(friendlyByteBuf);
		this.lastSeenMessages.write(friendlyByteBuf);
	}

	@Override
	public PacketType<ServerboundChatCommandPacket> type() {
		return GamePacketTypes.SERVERBOUND_CHAT_COMMAND;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleChatCommand(this);
	}
}
