package net.minecraft.client.renderer.debug;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class LightDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;

	public LightDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}
}
