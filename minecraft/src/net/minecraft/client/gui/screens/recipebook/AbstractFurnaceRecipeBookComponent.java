package net.minecraft.client.gui.screens.recipebook;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

@Environment(EnvType.CLIENT)
public abstract class AbstractFurnaceRecipeBookComponent extends RecipeBookComponent {
	private Iterator<Item> iterator;
	private Set<Item> fuels;
	private Slot fuelSlot;
	private Item fuel;
	private float time;

	@Override
	protected void initFilterButtonTextures() {
		this.filterButton.initTextureValues(152, 182, 28, 18, RECIPE_BOOK_LOCATION);
	}

	@Override
	public void slotClicked(@Nullable Slot slot) {
		super.slotClicked(slot);
		if (slot != null && slot.index < this.menu.getSize()) {
			this.fuelSlot = null;
		}
	}

	@Override
	public void setupGhostRecipe(Recipe<?> recipe, List<Slot> list) {
		ItemStack itemStack = recipe.getResultItem();
		this.ghostRecipe.setRecipe(recipe);
		this.ghostRecipe.addIngredient(Ingredient.of(itemStack), ((Slot)list.get(2)).x, ((Slot)list.get(2)).y);
		NonNullList<Ingredient> nonNullList = recipe.getIngredients();
		this.fuelSlot = (Slot)list.get(1);
		if (this.fuels == null) {
			this.fuels = this.getFuelItems();
		}

		this.iterator = this.fuels.iterator();
		this.fuel = null;
		Iterator<Ingredient> iterator = nonNullList.iterator();

		for (int i = 0; i < 2; i++) {
			if (!iterator.hasNext()) {
				return;
			}

			Ingredient ingredient = (Ingredient)iterator.next();
			if (!ingredient.isEmpty()) {
				Slot slot = (Slot)list.get(i);
				this.ghostRecipe.addIngredient(ingredient, slot.x, slot.y);
			}
		}
	}

	protected abstract Set<Item> getFuelItems();

	@Override
	public void renderGhostRecipe(PoseStack poseStack, int i, int j, boolean bl, float f) {
		super.renderGhostRecipe(poseStack, i, j, bl, f);
		if (this.fuelSlot != null) {
			if (!Screen.hasControlDown()) {
				this.time += f;
			}

			int k = this.fuelSlot.x + i;
			int l = this.fuelSlot.y + j;
			GuiComponent.fill(poseStack, k, l, k + 16, l + 16, 822018048);
			this.minecraft.getItemRenderer().renderAndDecorateItem(this.minecraft.player, this.getFuel().getDefaultInstance(), k, l);
			RenderSystem.depthFunc(516);
			GuiComponent.fill(poseStack, k, l, k + 16, l + 16, 822083583);
			RenderSystem.depthFunc(515);
		}
	}

	private Item getFuel() {
		if (this.fuel == null || this.time > 30.0F) {
			this.time = 0.0F;
			if (this.iterator == null || !this.iterator.hasNext()) {
				if (this.fuels == null) {
					this.fuels = this.getFuelItems();
				}

				this.iterator = this.fuels.iterator();
			}

			this.fuel = (Item)this.iterator.next();
		}

		return this.fuel;
	}
}
