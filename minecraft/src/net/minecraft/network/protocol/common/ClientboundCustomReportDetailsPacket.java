package net.minecraft.network.protocol.common;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundCustomReportDetailsPacket(Map<String, String> details) implements Packet<ClientCommonPacketListener> {
	private static final int MAX_DETAIL_KEY_LENGTH = 128;
	private static final int MAX_DETAIL_VALUE_LENGTH = 4096;
	private static final int MAX_DETAIL_COUNT = 32;
	private static final StreamCodec<ByteBuf, Map<String, String>> DETAILS_STREAM_CODEC = ByteBufCodecs.map(
		HashMap::new, ByteBufCodecs.stringUtf8(128), ByteBufCodecs.stringUtf8(4096), 32
	);
	public static final StreamCodec<ByteBuf, ClientboundCustomReportDetailsPacket> STREAM_CODEC = StreamCodec.composite(
		DETAILS_STREAM_CODEC, ClientboundCustomReportDetailsPacket::details, ClientboundCustomReportDetailsPacket::new
	);

	@Override
	public PacketType<ClientboundCustomReportDetailsPacket> type() {
		return CommonPacketTypes.CLIENTBOUND_CUSTOM_REPORT_DETAILS;
	}

	public void handle(ClientCommonPacketListener clientCommonPacketListener) {
		clientCommonPacketListener.handleCustomReportDetails(this);
	}
}
