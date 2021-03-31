package net.minecraft.stats;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.entity.player.Player;

public class StatsCounter {
	protected final Object2IntMap<Stat<?>> stats = Object2IntMaps.synchronize(new Object2IntOpenHashMap<>());

	public StatsCounter() {
		this.stats.defaultReturnValue(0);
	}

	public void increment(Player player, Stat<?> stat, int i) {
		int j = (int)Math.min((long)this.getValue(stat) + (long)i, 2147483647L);
		this.setValue(player, stat, j);
	}

	public void setValue(Player player, Stat<?> stat, int i) {
		this.stats.put(stat, i);
	}

	public <T> int getValue(StatType<T> statType, T object) {
		return statType.contains(object) ? this.getValue(statType.get(object)) : 0;
	}

	public int getValue(Stat<?> stat) {
		return this.stats.getInt(stat);
	}
}
