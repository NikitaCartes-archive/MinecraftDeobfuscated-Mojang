package net.minecraft.client.gui.screens.recipebook;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

@Environment(EnvType.CLIENT)
public class FurnaceRecipeBookComponent extends RecipeBookComponent<AbstractFurnaceMenu> {
	private static final WidgetSprites FILTER_SPRITES = new WidgetSprites(
		ResourceLocation.withDefaultNamespace("recipe_book/furnace_filter_enabled"),
		ResourceLocation.withDefaultNamespace("recipe_book/furnace_filter_disabled"),
		ResourceLocation.withDefaultNamespace("recipe_book/furnace_filter_enabled_highlighted"),
		ResourceLocation.withDefaultNamespace("recipe_book/furnace_filter_disabled_highlighted")
	);
	private final Component recipeFilterName;

	public FurnaceRecipeBookComponent(AbstractFurnaceMenu abstractFurnaceMenu, Component component, List<RecipeBookComponent.TabInfo> list) {
		super(abstractFurnaceMenu, list);
		this.recipeFilterName = component;
	}

	@Override
	protected void initFilterButtonTextures() {
		this.filterButton.initTextureValues(FILTER_SPRITES);
	}

	@Override
	protected boolean isCraftingSlot(Slot slot) {
		return switch (slot.index) {
			case 0, 1, 2 -> true;
			default -> false;
		};
	}

	@Override
	protected void fillGhostRecipe(GhostSlots ghostSlots, RecipeDisplay recipeDisplay, ContextMap contextMap) {
		ghostSlots.setResult(this.menu.getResultSlot(), contextMap, recipeDisplay.result());
		if (recipeDisplay instanceof FurnaceRecipeDisplay furnaceRecipeDisplay) {
			ghostSlots.setInput(this.menu.slots.get(0), contextMap, furnaceRecipeDisplay.ingredient());
			Slot slot = this.menu.slots.get(1);
			if (slot.getItem().isEmpty()) {
				ghostSlots.setInput(slot, contextMap, furnaceRecipeDisplay.fuel());
			}
		}
	}

	@Override
	protected Component getRecipeFilterName() {
		return this.recipeFilterName;
	}

	@Override
	protected void selectMatchingRecipes(RecipeCollection recipeCollection, StackedItemContents stackedItemContents) {
		recipeCollection.selectRecipes(stackedItemContents, recipeDisplay -> recipeDisplay instanceof FurnaceRecipeDisplay);
	}
}
