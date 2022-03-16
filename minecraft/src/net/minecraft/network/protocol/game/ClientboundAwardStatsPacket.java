package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;

public class ClientboundAwardStatsPacket implements Packet<ClientGamePacketListener> {
	private final Object2IntMap<Stat<?>> stats;

	public ClientboundAwardStatsPacket(Object2IntMap<Stat<?>> object2IntMap) {
		this.stats = object2IntMap;
	}

	public ClientboundAwardStatsPacket(FriendlyByteBuf friendlyByteBuf) {
		this.stats = friendlyByteBuf.readMap(Object2IntOpenHashMap::new, friendlyByteBuf2 -> {
			StatType<?> statType = friendlyByteBuf2.readById(Registry.STAT_TYPE);
			return readStatCap(friendlyByteBuf, statType);
		}, FriendlyByteBuf::readVarInt);
	}

	private static <T> Stat<T> readStatCap(FriendlyByteBuf friendlyByteBuf, StatType<T> statType) {
		return statType.get(friendlyByteBuf.readById(statType.getRegistry()));
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAwardStats(this);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeMap(this.stats, ClientboundAwardStatsPacket::writeStatCap, FriendlyByteBuf::writeVarInt);
	}

	private static <T> void writeStatCap(FriendlyByteBuf friendlyByteBuf, Stat<T> stat) {
		friendlyByteBuf.writeId(Registry.STAT_TYPE, stat.getType());
		friendlyByteBuf.writeId(stat.getType().getRegistry(), stat.getValue());
	}

	public Map<Stat<?>, Integer> getStats() {
		return this.stats;
	}
}
