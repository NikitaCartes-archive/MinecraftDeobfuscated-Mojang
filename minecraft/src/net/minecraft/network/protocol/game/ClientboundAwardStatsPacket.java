package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.io.IOException;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;

public class ClientboundAwardStatsPacket implements Packet<ClientGamePacketListener> {
	private Object2IntMap<Stat<?>> stats;

	public ClientboundAwardStatsPacket() {
	}

	public ClientboundAwardStatsPacket(Object2IntMap<Stat<?>> object2IntMap) {
		this.stats = object2IntMap;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAwardStats(this);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		int i = friendlyByteBuf.readVarInt();
		this.stats = new Object2IntOpenHashMap<>(i);

		for (int j = 0; j < i; j++) {
			this.readStat(Registry.STAT_TYPE.byId(friendlyByteBuf.readVarInt()), friendlyByteBuf);
		}
	}

	private <T> void readStat(StatType<T> statType, FriendlyByteBuf friendlyByteBuf) {
		int i = friendlyByteBuf.readVarInt();
		int j = friendlyByteBuf.readVarInt();
		this.stats.put(statType.get(statType.getRegistry().byId(i)), j);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.stats.size());

		for (Entry<Stat<?>> entry : this.stats.object2IntEntrySet()) {
			Stat<?> stat = (Stat<?>)entry.getKey();
			friendlyByteBuf.writeVarInt(Registry.STAT_TYPE.getId(stat.getType()));
			friendlyByteBuf.writeVarInt(this.getId(stat));
			friendlyByteBuf.writeVarInt(entry.getIntValue());
		}
	}

	private <T> int getId(Stat<T> stat) {
		return stat.getType().getRegistry().getId(stat.getValue());
	}

	@Environment(EnvType.CLIENT)
	public Map<Stat<?>, Integer> getStats() {
		return this.stats;
	}
}
