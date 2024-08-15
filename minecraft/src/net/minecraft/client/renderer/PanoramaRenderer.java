package net.minecraft.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

@Environment(EnvType.CLIENT)
public class PanoramaRenderer {
	public static final ResourceLocation PANORAMA_OVERLAY = ResourceLocation.withDefaultNamespace("textures/gui/title/background/panorama_overlay.png");
	private final Minecraft minecraft;
	private final CubeMap cubeMap;
	private float spin;

	public PanoramaRenderer(CubeMap cubeMap) {
		this.cubeMap = cubeMap;
		this.minecraft = Minecraft.getInstance();
	}

	public void render(GuiGraphics guiGraphics, int i, int j, float f, float g) {
		float h = this.minecraft.getDeltaTracker().getRealtimeDeltaTicks();
		float k = (float)((double)h * this.minecraft.options.panoramaSpeed().get());
		this.spin = wrap(this.spin + k * 0.1F, 360.0F);
		guiGraphics.flush();
		this.cubeMap.render(this.minecraft, 10.0F, -this.spin, f);
		guiGraphics.flush();
		guiGraphics.blit(RenderType::guiTextured, PANORAMA_OVERLAY, 0, 0, 0.0F, 0.0F, i, j, 16, 128, 16, 128, ARGB.white(f));
	}

	private static float wrap(float f, float g) {
		return f > g ? f - g : f;
	}
}
