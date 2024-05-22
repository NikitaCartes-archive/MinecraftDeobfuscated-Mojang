package net.minecraft.client.gui.screens.recipebook;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

@Environment(EnvType.CLIENT)
public abstract class AbstractFurnaceRecipeBookComponent extends RecipeBookComponent {
	private static final WidgetSprites FILTER_SPRITES = new WidgetSprites(
		ResourceLocation.withDefaultNamespace("recipe_book/furnace_filter_enabled"),
		ResourceLocation.withDefaultNamespace("recipe_book/furnace_filter_disabled"),
		ResourceLocation.withDefaultNamespace("recipe_book/furnace_filter_enabled_highlighted"),
		ResourceLocation.withDefaultNamespace("recipe_book/furnace_filter_disabled_highlighted")
	);
	@Nullable
	private Ingredient fuels;

	@Override
	protected void initFilterButtonTextures() {
		this.filterButton.initTextureValues(FILTER_SPRITES);
	}

	@Override
	public void slotClicked(@Nullable Slot slot) {
		super.slotClicked(slot);
		if (slot != null && slot.index < this.menu.getSize()) {
			this.ghostRecipe.clear();
		}
	}

	@Override
	public void setupGhostRecipe(RecipeHolder<?> recipeHolder, List<Slot> list) {
		ItemStack itemStack = recipeHolder.value().getResultItem(this.minecraft.level.registryAccess());
		this.ghostRecipe.setRecipe(recipeHolder);
		this.ghostRecipe.addIngredient(Ingredient.of(itemStack), ((Slot)list.get(2)).x, ((Slot)list.get(2)).y);
		NonNullList<Ingredient> nonNullList = recipeHolder.value().getIngredients();
		Slot slot = (Slot)list.get(1);
		if (slot.getItem().isEmpty()) {
			if (this.fuels == null) {
				this.fuels = Ingredient.of(this.getFuelItems().stream().filter(item -> item.isEnabled(this.minecraft.level.enabledFeatures())).map(ItemStack::new));
			}

			this.ghostRecipe.addIngredient(this.fuels, slot.x, slot.y);
		}

		Iterator<Ingredient> iterator = nonNullList.iterator();

		for (int i = 0; i < 2; i++) {
			if (!iterator.hasNext()) {
				return;
			}

			Ingredient ingredient = (Ingredient)iterator.next();
			if (!ingredient.isEmpty()) {
				Slot slot2 = (Slot)list.get(i);
				this.ghostRecipe.addIngredient(ingredient, slot2.x, slot2.y);
			}
		}
	}

	protected abstract Set<Item> getFuelItems();
}
