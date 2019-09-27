package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

@Environment(EnvType.CLIENT)
public class GhostRecipe {
	private Recipe<?> recipe;
	private final List<GhostRecipe.GhostIngredient> ingredients = Lists.<GhostRecipe.GhostIngredient>newArrayList();
	private float time;

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
	public Recipe<?> getRecipe() {
		return this.recipe;
	}

	public void setRecipe(Recipe<?> recipe) {
		this.recipe = recipe;
	}

	public void render(Minecraft minecraft, int i, int j, boolean bl, float f) {
		if (!Screen.hasControlDown()) {
			this.time += f;
		}

		for (int k = 0; k < this.ingredients.size(); k++) {
			GhostRecipe.GhostIngredient ghostIngredient = (GhostRecipe.GhostIngredient)this.ingredients.get(k);
			int l = ghostIngredient.getX() + i;
			int m = ghostIngredient.getY() + j;
			if (k == 0 && bl) {
				GuiComponent.fill(l - 4, m - 4, l + 20, m + 20, 822018048);
			} else {
				GuiComponent.fill(l, m, l + 16, m + 16, 822018048);
			}

			ItemStack itemStack = ghostIngredient.getItem();
			ItemRenderer itemRenderer = minecraft.getItemRenderer();
			itemRenderer.renderAndDecorateItem(minecraft.player, itemStack, l, m);
			RenderSystem.depthFunc(516);
			GuiComponent.fill(l, m, l + 16, m + 16, 822083583);
			RenderSystem.depthFunc(515);
			if (k == 0) {
				itemRenderer.renderGuiItemDecorations(minecraft.font, itemStack, l, m);
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
			return itemStacks[Mth.floor(GhostRecipe.this.time / 30.0F) % itemStacks.length];
		}
	}
}
