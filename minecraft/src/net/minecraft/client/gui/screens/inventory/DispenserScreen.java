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
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		super.render(poseStack, i, j, f);
		this.renderTooltip(poseStack, i, j);
	}

	@Override
	protected void renderBg(PoseStack poseStack, float f, int i, int j) {
		RenderSystem.setShaderTexture(0, CONTAINER_LOCATION);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
	}
}
