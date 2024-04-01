package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class LogoRenderer {
	public static final ResourceLocation POISONOUS_POTATO_LOGO = new ResourceLocation("nothingtoseeheremovealong", "textures/gui/title/poisonous_potato_logo.png");
	public static final int LOGO_WIDTH = 256;
	public static final int LOGO_HEIGHT = 44;
	private static final int LOGO_TEXTURE_WIDTH = 256;
	private static final int LOGO_TEXTURE_HEIGHT = 128;
	public static final int DEFAULT_HEIGHT_OFFSET = 30;
	private final boolean keepLogoThroughFade;

	public LogoRenderer(boolean bl) {
		this.keepLogoThroughFade = bl;
	}

	public void renderLogo(GuiGraphics guiGraphics, int i, float f) {
		this.renderLogo(guiGraphics, i, f, 30);
	}

	public void renderLogo(GuiGraphics guiGraphics, int i, float f, int j) {
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.keepLogoThroughFade ? 1.0F : f);
		RenderSystem.enableBlend();
		int k = i / 2 - 128;
		guiGraphics.blit(POISONOUS_POTATO_LOGO, k, j, 0.0F, 0.0F, 256, 128, 256, 128);
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
	}
}
