package net.minecraft.client.gui.screens.inventory;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeBookCategories;

@Environment(EnvType.CLIENT)
public class SmokerScreen extends AbstractFurnaceScreen<SmokerMenu> {
	private static final ResourceLocation LIT_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/smoker/lit_progress");
	private static final ResourceLocation BURN_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/smoker/burn_progress");
	private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/smoker.png");
	private static final Component FILTER_NAME = Component.translatable("gui.recipebook.toggleRecipes.smokable");
	private static final List<RecipeBookComponent.TabInfo> TABS = List.of(
		new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.SMOKER), new RecipeBookComponent.TabInfo(Items.PORKCHOP, RecipeBookCategories.SMOKER_FOOD)
	);

	public SmokerScreen(SmokerMenu smokerMenu, Inventory inventory, Component component) {
		super(smokerMenu, inventory, component, FILTER_NAME, TEXTURE, LIT_PROGRESS_SPRITE, BURN_PROGRESS_SPRITE, TABS);
	}
}
