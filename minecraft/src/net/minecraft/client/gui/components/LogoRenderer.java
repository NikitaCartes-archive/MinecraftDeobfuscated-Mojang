package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class LogoRenderer {
	public static final ResourceLocation MINECRAFT_LOGO = new ResourceLocation("textures/gui/title/minecraft.png");
	public static final ResourceLocation EASTER_EGG_LOGO = new ResourceLocation("textures/gui/title/minceraft.png");
	public static final ResourceLocation MINECRAFT_EDITION = new ResourceLocation("textures/gui/title/edition.png");
	public static final int LOGO_WIDTH = 256;
	public static final int LOGO_HEIGHT = 44;
	private static final int LOGO_TEXTURE_WIDTH = 256;
	private static final int LOGO_TEXTURE_HEIGHT = 64;
	private static final int EDITION_WIDTH = 128;
	private static final int EDITION_HEIGHT = 14;
	private static final int EDITION_TEXTURE_WIDTH = 128;
	private static final int EDITION_TEXTURE_HEIGHT = 16;
	public static final int DEFAULT_HEIGHT_OFFSET = 30;
	private static final int EDITION_LOGO_OVERLAP = 7;
	private final boolean showEasterEgg = (double)RandomSource.create().nextFloat() < 1.0E-4;
	private final boolean keepLogoThroughFade;

	public LogoRenderer(boolean bl) {
		this.keepLogoThroughFade = bl;
	}

	public void renderLogo(GuiGraphics guiGraphics, int i, float f) {
		this.renderLogo(guiGraphics, i, f, 30);
	}

	public void renderLogo(GuiGraphics guiGraphics, int i, float f, int j) {
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.keepLogoThroughFade ? 1.0F : f);
		int k = i / 2 - 128;
		guiGraphics.blit(this.showEasterEgg ? EASTER_EGG_LOGO : MINECRAFT_LOGO, k, j, 0.0F, 0.0F, 256, 44, 256, 64);
		int l = i / 2 - 64;
		int m = j + 44 - 7;
		guiGraphics.blit(MINECRAFT_EDITION, l, m, 0.0F, 0.0F, 128, 14, 128, 16);
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
