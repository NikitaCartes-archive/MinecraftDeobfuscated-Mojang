package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundServerDataPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundServerDataPacket> STREAM_CODEC = Packet.codec(
		ClientboundServerDataPacket::write, ClientboundServerDataPacket::new
	);
	private final Component motd;
	private final Optional<byte[]> iconBytes;

	public ClientboundServerDataPacket(Component component, Optional<byte[]> optional) {
		this.motd = component;
		this.iconBytes = optional;
	}

	private ClientboundServerDataPacket(FriendlyByteBuf friendlyByteBuf) {
		this.motd = friendlyByteBuf.readComponentTrusted();
		this.iconBytes = friendlyByteBuf.readOptional(FriendlyByteBuf::readByteArray);
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.motd);
		friendlyByteBuf.writeOptional(this.iconBytes, FriendlyByteBuf::writeByteArray);
	}

	@Override
	public PacketType<ClientboundServerDataPacket> type() {
		return GamePacketTypes.CLIENTBOUND_SERVER_DATA;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleServerData(this);
	}

	public Component getMotd() {
		return this.motd;
	}

	public Optional<byte[]> getIconBytes() {
		return this.iconBytes;
	}
}
