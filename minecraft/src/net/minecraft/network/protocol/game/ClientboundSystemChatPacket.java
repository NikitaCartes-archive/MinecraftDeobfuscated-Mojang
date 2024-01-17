package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundSystemChatPacket(Component content, boolean overlay) implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundSystemChatPacket> STREAM_CODEC = Packet.codec(
		ClientboundSystemChatPacket::write, ClientboundSystemChatPacket::new
	);

	private ClientboundSystemChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readComponentTrusted(), friendlyByteBuf.readBoolean());
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.content);
		friendlyByteBuf.writeBoolean(this.overlay);
	}

	@Override
	public PacketType<ClientboundSystemChatPacket> type() {
		return GamePacketTypes.CLIENTBOUND_SYSTEM_CHAT;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSystemChat(this);
	}

	@Override
	public boolean isSkippable() {
		return true;
	}
}
