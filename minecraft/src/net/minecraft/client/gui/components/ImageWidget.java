package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class ImageWidget extends AbstractWidget {
	private final ResourceLocation imageLocation;

	public ImageWidget(int i, int j, ResourceLocation resourceLocation) {
		this(0, 0, i, j, resourceLocation);
	}

	public ImageWidget(int i, int j, int k, int l, ResourceLocation resourceLocation) {
		super(i, j, k, l, Component.empty());
		this.imageLocation = resourceLocation;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}

	@Override
	public void renderWidget(PoseStack poseStack, int i, int j, float f) {
		RenderSystem.setShaderTexture(0, this.imageLocation);
		int k = this.getWidth();
		int l = this.getHeight();
		GuiComponent.blit(poseStack, this.getX(), this.getY(), 0.0F, 0.0F, k, l, k, l);
	}
}
