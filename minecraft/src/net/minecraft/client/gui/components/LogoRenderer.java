package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class LogoRenderer extends GuiComponent {
	public static final ResourceLocation MINECRAFT_LOGO = new ResourceLocation("textures/gui/title/minecraft.png");
	public static final ResourceLocation MINECRAFT_EDITION = new ResourceLocation("textures/gui/title/edition.png");
	public static final int LOGO_WIDTH = 274;
	public static final int LOGO_HEIGHT = 44;
	public static final int DEFAULT_HEIGHT_OFFSET = 30;
	private final boolean showEasterEgg = (double)RandomSource.create().nextFloat() < 1.0E-4;
	private final boolean keepLogoThroughFade;

	public LogoRenderer(boolean bl) {
		this.keepLogoThroughFade = bl;
	}

	public void renderLogo(PoseStack poseStack, int i, float f) {
		this.renderLogo(poseStack, i, f, 30);
	}

	public void renderLogo(PoseStack poseStack, int i, float f, int j) {
		RenderSystem.setShaderTexture(0, MINECRAFT_LOGO);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.keepLogoThroughFade ? 1.0F : f);
		int k = i / 2 - 137;
		if (this.showEasterEgg) {
			blitOutlineBlack(k, j, (integer, integer2) -> {
				blit(poseStack, integer, integer2, 0, 0, 99, 44);
				blit(poseStack, integer + 99, integer2, 129, 0, 27, 44);
				blit(poseStack, integer + 99 + 26, integer2, 126, 0, 3, 44);
				blit(poseStack, integer + 99 + 26 + 3, integer2, 99, 0, 26, 44);
				blit(poseStack, integer + 155, integer2, 0, 45, 155, 44);
			});
		} else {
			blitOutlineBlack(k, j, (integer, integer2) -> {
				blit(poseStack, integer, integer2, 0, 0, 155, 44);
				blit(poseStack, integer + 155, integer2, 0, 45, 155, 44);
			});
		}

		RenderSystem.setShaderTexture(0, MINECRAFT_EDITION);
		blit(poseStack, k + 88, j + 37, 0.0F, 0.0F, 98, 14, 128, 16);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
