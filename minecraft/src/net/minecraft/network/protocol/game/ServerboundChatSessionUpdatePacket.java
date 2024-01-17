package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundChatSessionUpdatePacket(RemoteChatSession.Data chatSession) implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundChatSessionUpdatePacket> STREAM_CODEC = Packet.codec(
		ServerboundChatSessionUpdatePacket::write, ServerboundChatSessionUpdatePacket::new
	);

	private ServerboundChatSessionUpdatePacket(FriendlyByteBuf friendlyByteBuf) {
		this(RemoteChatSession.Data.read(friendlyByteBuf));
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		RemoteChatSession.Data.write(friendlyByteBuf, this.chatSession);
	}

	@Override
	public PacketType<ServerboundChatSessionUpdatePacket> type() {
		return GamePacketTypes.SERVERBOUND_CHAT_SESSION_UPDATE;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleChatSessionUpdate(this);
	}
}
