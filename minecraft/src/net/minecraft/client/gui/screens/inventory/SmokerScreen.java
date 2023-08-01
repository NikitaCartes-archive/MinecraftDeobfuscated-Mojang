package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.recipebook.SmokingRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SmokerMenu;

@Environment(EnvType.CLIENT)
public class SmokerScreen extends AbstractFurnaceScreen<SmokerMenu> {
	private static final ResourceLocation LIT_PROGRESS_SPRITE = new ResourceLocation("container/smoker/lit_progress");
	private static final ResourceLocation BURN_PROGRESS_SPRITE = new ResourceLocation("container/smoker/burn_progress");
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/smoker.png");

	public SmokerScreen(SmokerMenu smokerMenu, Inventory inventory, Component component) {
		super(smokerMenu, new SmokingRecipeBookComponent(), inventory, component, TEXTURE, LIT_PROGRESS_SPRITE, BURN_PROGRESS_SPRITE);
	}
}
