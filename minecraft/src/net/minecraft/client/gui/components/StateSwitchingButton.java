package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class StateSwitchingButton extends AbstractWidget {
	protected ResourceLocation resourceLocation;
	protected boolean isStateTriggered;
	protected int xTexStart;
	protected int yTexStart;
	protected int xDiffTex;
	protected int yDiffTex;

	public StateSwitchingButton(int i, int j, int k, int l, boolean bl) {
		super(i, j, k, l, TextComponent.EMPTY);
		this.isStateTriggered = bl;
	}

	public void initTextureValues(int i, int j, int k, int l, ResourceLocation resourceLocation) {
		this.xTexStart = i;
		this.yTexStart = j;
		this.xDiffTex = k;
		this.yDiffTex = l;
		this.resourceLocation = resourceLocation;
	}

	public void setStateTriggered(boolean bl) {
		this.isStateTriggered = bl;
	}

	public boolean isStateTriggered() {
		return this.isStateTriggered;
	}

	public void setPosition(int i, int j) {
		this.x = i;
		this.y = j;
	}

	@Override
	public void renderButton(PoseStack poseStack, int i, int j, float f) {
		Minecraft minecraft = Minecraft.getInstance();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, this.resourceLocation);
		RenderSystem.disableDepthTest();
		int k = this.xTexStart;
		int l = this.yTexStart;
		if (this.isStateTriggered) {
			k += this.xDiffTex;
		}

		if (this.isHovered()) {
			l += this.yDiffTex;
		}

		this.blit(poseStack, this.x, this.y, k, l, this.width, this.height);
		RenderSystem.enableDepthTest();
	}
}
