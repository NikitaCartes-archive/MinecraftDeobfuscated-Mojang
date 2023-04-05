package net.minecraft.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class PanoramaRenderer {
	private final Minecraft minecraft;
	private final CubeMap cubeMap;
	private float spin;
	private float bob;

	public PanoramaRenderer(CubeMap cubeMap) {
		this.cubeMap = cubeMap;
		this.minecraft = Minecraft.getInstance();
	}

	public void render(float f, float g) {
		float h = (float)((double)f * this.minecraft.options.panoramaSpeed().get());
		this.spin = wrap(this.spin + h * 0.1F, 360.0F);
		this.bob = wrap(this.bob + h * 0.001F, (float) (Math.PI * 2));
		this.cubeMap.render(this.minecraft, 10.0F, -this.spin, g);
	}

	private static float wrap(float f, float g) {
		return f > g ? f - g : f;
	}
}
