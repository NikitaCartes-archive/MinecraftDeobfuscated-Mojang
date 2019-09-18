package net.minecraft.client.renderer.debug;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class HeightMapRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;

	public HeightMapRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}
}
