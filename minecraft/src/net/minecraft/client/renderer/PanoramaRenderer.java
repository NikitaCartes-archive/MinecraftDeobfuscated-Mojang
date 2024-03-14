package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class PanoramaRenderer {
	public static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
	private final Minecraft minecraft;
	private final CubeMap cubeMap;
	private float spin;
	private float bob;

	public PanoramaRenderer(CubeMap cubeMap) {
		this.cubeMap = cubeMap;
		this.minecraft = Minecraft.getInstance();
	}

	public void render(GuiGraphics guiGraphics, int i, int j, float f, float g) {
		float h = (float)((double)g * this.minecraft.options.panoramaSpeed().get());
		this.spin = wrap(this.spin + h * 0.1F, 360.0F);
		this.bob = wrap(this.bob + h * 0.001F, (float) (Math.PI * 2));
		this.cubeMap.render(this.minecraft, 10.0F, -this.spin, f);
		RenderSystem.enableBlend();
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, f);
		guiGraphics.blit(PANORAMA_OVERLAY, 0, 0, i, j, 0.0F, 0.0F, 16, 128, 16, 128);
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
	}

	private static float wrap(float f, float g) {
		return f > g ? f - g : f;
	}
}
