package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundDisguisedChatPacket(Component message, ChatType.BoundNetwork chatType) implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundDisguisedChatPacket> STREAM_CODEC = Packet.codec(
		ClientboundDisguisedChatPacket::write, ClientboundDisguisedChatPacket::new
	);

	private ClientboundDisguisedChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readComponentTrusted(), new ChatType.BoundNetwork(friendlyByteBuf));
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.message);
		this.chatType.write(friendlyByteBuf);
	}

	@Override
	public PacketType<ClientboundDisguisedChatPacket> type() {
		return GamePacketTypes.CLIENTBOUND_DISGUISED_CHAT;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleDisguisedChat(this);
	}

	@Override
	public boolean isSkippable() {
		return true;
	}
}
