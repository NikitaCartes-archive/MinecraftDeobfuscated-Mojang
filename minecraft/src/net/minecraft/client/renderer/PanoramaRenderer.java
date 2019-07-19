package net.minecraft.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class PanoramaRenderer {
	private final Minecraft minecraft;
	private final CubeMap cubeMap;
	private float time;

	public PanoramaRenderer(CubeMap cubeMap) {
		this.cubeMap = cubeMap;
		this.minecraft = Minecraft.getInstance();
	}

	public void render(float f, float g) {
		this.time += f;
		this.cubeMap.render(this.minecraft, Mth.sin(this.time * 0.001F) * 5.0F + 25.0F, -this.time * 0.1F, g);
		this.minecraft.window.setupGuiState(Minecraft.ON_OSX);
	}
}
