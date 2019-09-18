package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

@Environment(EnvType.CLIENT)
public class NeighborsUpdateRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;
	private final Map<Long, Map<BlockPos, Integer>> lastUpdate = Maps.newTreeMap(Ordering.natural().reverse());

	NeighborsUpdateRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void addUpdate(long l, BlockPos blockPos) {
		Map<BlockPos, Integer> map = (Map<BlockPos, Integer>)this.lastUpdate.get(l);
		if (map == null) {
			map = Maps.<BlockPos, Integer>newHashMap();
			this.lastUpdate.put(l, map);
		}

		Integer integer = (Integer)map.get(blockPos);
		if (integer == null) {
			integer = 0;
		}

		map.put(blockPos, integer + 1);
	}
}
