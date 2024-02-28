package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundDisguisedChatPacket(Component message, ChatType.Bound chatType) implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDisguisedChatPacket> STREAM_CODEC = StreamCodec.composite(
		ComponentSerialization.TRUSTED_STREAM_CODEC,
		ClientboundDisguisedChatPacket::message,
		ChatType.Bound.STREAM_CODEC,
		ClientboundDisguisedChatPacket::chatType,
		ClientboundDisguisedChatPacket::new
	);

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
