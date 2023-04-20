package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class PlayerFaceRenderer {
	public static final int SKIN_HEAD_U = 8;
	public static final int SKIN_HEAD_V = 8;
	public static final int SKIN_HEAD_WIDTH = 8;
	public static final int SKIN_HEAD_HEIGHT = 8;
	public static final int SKIN_HAT_U = 40;
	public static final int SKIN_HAT_V = 8;
	public static final int SKIN_HAT_WIDTH = 8;
	public static final int SKIN_HAT_HEIGHT = 8;
	public static final int SKIN_TEX_WIDTH = 64;
	public static final int SKIN_TEX_HEIGHT = 64;

	public static void draw(GuiGraphics guiGraphics, ResourceLocation resourceLocation, int i, int j, int k) {
		draw(guiGraphics, resourceLocation, i, j, k, true, false);
	}

	public static void draw(GuiGraphics guiGraphics, ResourceLocation resourceLocation, int i, int j, int k, boolean bl, boolean bl2) {
		int l = 8 + (bl2 ? 8 : 0);
		int m = 8 * (bl2 ? -1 : 1);
		guiGraphics.blit(resourceLocation, i, j, k, k, 8.0F, (float)l, 8, m, 64, 64);
		if (bl) {
			drawHat(guiGraphics, resourceLocation, i, j, k, bl2);
		}
	}

	private static void drawHat(GuiGraphics guiGraphics, ResourceLocation resourceLocation, int i, int j, int k, boolean bl) {
		int l = 8 + (bl ? 8 : 0);
		int m = 8 * (bl ? -1 : 1);
		RenderSystem.enableBlend();
		guiGraphics.blit(resourceLocation, i, j, k, k, 40.0F, (float)l, 8, m, 64, 64);
		RenderSystem.disableBlend();
	}
}
