package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.GrindstoneMenu;

@Environment(EnvType.CLIENT)
public class GrindstoneScreen extends AbstractContainerScreen<GrindstoneMenu> {
	private static final ResourceLocation GRINDSTONE_LOCATION = new ResourceLocation("textures/gui/container/grindstone.png");

	public GrindstoneScreen(GrindstoneMenu grindstoneMenu, Inventory inventory, Component component) {
		super(grindstoneMenu, inventory, component);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.renderBg(poseStack, f, i, j);
		super.render(poseStack, i, j, f);
		this.renderTooltip(poseStack, i, j);
	}

	@Override
	protected void renderBg(PoseStack poseStack, float f, int i, int j) {
		RenderSystem.setShaderTexture(0, GRINDSTONE_LOCATION);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
		if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(2).hasItem()) {
			blit(poseStack, k + 92, l + 31, this.imageWidth, 0, 28, 21);
		}
	}
}
