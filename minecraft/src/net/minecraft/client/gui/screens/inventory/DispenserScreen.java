package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.DispenserMenu;

@Environment(EnvType.CLIENT)
public class DispenserScreen extends AbstractContainerScreen<DispenserMenu> {
	private static final ResourceLocation CONTAINER_LOCATION = new ResourceLocation("textures/gui/container/dispenser.png");

	public DispenserScreen(DispenserMenu dispenserMenu, Inventory inventory, Component component) {
		super(dispenserMenu, inventory, component);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		super.render(poseStack, i, j, f);
		this.renderTooltip(poseStack, i, j);
	}

	@Override
	protected void renderLabels(PoseStack poseStack, int i, int j) {
		this.font.draw(poseStack, this.title, (float)(this.imageWidth / 2 - this.font.width(this.title) / 2), 6.0F, 4210752);
		this.font.draw(poseStack, this.inventory.getDisplayName(), 8.0F, (float)(this.imageHeight - 96 + 2), 4210752);
	}

	@Override
	protected void renderBg(PoseStack poseStack, float f, int i, int j) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(CONTAINER_LOCATION);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		this.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
	}
}
