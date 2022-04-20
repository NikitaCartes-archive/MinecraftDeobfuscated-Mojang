package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class ImageButton extends Button {
	private final ResourceLocation resourceLocation;
	private final int xTexStart;
	private final int yTexStart;
	private final int yDiffTex;
	private final int textureWidth;
	private final int textureHeight;

	public ImageButton(int i, int j, int k, int l, int m, int n, ResourceLocation resourceLocation, Button.OnPress onPress) {
		this(i, j, k, l, m, n, l, resourceLocation, 256, 256, onPress);
	}

	public ImageButton(int i, int j, int k, int l, int m, int n, int o, ResourceLocation resourceLocation, Button.OnPress onPress) {
		this(i, j, k, l, m, n, o, resourceLocation, 256, 256, onPress);
	}

	public ImageButton(int i, int j, int k, int l, int m, int n, int o, ResourceLocation resourceLocation, int p, int q, Button.OnPress onPress) {
		this(i, j, k, l, m, n, o, resourceLocation, p, q, onPress, CommonComponents.EMPTY);
	}

	public ImageButton(
		int i, int j, int k, int l, int m, int n, int o, ResourceLocation resourceLocation, int p, int q, Button.OnPress onPress, Component component
	) {
		this(i, j, k, l, m, n, o, resourceLocation, p, q, onPress, NO_TOOLTIP, component);
	}

	public ImageButton(
		int i,
		int j,
		int k,
		int l,
		int m,
		int n,
		int o,
		ResourceLocation resourceLocation,
		int p,
		int q,
		Button.OnPress onPress,
		Button.OnTooltip onTooltip,
		Component component
	) {
		super(i, j, k, l, component, onPress, onTooltip);
		this.textureWidth = p;
		this.textureHeight = q;
		this.xTexStart = m;
		this.yTexStart = n;
		this.yDiffTex = o;
		this.resourceLocation = resourceLocation;
	}

	public void setPosition(int i, int j) {
		this.x = i;
		this.y = j;
	}

	@Override
	public void renderButton(PoseStack poseStack, int i, int j, float f) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, this.resourceLocation);
		int k = this.yTexStart;
		if (this.isHoveredOrFocused()) {
			k += this.yDiffTex;
		}

		RenderSystem.enableDepthTest();
		blit(poseStack, this.x, this.y, (float)this.xTexStart, (float)k, this.width, this.height, this.textureWidth, this.textureHeight);
		if (this.isHovered) {
			this.renderToolTip(poseStack, i, j);
		}
	}
}
