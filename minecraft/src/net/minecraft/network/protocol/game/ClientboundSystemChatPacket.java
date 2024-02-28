package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundSystemChatPacket(Component content, boolean overlay) implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSystemChatPacket> STREAM_CODEC = StreamCodec.composite(
		ComponentSerialization.TRUSTED_STREAM_CODEC,
		ClientboundSystemChatPacket::content,
		ByteBufCodecs.BOOL,
		ClientboundSystemChatPacket::overlay,
		ClientboundSystemChatPacket::new
	);

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
