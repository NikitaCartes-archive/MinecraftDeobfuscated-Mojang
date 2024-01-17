package net.minecraft.network.protocol.common;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundResourcePackPacket(UUID id, ServerboundResourcePackPacket.Action action) implements Packet<ServerCommonPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundResourcePackPacket> STREAM_CODEC = Packet.codec(
		ServerboundResourcePackPacket::write, ServerboundResourcePackPacket::new
	);

	private ServerboundResourcePackPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readUUID(), friendlyByteBuf.readEnum(ServerboundResourcePackPacket.Action.class));
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUUID(this.id);
		friendlyByteBuf.writeEnum(this.action);
	}

	@Override
	public PacketType<ServerboundResourcePackPacket> type() {
		return CommonPacketTypes.SERVERBOUND_RESOURCE_PACK;
	}

	public void handle(ServerCommonPacketListener serverCommonPacketListener) {
		serverCommonPacketListener.handleResourcePackResponse(this);
	}

	public static enum Action {
		SUCCESSFULLY_LOADED,
		DECLINED,
		FAILED_DOWNLOAD,
		ACCEPTED,
		DOWNLOADED,
		INVALID_URL,
		FAILED_RELOAD,
		DISCARDED;

		public boolean isTerminal() {
			return this != ACCEPTED && this != DOWNLOADED;
		}
	}
}
