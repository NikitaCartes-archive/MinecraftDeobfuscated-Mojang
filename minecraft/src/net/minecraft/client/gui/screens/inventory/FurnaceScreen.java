package net.minecraft.client.gui.screens.inventory;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeBookCategories;

@Environment(EnvType.CLIENT)
public class FurnaceScreen extends AbstractFurnaceScreen<FurnaceMenu> {
	private static final ResourceLocation LIT_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/furnace/lit_progress");
	private static final ResourceLocation BURN_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");
	private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");
	private static final Component FILTER_NAME = Component.translatable("gui.recipebook.toggleRecipes.smeltable");
	private static final List<RecipeBookComponent.TabInfo> TABS = List.of(
		new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.FURNACE),
		new RecipeBookComponent.TabInfo(Items.PORKCHOP, RecipeBookCategories.FURNACE_FOOD),
		new RecipeBookComponent.TabInfo(Items.STONE, RecipeBookCategories.FURNACE_BLOCKS),
		new RecipeBookComponent.TabInfo(Items.LAVA_BUCKET, Items.EMERALD, RecipeBookCategories.FURNACE_MISC)
	);

	public FurnaceScreen(FurnaceMenu furnaceMenu, Inventory inventory, Component component) {
		super(furnaceMenu, inventory, component, FILTER_NAME, TEXTURE, LIT_PROGRESS_SPRITE, BURN_PROGRESS_SPRITE, TABS);
	}
}
