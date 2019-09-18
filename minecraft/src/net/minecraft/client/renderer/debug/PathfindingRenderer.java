package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.pathfinder.Path;

@Environment(EnvType.CLIENT)
public class PathfindingRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;
	private final Map<Integer, Path> pathMap = Maps.<Integer, Path>newHashMap();
	private final Map<Integer, Float> pathMaxDist = Maps.<Integer, Float>newHashMap();
	private final Map<Integer, Long> creationMap = Maps.<Integer, Long>newHashMap();

	public PathfindingRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void addPath(int i, Path path, float f) {
		this.pathMap.put(i, path);
		this.creationMap.put(i, Util.getMillis());
		this.pathMaxDist.put(i, f);
	}
}
