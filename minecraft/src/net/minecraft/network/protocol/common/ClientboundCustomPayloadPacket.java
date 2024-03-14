package net.minecraft.network.protocol.common;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.custom.BeeDebugPayload;
import net.minecraft.network.protocol.common.custom.BrainDebugPayload;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.BreezeDebugPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.network.protocol.common.custom.GameEventDebugPayload;
import net.minecraft.network.protocol.common.custom.GameEventListenerDebugPayload;
import net.minecraft.network.protocol.common.custom.GameTestAddMarkerDebugPayload;
import net.minecraft.network.protocol.common.custom.GameTestClearMarkersDebugPayload;
import net.minecraft.network.protocol.common.custom.GoalDebugPayload;
import net.minecraft.network.protocol.common.custom.HiveDebugPayload;
import net.minecraft.network.protocol.common.custom.NeighborUpdatesDebugPayload;
import net.minecraft.network.protocol.common.custom.PathfindingDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiAddedDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiRemovedDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiTicketCountDebugPayload;
import net.minecraft.network.protocol.common.custom.RaidsDebugPayload;
import net.minecraft.network.protocol.common.custom.StructuresDebugPayload;
import net.minecraft.network.protocol.common.custom.VillageSectionsDebugPayload;
import net.minecraft.network.protocol.common.custom.WorldGenAttemptDebugPayload;

public record ClientboundCustomPayloadPacket(CustomPacketPayload payload) implements Packet<ClientCommonPacketListener> {
	private static final int MAX_PAYLOAD_SIZE = 1048576;
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCustomPayloadPacket> GAMEPLAY_STREAM_CODEC = CustomPacketPayload.<RegistryFriendlyByteBuf>codec(
			resourceLocation -> DiscardedPayload.codec(resourceLocation, 1048576),
			Util.make(
				Lists.<CustomPacketPayload.TypeAndCodec<? super RegistryFriendlyByteBuf, ?>>newArrayList(
					new CustomPacketPayload.TypeAndCodec<>(BrandPayload.TYPE, BrandPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(BeeDebugPayload.TYPE, BeeDebugPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(BrainDebugPayload.TYPE, BrainDebugPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(BreezeDebugPayload.TYPE, BreezeDebugPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(GameEventDebugPayload.TYPE, GameEventDebugPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(GameEventListenerDebugPayload.TYPE, GameEventListenerDebugPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(GameTestAddMarkerDebugPayload.TYPE, GameTestAddMarkerDebugPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(GameTestClearMarkersDebugPayload.TYPE, GameTestClearMarkersDebugPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(GoalDebugPayload.TYPE, GoalDebugPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(HiveDebugPayload.TYPE, HiveDebugPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(NeighborUpdatesDebugPayload.TYPE, NeighborUpdatesDebugPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(PathfindingDebugPayload.TYPE, PathfindingDebugPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(PoiAddedDebugPayload.TYPE, PoiAddedDebugPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(PoiRemovedDebugPayload.TYPE, PoiRemovedDebugPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(PoiTicketCountDebugPayload.TYPE, PoiTicketCountDebugPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(RaidsDebugPayload.TYPE, RaidsDebugPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(StructuresDebugPayload.TYPE, StructuresDebugPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(VillageSectionsDebugPayload.TYPE, VillageSectionsDebugPayload.STREAM_CODEC),
					new CustomPacketPayload.TypeAndCodec<>(WorldGenAttemptDebugPayload.TYPE, WorldGenAttemptDebugPayload.STREAM_CODEC)
				),
				arrayList -> {
				}
			)
		)
		.map(ClientboundCustomPayloadPacket::new, ClientboundCustomPayloadPacket::payload);
	public static final StreamCodec<FriendlyByteBuf, ClientboundCustomPayloadPacket> CONFIG_STREAM_CODEC = CustomPacketPayload.<FriendlyByteBuf>codec(
			resourceLocation -> DiscardedPayload.codec(resourceLocation, 1048576),
			List.of(new CustomPacketPayload.TypeAndCodec<>(BrandPayload.TYPE, BrandPayload.STREAM_CODEC))
		)
		.map(ClientboundCustomPayloadPacket::new, ClientboundCustomPayloadPacket::payload);

	@Override
	public PacketType<ClientboundCustomPayloadPacket> type() {
		return CommonPacketTypes.CLIENTBOUND_CUSTOM_PAYLOAD;
	}

	public void handle(ClientCommonPacketListener clientCommonPacketListener) {
		clientCommonPacketListener.handleCustomPayload(this);
	}
}
