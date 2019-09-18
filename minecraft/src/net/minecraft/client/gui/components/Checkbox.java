package net.minecraft.client.gui.components;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class Checkbox extends AbstractButton {
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");
	boolean selected;

	public Checkbox(int i, int j, int k, int l, String string, boolean bl) {
		super(i, j, k, l, string);
		this.selected = bl;
	}

	@Override
	public void onPress() {
		this.selected = !this.selected;
	}

	public boolean selected() {
		return this.selected;
	}

	@Override
	public void renderButton(int i, int j, float f) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.getTextureManager().bind(TEXTURE);
		RenderSystem.enableDepthTest();
		Font font = minecraft.font;
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		blit(this.x, this.y, 0.0F, this.selected ? 20.0F : 0.0F, 20, this.height, 32, 64);
		this.renderBg(minecraft, i, j);
		int k = 14737632;
		this.drawString(font, this.getMessage(), this.x + 24, this.y + (this.height - 8) / 2, 14737632 | Mth.ceil(this.alpha * 255.0F) << 24);
	}
}
