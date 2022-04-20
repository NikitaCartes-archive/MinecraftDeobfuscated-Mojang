package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

@Environment(EnvType.CLIENT)
public class OverlayRecipeComponent extends GuiComponent implements Widget, GuiEventListener {
	static final ResourceLocation RECIPE_BOOK_LOCATION = new ResourceLocation("textures/gui/recipe_book.png");
	private static final int MAX_ROW = 4;
	private static final int MAX_ROW_LARGE = 5;
	private static final float ITEM_RENDER_SCALE = 0.375F;
	private final List<OverlayRecipeComponent.OverlayRecipeButton> recipeButtons = Lists.<OverlayRecipeComponent.OverlayRecipeButton>newArrayList();
	private boolean isVisible;
	private int x;
	private int y;
	Minecraft minecraft;
	private RecipeCollection collection;
	@Nullable
	private Recipe<?> lastRecipeClicked;
	float time;
	boolean isFurnaceMenu;

	public void init(Minecraft minecraft, RecipeCollection recipeCollection, int i, int j, int k, int l, float f) {
		this.minecraft = minecraft;
		this.collection = recipeCollection;
		if (minecraft.player.containerMenu instanceof AbstractFurnaceMenu) {
			this.isFurnaceMenu = true;
		}

		boolean bl = minecraft.player.getRecipeBook().isFiltering((RecipeBookMenu<?>)minecraft.player.containerMenu);
		List<Recipe<?>> list = recipeCollection.getDisplayRecipes(true);
		List<Recipe<?>> list2 = bl ? Collections.emptyList() : recipeCollection.getDisplayRecipes(false);
		int m = list.size();
		int n = m + list2.size();
		int o = n <= 16 ? 4 : 5;
		int p = (int)Math.ceil((double)((float)n / (float)o));
		this.x = i;
		this.y = j;
		int q = 25;
		float g = (float)(this.x + Math.min(n, o) * 25);
		float h = (float)(k + 50);
		if (g > h) {
			this.x = (int)((float)this.x - f * (float)((int)((g - h) / f)));
		}

		float r = (float)(this.y + p * 25);
		float s = (float)(l + 50);
		if (r > s) {
			this.y = (int)((float)this.y - f * (float)Mth.ceil((r - s) / f));
		}

		float t = (float)this.y;
		float u = (float)(l - 100);
		if (t < u) {
			this.y = (int)((float)this.y - f * (float)Mth.ceil((t - u) / f));
		}

		this.isVisible = true;
		this.recipeButtons.clear();

		for (int v = 0; v < n; v++) {
			boolean bl2 = v < m;
			Recipe<?> recipe = bl2 ? (Recipe)list.get(v) : (Recipe)list2.get(v - m);
			int w = this.x + 4 + 25 * (v % o);
			int x = this.y + 5 + 25 * (v / o);
			if (this.isFurnaceMenu) {
				this.recipeButtons.add(new OverlayRecipeComponent.OverlaySmeltingRecipeButton(w, x, recipe, bl2));
			} else {
				this.recipeButtons.add(new OverlayRecipeComponent.OverlayRecipeButton(w, x, recipe, bl2));
			}
		}

		this.lastRecipeClicked = null;
	}

	@Override
	public boolean changeFocus(boolean bl) {
		return false;
	}

	public RecipeCollection getRecipeCollection() {
		return this.collection;
	}

	@Nullable
	public Recipe<?> getLastRecipeClicked() {
		return this.lastRecipeClicked;
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (i != 0) {
			return false;
		} else {
			for (OverlayRecipeComponent.OverlayRecipeButton overlayRecipeButton : this.recipeButtons) {
				if (overlayRecipeButton.mouseClicked(d, e, i)) {
					this.lastRecipeClicked = overlayRecipeButton.recipe;
					return true;
				}
			}

			return false;
		}
	}

	@Override
	public boolean isMouseOver(double d, double e) {
		return false;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		if (this.isVisible) {
			this.time += f;
			RenderSystem.enableBlend();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, RECIPE_BOOK_LOCATION);
			poseStack.pushPose();
			poseStack.translate(0.0, 0.0, 170.0);
			int k = this.recipeButtons.size() <= 16 ? 4 : 5;
			int l = Math.min(this.recipeButtons.size(), k);
			int m = Mth.ceil((float)this.recipeButtons.size() / (float)k);
			int n = 24;
			int o = 4;
			int p = 82;
			int q = 208;
			this.nineInchSprite(poseStack, l, m, 24, 4, 82, 208);
			RenderSystem.disableBlend();

			for (OverlayRecipeComponent.OverlayRecipeButton overlayRecipeButton : this.recipeButtons) {
				overlayRecipeButton.render(poseStack, i, j, f);
			}

			poseStack.popPose();
		}
	}

	private void nineInchSprite(PoseStack poseStack, int i, int j, int k, int l, int m, int n) {
		this.blit(poseStack, this.x, this.y, m, n, l, l);
		this.blit(poseStack, this.x + l * 2 + i * k, this.y, m + k + l, n, l, l);
		this.blit(poseStack, this.x, this.y + l * 2 + j * k, m, n + k + l, l, l);
		this.blit(poseStack, this.x + l * 2 + i * k, this.y + l * 2 + j * k, m + k + l, n + k + l, l, l);

		for (int o = 0; o < i; o++) {
			this.blit(poseStack, this.x + l + o * k, this.y, m + l, n, k, l);
			this.blit(poseStack, this.x + l + (o + 1) * k, this.y, m + l, n, l, l);

			for (int p = 0; p < j; p++) {
				if (o == 0) {
					this.blit(poseStack, this.x, this.y + l + p * k, m, n + l, l, k);
					this.blit(poseStack, this.x, this.y + l + (p + 1) * k, m, n + l, l, l);
				}

				this.blit(poseStack, this.x + l + o * k, this.y + l + p * k, m + l, n + l, k, k);
				this.blit(poseStack, this.x + l + (o + 1) * k, this.y + l + p * k, m + l, n + l, l, k);
				this.blit(poseStack, this.x + l + o * k, this.y + l + (p + 1) * k, m + l, n + l, k, l);
				this.blit(poseStack, this.x + l + (o + 1) * k - 1, this.y + l + (p + 1) * k - 1, m + l, n + l, l + 1, l + 1);
				if (o == i - 1) {
					this.blit(poseStack, this.x + l * 2 + i * k, this.y + l + p * k, m + k + l, n + l, l, k);
					this.blit(poseStack, this.x + l * 2 + i * k, this.y + l + (p + 1) * k, m + k + l, n + l, l, l);
				}
			}

			this.blit(poseStack, this.x + l + o * k, this.y + l * 2 + j * k, m + l, n + k + l, k, l);
			this.blit(poseStack, this.x + l + (o + 1) * k, this.y + l * 2 + j * k, m + l, n + k + l, l, l);
		}
	}

	public void setVisible(boolean bl) {
		this.isVisible = bl;
	}

	public boolean isVisible() {
		return this.isVisible;
	}

	@Environment(EnvType.CLIENT)
	class OverlayRecipeButton extends AbstractWidget implements PlaceRecipe<Ingredient> {
		final Recipe<?> recipe;
		private final boolean isCraftable;
		protected final List<OverlayRecipeComponent.OverlayRecipeButton.Pos> ingredientPos = Lists.<OverlayRecipeComponent.OverlayRecipeButton.Pos>newArrayList();

		public OverlayRecipeButton(int i, int j, Recipe<?> recipe, boolean bl) {
			super(i, j, 200, 20, CommonComponents.EMPTY);
			this.width = 24;
			this.height = 24;
			this.recipe = recipe;
			this.isCraftable = bl;
			this.calculateIngredientsPositions(recipe);
		}

		protected void calculateIngredientsPositions(Recipe<?> recipe) {
			this.placeRecipe(3, 3, -1, recipe, recipe.getIngredients().iterator(), 0);
		}

		@Override
		public void updateNarration(NarrationElementOutput narrationElementOutput) {
			this.defaultButtonNarrationText(narrationElementOutput);
		}

		@Override
		public void addItemToSlot(Iterator<Ingredient> iterator, int i, int j, int k, int l) {
			ItemStack[] itemStacks = ((Ingredient)iterator.next()).getItems();
			if (itemStacks.length != 0) {
				this.ingredientPos.add(new OverlayRecipeComponent.OverlayRecipeButton.Pos(3 + l * 7, 3 + k * 7, itemStacks));
			}
		}

		@Override
		public void renderButton(PoseStack poseStack, int i, int j, float f) {
			RenderSystem.setShaderTexture(0, OverlayRecipeComponent.RECIPE_BOOK_LOCATION);
			int k = 152;
			if (!this.isCraftable) {
				k += 26;
			}

			int l = OverlayRecipeComponent.this.isFurnaceMenu ? 130 : 78;
			if (this.isHoveredOrFocused()) {
				l += 26;
			}

			this.blit(poseStack, this.x, this.y, k, l, this.width, this.height);
			PoseStack poseStack2 = RenderSystem.getModelViewStack();
			poseStack2.pushPose();
			poseStack2.translate((double)(this.x + 2), (double)(this.y + 2), 125.0);

			for (OverlayRecipeComponent.OverlayRecipeButton.Pos pos : this.ingredientPos) {
				poseStack2.pushPose();
				poseStack2.translate((double)pos.x, (double)pos.y, 0.0);
				poseStack2.scale(0.375F, 0.375F, 1.0F);
				poseStack2.translate(-8.0, -8.0, 0.0);
				RenderSystem.applyModelViewMatrix();
				OverlayRecipeComponent.this.minecraft
					.getItemRenderer()
					.renderAndDecorateItem(pos.ingredients[Mth.floor(OverlayRecipeComponent.this.time / 30.0F) % pos.ingredients.length], 0, 0);
				poseStack2.popPose();
			}

			poseStack2.popPose();
			RenderSystem.applyModelViewMatrix();
		}

		@Environment(EnvType.CLIENT)
		protected class Pos {
			public final ItemStack[] ingredients;
			public final int x;
			public final int y;

			public Pos(int i, int j, ItemStack[] itemStacks) {
				this.x = i;
				this.y = j;
				this.ingredients = itemStacks;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class OverlaySmeltingRecipeButton extends OverlayRecipeComponent.OverlayRecipeButton {
		public OverlaySmeltingRecipeButton(int i, int j, Recipe<?> recipe, boolean bl) {
			super(i, j, recipe, bl);
		}

		@Override
		protected void calculateIngredientsPositions(Recipe<?> recipe) {
			ItemStack[] itemStacks = recipe.getIngredients().get(0).getItems();
			this.ingredientPos.add(new OverlayRecipeComponent.OverlayRecipeButton.Pos(10, 10, itemStacks));
		}
	}
}
