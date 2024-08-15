package net.minecraft.client.gui.screens.recipebook;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

@Environment(EnvType.CLIENT)
public class CraftingRecipeBookComponent extends RecipeBookComponent<AbstractCraftingMenu> {
	private static final WidgetSprites FILTER_BUTTON_SPRITES = new WidgetSprites(
		ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled"),
		ResourceLocation.withDefaultNamespace("recipe_book/filter_disabled"),
		ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled_highlighted"),
		ResourceLocation.withDefaultNamespace("recipe_book/filter_disabled_highlighted")
	);
	private static final Component ONLY_CRAFTABLES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.craftable");

	public CraftingRecipeBookComponent(AbstractCraftingMenu abstractCraftingMenu) {
		super(abstractCraftingMenu);
	}

	@Override
	protected boolean isCraftingSlot(Slot slot) {
		return this.menu.getResultSlot() == slot || this.menu.getInputGridSlots().contains(slot);
	}

	@Override
	protected void setupGhostRecipeSlots(GhostSlots ghostSlots, RecipeHolder<?> recipeHolder) {
		ItemStack itemStack = recipeHolder.value().getResultItem(this.minecraft.level.registryAccess());
		Slot slot = this.menu.getResultSlot();
		ghostSlots.addResult(itemStack, slot);
		List<Slot> list = this.menu.getInputGridSlots();
		PlaceRecipeHelper.placeRecipe(
			this.menu.getGridWidth(),
			this.menu.getGridHeight(),
			recipeHolder,
			recipeHolder.value().placementInfo().slotInfo(),
			(optional, i, j, k) -> optional.ifPresent(slotInfo -> {
					Slot slotx = (Slot)list.get(i);
					ghostSlots.addIngredient(slotInfo.possibleItems(), slotx);
				})
		);
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
	protected void selectMatchingRecipes(RecipeCollection recipeCollection, StackedItemContents stackedItemContents, RecipeBook recipeBook) {
		recipeCollection.selectMatchingRecipes(stackedItemContents, this.menu.getGridWidth(), this.menu.getGridHeight(), recipeBook);
	}
}
