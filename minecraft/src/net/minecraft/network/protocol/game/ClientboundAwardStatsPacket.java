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
		this.stats = friendlyByteBuf.readMap(Object2IntOpenHashMap::new, friendlyByteBufx -> {
			int i = friendlyByteBufx.readVarInt();
			int j = friendlyByteBufx.readVarInt();
			return readStatCap(Registry.STAT_TYPE.byId(i), j);
		}, FriendlyByteBuf::readVarInt);
	}

	private static <T> Stat<T> readStatCap(StatType<T> statType, int i) {
		return statType.get(statType.getRegistry().byId(i));
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAwardStats(this);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeMap(this.stats, (friendlyByteBufx, stat) -> {
			friendlyByteBufx.writeVarInt(Registry.STAT_TYPE.getId(stat.getType()));
			friendlyByteBufx.writeVarInt(this.getStatIdCap(stat));
		}, FriendlyByteBuf::writeVarInt);
	}

	private <T> int getStatIdCap(Stat<T> stat) {
		return stat.getType().getRegistry().getId(stat.getValue());
	}

	public Map<Stat<?>, Integer> getStats() {
		return this.stats;
	}
}
