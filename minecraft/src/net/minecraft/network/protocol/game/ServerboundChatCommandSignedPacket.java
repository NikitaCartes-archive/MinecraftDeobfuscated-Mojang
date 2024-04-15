package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundChatCommandSignedPacket(
	String command, Instant timeStamp, long salt, ArgumentSignatures argumentSignatures, LastSeenMessages.Update lastSeenMessages
) implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundChatCommandSignedPacket> STREAM_CODEC = Packet.codec(
		ServerboundChatCommandSignedPacket::write, ServerboundChatCommandSignedPacket::new
	);

	private ServerboundChatCommandSignedPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readUtf(),
			friendlyByteBuf.readInstant(),
			friendlyByteBuf.readLong(),
			new ArgumentSignatures(friendlyByteBuf),
			new LastSeenMessages.Update(friendlyByteBuf)
		);
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.command);
		friendlyByteBuf.writeInstant(this.timeStamp);
		friendlyByteBuf.writeLong(this.salt);
		this.argumentSignatures.write(friendlyByteBuf);
		this.lastSeenMessages.write(friendlyByteBuf);
	}

	@Override
	public PacketType<ServerboundChatCommandSignedPacket> type() {
		return GamePacketTypes.SERVERBOUND_CHAT_COMMAND_SIGNED;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleSignedChatCommand(this);
	}
}
