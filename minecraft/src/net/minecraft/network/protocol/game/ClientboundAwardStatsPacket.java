package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.stats.Stat;

public record ClientboundAwardStatsPacket(Object2IntMap<Stat<?>> stats) implements Packet<ClientGamePacketListener> {
	private static final StreamCodec<RegistryFriendlyByteBuf, Object2IntMap<Stat<?>>> STAT_VALUES_STREAM_CODEC = ByteBufCodecs.map(
		Object2IntOpenHashMap::new, Stat.STREAM_CODEC, ByteBufCodecs.VAR_INT
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAwardStatsPacket> STREAM_CODEC = STAT_VALUES_STREAM_CODEC.map(
		ClientboundAwardStatsPacket::new, ClientboundAwardStatsPacket::stats
	);

	@Override
	public PacketType<ClientboundAwardStatsPacket> type() {
		return GamePacketTypes.CLIENTBOUND_AWARD_STATS;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAwardStats(this);
	}
}
