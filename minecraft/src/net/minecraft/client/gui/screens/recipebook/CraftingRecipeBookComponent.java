package net.minecraft.client.gui.screens.recipebook;

import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.BasicRecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

@Environment(EnvType.CLIENT)
public class CraftingRecipeBookComponent extends RecipeBookComponent<AbstractCraftingMenu> {
	private static final WidgetSprites FILTER_BUTTON_SPRITES = new WidgetSprites(
		ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled"),
		ResourceLocation.withDefaultNamespace("recipe_book/filter_disabled"),
		ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled_highlighted"),
		ResourceLocation.withDefaultNamespace("recipe_book/filter_disabled_highlighted")
	);
	private static final Component ONLY_CRAFTABLES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.craftable");
	private static final List<RecipeBookComponent.TabInfo> TABS = List.of(
		new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.CRAFTING),
		new RecipeBookComponent.TabInfo(Items.IRON_AXE, Items.GOLDEN_SWORD, BasicRecipeBookCategory.CRAFTING_EQUIPMENT),
		new RecipeBookComponent.TabInfo(Items.BRICKS, BasicRecipeBookCategory.CRAFTING_BUILDING_BLOCKS),
		new RecipeBookComponent.TabInfo(Items.LAVA_BUCKET, Items.APPLE, BasicRecipeBookCategory.CRAFTING_MISC),
		new RecipeBookComponent.TabInfo(Items.REDSTONE, BasicRecipeBookCategory.CRAFTING_REDSTONE)
	);

	public CraftingRecipeBookComponent(AbstractCraftingMenu abstractCraftingMenu) {
		super(abstractCraftingMenu, TABS);
	}

	@Override
	protected boolean isCraftingSlot(Slot slot) {
		return this.menu.getResultSlot() == slot || this.menu.getInputGridSlots().contains(slot);
	}

	private boolean canDisplay(RecipeDisplay recipeDisplay) {
		int i = this.menu.getGridWidth();
		int j = this.menu.getGridHeight();
		Objects.requireNonNull(recipeDisplay);

		return switch (recipeDisplay) {
			case ShapedCraftingRecipeDisplay shapedCraftingRecipeDisplay -> i >= shapedCraftingRecipeDisplay.width() && j >= shapedCraftingRecipeDisplay.height();
			case ShapelessCraftingRecipeDisplay shapelessCraftingRecipeDisplay -> i * j >= shapelessCraftingRecipeDisplay.ingredients().size();
			default -> false;
		};
	}

	@Override
	protected void fillGhostRecipe(GhostSlots ghostSlots, RecipeDisplay recipeDisplay, SlotDisplay.ResolutionContext resolutionContext) {
		ghostSlots.setResult(this.menu.getResultSlot(), resolutionContext, recipeDisplay.result());
		Objects.requireNonNull(recipeDisplay);
		switch (recipeDisplay) {
			case ShapedCraftingRecipeDisplay shapedCraftingRecipeDisplay:
				List<Slot> list = this.menu.getInputGridSlots();
				PlaceRecipeHelper.placeRecipe(
					this.menu.getGridWidth(),
					this.menu.getGridHeight(),
					shapedCraftingRecipeDisplay.width(),
					shapedCraftingRecipeDisplay.height(),
					shapedCraftingRecipeDisplay.ingredients(),
					(slotDisplay, ix, jx, k) -> {
						Slot slot = (Slot)list.get(ix);
						ghostSlots.setInput(slot, resolutionContext, slotDisplay);
					}
				);
				break;
			case ShapelessCraftingRecipeDisplay shapelessCraftingRecipeDisplay:
				label15: {
					List<Slot> list2 = this.menu.getInputGridSlots();
					int i = Math.min(shapelessCraftingRecipeDisplay.ingredients().size(), list2.size());

					for (int j = 0; j < i; j++) {
						ghostSlots.setInput((Slot)list2.get(j), resolutionContext, (SlotDisplay)shapelessCraftingRecipeDisplay.ingredients().get(j));
					}
					break label15;
				}
		}
	}

	@Override
	protected void initFilterButtonTextures() {
		this.filterButton.initTextureValues(FILTER_BUTTON_SPRITES);
	}

	@Override
	protected Component getRecipeFilterName() {
		return ONLY_CRAFTABLES_TOOLTIP;
	}

	@Override
	protected void selectMatchingRecipes(RecipeCollection recipeCollection, StackedItemContents stackedItemContents) {
		recipeCollection.selectRecipes(stackedItemContents, this::canDisplay);
	}
}
