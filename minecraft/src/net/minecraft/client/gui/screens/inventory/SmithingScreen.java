package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SmithingMenu;

@Environment(EnvType.CLIENT)
public class SmithingScreen extends ItemCombinerScreen<SmithingMenu> {
	private static final ResourceLocation SMITHING_LOCATION = new ResourceLocation("textures/gui/container/smithing.png");

	public SmithingScreen(SmithingMenu smithingMenu, Inventory inventory, Component component) {
		super(smithingMenu, inventory, component, SMITHING_LOCATION);
	}

	@Override
	protected void renderLabels(PoseStack poseStack, int i, int j) {
		RenderSystem.disableBlend();
		this.font.draw(poseStack, this.title, 60.0F, 20.0F, 4210752);
		this.font.draw(poseStack, this.inventory.getDisplayName(), 8.0F, (float)(this.imageHeight - 96 + 2), 4210752);
	}
}
