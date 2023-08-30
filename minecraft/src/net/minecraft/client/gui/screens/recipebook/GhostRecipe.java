package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

@Environment(EnvType.CLIENT)
public class GhostRecipe {
	@Nullable
	private RecipeHolder<?> recipe;
	private final List<GhostRecipe.GhostIngredient> ingredients = Lists.<GhostRecipe.GhostIngredient>newArrayList();
	float time;

	public void clear() {
		this.recipe = null;
		this.ingredients.clear();
		this.time = 0.0F;
	}

	public void addIngredient(Ingredient ingredient, int i, int j) {
		this.ingredients.add(new GhostRecipe.GhostIngredient(ingredient, i, j));
	}

	public GhostRecipe.GhostIngredient get(int i) {
		return (GhostRecipe.GhostIngredient)this.ingredients.get(i);
	}

	public int size() {
		return this.ingredients.size();
	}

	@Nullable
	public RecipeHolder<?> getRecipe() {
		return this.recipe;
	}

	public void setRecipe(RecipeHolder<?> recipeHolder) {
		this.recipe = recipeHolder;
	}

	public void render(GuiGraphics guiGraphics, Minecraft minecraft, int i, int j, boolean bl, float f) {
		if (!Screen.hasControlDown()) {
			this.time += f;
		}

		for (int k = 0; k < this.ingredients.size(); k++) {
			GhostRecipe.GhostIngredient ghostIngredient = (GhostRecipe.GhostIngredient)this.ingredients.get(k);
			int l = ghostIngredient.getX() + i;
			int m = ghostIngredient.getY() + j;
			if (k == 0 && bl) {
				guiGraphics.fill(l - 4, m - 4, l + 20, m + 20, 822018048);
			} else {
				guiGraphics.fill(l, m, l + 16, m + 16, 822018048);
			}

			ItemStack itemStack = ghostIngredient.getItem();
			guiGraphics.renderFakeItem(itemStack, l, m);
			guiGraphics.fill(RenderType.guiGhostRecipeOverlay(), l, m, l + 16, m + 16, 822083583);
			if (k == 0) {
				guiGraphics.renderItemDecorations(minecraft.font, itemStack, l, m);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public class GhostIngredient {
		private final Ingredient ingredient;
		private final int x;
		private final int y;

		public GhostIngredient(Ingredient ingredient, int i, int j) {
			this.ingredient = ingredient;
			this.x = i;
			this.y = j;
		}

		public int getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}

		public ItemStack getItem() {
			ItemStack[] itemStacks = this.ingredient.getItems();
			return itemStacks.length == 0 ? ItemStack.EMPTY : itemStacks[Mth.floor(GhostRecipe.this.time / 30.0F) % itemStacks.length];
		}
	}
}
