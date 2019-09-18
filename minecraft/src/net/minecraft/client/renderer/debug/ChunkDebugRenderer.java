package net.minecraft.client.renderer.debug;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class ChunkDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;
	private double lastUpdateTime = Double.MIN_VALUE;
	private final int radius = 12;

	public ChunkDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}
}
