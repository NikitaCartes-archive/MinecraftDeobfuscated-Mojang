package net.minecraft.network.protocol.game;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.inventory.MenuType;

public class ClientboundOpenScreenPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenScreenPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.CONTAINER_ID,
		ClientboundOpenScreenPacket::getContainerId,
		ByteBufCodecs.registry(Registries.MENU),
		ClientboundOpenScreenPacket::getType,
		ComponentSerialization.TRUSTED_STREAM_CODEC,
		ClientboundOpenScreenPacket::getTitle,
		ClientboundOpenScreenPacket::new
	);
	private final int containerId;
	private final MenuType<?> type;
	private final Component title;

	public ClientboundOpenScreenPacket(int i, MenuType<?> menuType, Component component) {
		this.containerId = i;
		this.type = menuType;
		this.title = component;
	}

	@Override
	public PacketType<ClientboundOpenScreenPacket> type() {
		return GamePacketTypes.CLIENTBOUND_OPEN_SCREEN;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleOpenScreen(this);
	}

	public int getContainerId() {
		return this.containerId;
	}

	public MenuType<?> getType() {
		return this.type;
	}

	public Component getTitle() {
		return this.title;
	}
}
