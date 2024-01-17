package net.minecraft.network.protocol.login;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;
import net.minecraft.network.protocol.login.custom.DiscardedQueryAnswerPayload;

public record ServerboundCustomQueryAnswerPacket(int transactionId, @Nullable CustomQueryAnswerPayload payload) implements Packet<ServerLoginPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundCustomQueryAnswerPacket> STREAM_CODEC = Packet.codec(
		ServerboundCustomQueryAnswerPacket::write, ServerboundCustomQueryAnswerPacket::read
	);
	private static final int MAX_PAYLOAD_SIZE = 1048576;

	private static ServerboundCustomQueryAnswerPacket read(FriendlyByteBuf friendlyByteBuf) {
		int i = friendlyByteBuf.readVarInt();
		return new ServerboundCustomQueryAnswerPacket(i, readPayload(i, friendlyByteBuf));
	}

	private static CustomQueryAnswerPayload readPayload(int i, FriendlyByteBuf friendlyByteBuf) {
		return readUnknownPayload(friendlyByteBuf);
	}

	private static CustomQueryAnswerPayload readUnknownPayload(FriendlyByteBuf friendlyByteBuf) {
		int i = friendlyByteBuf.readableBytes();
		if (i >= 0 && i <= 1048576) {
			friendlyByteBuf.skipBytes(i);
			return DiscardedQueryAnswerPayload.INSTANCE;
		} else {
			throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
		}
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.transactionId);
		friendlyByteBuf.writeNullable(this.payload, (friendlyByteBufx, customQueryAnswerPayload) -> customQueryAnswerPayload.write(friendlyByteBufx));
	}

	@Override
	public PacketType<ServerboundCustomQueryAnswerPacket> type() {
		return LoginPacketTypes.SERVERBOUND_CUSTOM_QUERY_ANSWER;
	}

	public void handle(ServerLoginPacketListener serverLoginPacketListener) {
		serverLoginPacketListener.handleCustomQueryPacket(this);
	}
}
