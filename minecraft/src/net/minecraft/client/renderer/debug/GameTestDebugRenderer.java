package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;

@Environment(EnvType.CLIENT)
public class GameTestDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Map<BlockPos, GameTestDebugRenderer.Marker> markers = Maps.<BlockPos, GameTestDebugRenderer.Marker>newHashMap();

	public void addMarker(BlockPos blockPos, int i, String string, int j) {
		this.markers.put(blockPos, new GameTestDebugRenderer.Marker(i, string, Util.getMillis() + (long)j));
	}

	@Override
	public void clear() {
		this.markers.clear();
	}

	@Environment(EnvType.CLIENT)
	static class Marker {
		public int color;
		public String text;
		public long removeAtTime;

		public Marker(int i, String string, long l) {
			this.color = i;
			this.text = string;
			this.removeAtTime = l;
		}
	}
}
