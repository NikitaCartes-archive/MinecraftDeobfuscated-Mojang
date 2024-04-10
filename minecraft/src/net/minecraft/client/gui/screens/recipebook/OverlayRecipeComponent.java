package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
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
import net.minecraft.world.item.crafting.RecipeHolder;

@Environment(EnvType.CLIENT)
public class OverlayRecipeComponent implements Renderable, GuiEventListener {
	private static final ResourceLocation OVERLAY_RECIPE_SPRITE = new ResourceLocation("recipe_book/overlay_recipe");
	static final ResourceLocation FURNACE_OVERLAY_HIGHLIGHTED_SPRITE = new ResourceLocation("recipe_book/furnace_overlay_highlighted");
	static final ResourceLocation FURNACE_OVERLAY_SPRITE = new ResourceLocation("recipe_book/furnace_overlay");
	static final ResourceLocation CRAFTING_OVERLAY_HIGHLIGHTED_SPRITE = new ResourceLocation("recipe_book/crafting_overlay_highlighted");
	static final ResourceLocation CRAFTING_OVERLAY_SPRITE = new ResourceLocation("recipe_book/crafting_overlay");
	static final ResourceLocation FURNACE_OVERLAY_DISABLED_HIGHLIGHTED_SPRITE = new ResourceLocation("recipe_book/furnace_overlay_disabled_highlighted");
	static final ResourceLocation FURNACE_OVERLAY_DISABLED_SPRITE = new ResourceLocation("recipe_book/furnace_overlay_disabled");
	static final ResourceLocation CRAFTING_OVERLAY_DISABLED_HIGHLIGHTED_SPRITE = new ResourceLocation("recipe_book/crafting_overlay_disabled_highlighted");
	static final ResourceLocation CRAFTING_OVERLAY_DISABLED_SPRITE = new ResourceLocation("recipe_book/crafting_overlay_disabled");
	private static final int MAX_ROW = 4;
	private static final int MAX_ROW_LARGE = 5;
	private static final float ITEM_RENDER_SCALE = 0.375F;
	public static final int BUTTON_SIZE = 25;
	private final List<OverlayRecipeComponent.OverlayRecipeButton> recipeButtons = Lists.<OverlayRecipeComponent.OverlayRecipeButton>newArrayList();
	private boolean isVisible;
	private int x;
	private int y;
	private Minecraft minecraft;
	private RecipeCollection collection;
	@Nullable
	private RecipeHolder<?> lastRecipeClicked;
	float time;
	boolean isFurnaceMenu;

	public void init(Minecraft minecraft, RecipeCollection recipeCollection, int i, int j, int k, int l, float f) {
		this.minecraft = minecraft;
		this.collection = recipeCollection;
		if (minecraft.player.containerMenu instanceof AbstractFurnaceMenu) {
			this.isFurnaceMenu = true;
		}

		boolean bl = minecraft.player.getRecipeBook().isFiltering((RecipeBookMenu<?>)minecraft.player.containerMenu);
		List<RecipeHolder<?>> list = recipeCollection.getDisplayRecipes(true);
		List<RecipeHolder<?>> list2 = bl ? Collections.emptyList() : recipeCollection.getDisplayRecipes(false);
		int m = list.size();
		int n = m + list2.size();
		int o = n <= 16 ? 4 : 5;
		int p = (int)Math.ceil((double)((float)n / (float)o));
		this.x = i;
		this.y = j;
		float g = (float)(this.x + Math.min(n, o) * 25);
		float h = (float)(k + 50);
		if (g > h) {
			this.x = (int)((float)this.x - f * (float)((int)((g - h) / f)));
		}

		float q = (float)(this.y + p * 25);
		float r = (float)(l + 50);
		if (q > r) {
			this.y = (int)((float)this.y - f * (float)Mth.ceil((q - r) / f));
		}

		float s = (float)this.y;
		float t = (float)(l - 100);
		if (s < t) {
			this.y = (int)((float)this.y - f * (float)Mth.ceil((s - t) / f));
		}

		this.isVisible = true;
		this.recipeButtons.clear();

		for (int u = 0; u < n; u++) {
			boolean bl2 = u < m;
			RecipeHolder<?> recipeHolder = bl2 ? (RecipeHolder)list.get(u) : (RecipeHolder)list2.get(u - m);
			int v = this.x + 4 + 25 * (u % o);
			int w = this.y + 5 + 25 * (u / o);
			if (this.isFurnaceMenu) {
				this.recipeButtons.add(new OverlayRecipeComponent.OverlaySmeltingRecipeButton(v, w, recipeHolder, bl2));
			} else {
				this.recipeButtons.add(new OverlayRecipeComponent.OverlayRecipeButton(v, w, recipeHolder, bl2));
			}
		}

		this.lastRecipeClicked = null;
	}

	public RecipeCollection getRecipeCollection() {
		return this.collection;
	}

	@Nullable
	public RecipeHolder<?> getLastRecipeClicked() {
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
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.isVisible) {
			this.time += f;
			RenderSystem.enableBlend();
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0.0F, 0.0F, 1000.0F);
			int k = this.recipeButtons.size() <= 16 ? 4 : 5;
			int l = Math.min(this.recipeButtons.size(), k);
			int m = Mth.ceil((float)this.recipeButtons.size() / (float)k);
			int n = 4;
			guiGraphics.blitSprite(OVERLAY_RECIPE_SPRITE, this.x, this.y, l * 25 + 8, m * 25 + 8);
			RenderSystem.disableBlend();

			for (OverlayRecipeComponent.OverlayRecipeButton overlayRecipeButton : this.recipeButtons) {
				overlayRecipeButton.render(guiGraphics, i, j, f);
			}

			guiGraphics.pose().popPose();
		}
	}

	public void setVisible(boolean bl) {
		this.isVisible = bl;
	}

	public boolean isVisible() {
		return this.isVisible;
	}

	@Override
	public void setFocused(boolean bl) {
	}

	@Override
	public boolean isFocused() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	class OverlayRecipeButton extends AbstractWidget implements PlaceRecipe<Ingredient> {
		final RecipeHolder<?> recipe;
		private final boolean isCraftable;
		protected final List<OverlayRecipeComponent.OverlayRecipeButton.Pos> ingredientPos = Lists.<OverlayRecipeComponent.OverlayRecipeButton.Pos>newArrayList();

		public OverlayRecipeButton(final int i, final int j, final RecipeHolder<?> recipeHolder, final boolean bl) {
			super(i, j, 200, 20, CommonComponents.EMPTY);
			this.width = 24;
			this.height = 24;
			this.recipe = recipeHolder;
			this.isCraftable = bl;
			this.calculateIngredientsPositions(recipeHolder);
		}

		protected void calculateIngredientsPositions(RecipeHolder<?> recipeHolder) {
			this.placeRecipe(3, 3, -1, recipeHolder, recipeHolder.value().getIngredients().iterator(), 0);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
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
		public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
			ResourceLocation resourceLocation;
			if (this.isCraftable) {
				if (OverlayRecipeComponent.this.isFurnaceMenu) {
					resourceLocation = this.isHoveredOrFocused() ? OverlayRecipeComponent.FURNACE_OVERLAY_HIGHLIGHTED_SPRITE : OverlayRecipeComponent.FURNACE_OVERLAY_SPRITE;
				} else {
					resourceLocation = this.isHoveredOrFocused() ? OverlayRecipeComponent.CRAFTING_OVERLAY_HIGHLIGHTED_SPRITE : OverlayRecipeComponent.CRAFTING_OVERLAY_SPRITE;
				}
			} else if (OverlayRecipeComponent.this.isFurnaceMenu) {
				resourceLocation = this.isHoveredOrFocused()
					? OverlayRecipeComponent.FURNACE_OVERLAY_DISABLED_HIGHLIGHTED_SPRITE
					: OverlayRecipeComponent.FURNACE_OVERLAY_DISABLED_SPRITE;
			} else {
				resourceLocation = this.isHoveredOrFocused()
					? OverlayRecipeComponent.CRAFTING_OVERLAY_DISABLED_HIGHLIGHTED_SPRITE
					: OverlayRecipeComponent.CRAFTING_OVERLAY_DISABLED_SPRITE;
			}

			guiGraphics.blitSprite(resourceLocation, this.getX(), this.getY(), this.width, this.height);
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate((double)(this.getX() + 2), (double)(this.getY() + 2), 150.0);

			for (OverlayRecipeComponent.OverlayRecipeButton.Pos pos : this.ingredientPos) {
				guiGraphics.pose().pushPose();
				guiGraphics.pose().translate((double)pos.x, (double)pos.y, 0.0);
				guiGraphics.pose().scale(0.375F, 0.375F, 1.0F);
				guiGraphics.pose().translate(-8.0, -8.0, 0.0);
				if (pos.ingredients.length > 0) {
					guiGraphics.renderItem(pos.ingredients[Mth.floor(OverlayRecipeComponent.this.time / 30.0F) % pos.ingredients.length], 0, 0);
				}

				guiGraphics.pose().popPose();
			}

			guiGraphics.pose().popPose();
		}

		@Environment(EnvType.CLIENT)
		protected class Pos {
			public final ItemStack[] ingredients;
			public final int x;
			public final int y;

			public Pos(final int i, final int j, final ItemStack[] itemStacks) {
				this.x = i;
				this.y = j;
				this.ingredients = itemStacks;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class OverlaySmeltingRecipeButton extends OverlayRecipeComponent.OverlayRecipeButton {
		public OverlaySmeltingRecipeButton(final int i, final int j, final RecipeHolder<?> recipeHolder, final boolean bl) {
			super(i, j, recipeHolder, bl);
		}

		@Override
		protected void calculateIngredientsPositions(RecipeHolder<?> recipeHolder) {
			Ingredient ingredient = recipeHolder.value().getIngredients().get(0);
			ItemStack[] itemStacks = ingredient.getItems();
			this.ingredientPos.add(new OverlayRecipeComponent.OverlayRecipeButton.Pos(10, 10, itemStacks));
		}
	}
}
