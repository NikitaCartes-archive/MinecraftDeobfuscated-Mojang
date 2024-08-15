package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

@Environment(EnvType.CLIENT)
public class OverlayRecipeComponent implements Renderable, GuiEventListener {
	private static final ResourceLocation OVERLAY_RECIPE_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/overlay_recipe");
	private static final int MAX_ROW = 4;
	private static final int MAX_ROW_LARGE = 5;
	private static final float ITEM_RENDER_SCALE = 0.375F;
	public static final int BUTTON_SIZE = 25;
	private final List<OverlayRecipeComponent.OverlayRecipeButton> recipeButtons = Lists.<OverlayRecipeComponent.OverlayRecipeButton>newArrayList();
	private boolean isVisible;
	private int x;
	private int y;
	private RecipeCollection collection;
	@Nullable
	private RecipeHolder<?> lastRecipeClicked;
	final SlotSelectTime slotSelectTime;
	private final boolean isFurnaceMenu;

	public OverlayRecipeComponent(SlotSelectTime slotSelectTime, boolean bl) {
		this.slotSelectTime = slotSelectTime;
		this.isFurnaceMenu = bl;
	}

	public void init(RecipeCollection recipeCollection, boolean bl, int i, int j, int k, int l, float f) {
		this.collection = recipeCollection;
		List<RecipeHolder<?>> list = recipeCollection.getFittingRecipes(RecipeCollection.CraftableStatus.CRAFTABLE);
		List<RecipeHolder<?>> list2 = bl ? Collections.emptyList() : recipeCollection.getFittingRecipes(RecipeCollection.CraftableStatus.NOT_CRAFTABLE);
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
				this.recipeButtons.add(new OverlayRecipeComponent.OverlayCraftingRecipeButton(v, w, recipeHolder, bl2));
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
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0.0F, 0.0F, 1000.0F);
			int k = this.recipeButtons.size() <= 16 ? 4 : 5;
			int l = Math.min(this.recipeButtons.size(), k);
			int m = Mth.ceil((float)this.recipeButtons.size() / (float)k);
			int n = 4;
			guiGraphics.blitSprite(RenderType::guiTextured, OVERLAY_RECIPE_SPRITE, this.x, this.y, l * 25 + 8, m * 25 + 8);

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
	class OverlayCraftingRecipeButton extends OverlayRecipeComponent.OverlayRecipeButton {
		private static final ResourceLocation ENABLED_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/crafting_overlay");
		private static final ResourceLocation HIGHLIGHTED_ENABLED_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/crafting_overlay_highlighted");
		private static final ResourceLocation DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/crafting_overlay_disabled");
		private static final ResourceLocation HIGHLIGHTED_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/crafting_overlay_disabled_highlighted");

		public OverlayCraftingRecipeButton(final int i, final int j, final RecipeHolder<?> recipeHolder, final boolean bl) {
			super(i, j, recipeHolder, bl, calculateIngredientsPositions(recipeHolder));
		}

		private static List<OverlayRecipeComponent.OverlayRecipeButton.Pos> calculateIngredientsPositions(RecipeHolder<?> recipeHolder) {
			List<OverlayRecipeComponent.OverlayRecipeButton.Pos> list = new ArrayList();
			PlaceRecipeHelper.placeRecipe(
				3,
				3,
				recipeHolder,
				recipeHolder.value().placementInfo().slotInfo(),
				(optional, i, j, k) -> optional.ifPresent(slotInfo -> list.add(createGridPos(j, k, slotInfo.possibleItems())))
			);
			return list;
		}

		@Override
		protected ResourceLocation getSprite(boolean bl) {
			if (bl) {
				return this.isHoveredOrFocused() ? HIGHLIGHTED_ENABLED_SPRITE : ENABLED_SPRITE;
			} else {
				return this.isHoveredOrFocused() ? HIGHLIGHTED_DISABLED_SPRITE : DISABLED_SPRITE;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	abstract class OverlayRecipeButton extends AbstractWidget {
		final RecipeHolder<?> recipe;
		private final boolean isCraftable;
		private final List<OverlayRecipeComponent.OverlayRecipeButton.Pos> slots;

		public OverlayRecipeButton(
			final int i, final int j, final RecipeHolder<?> recipeHolder, final boolean bl, final List<OverlayRecipeComponent.OverlayRecipeButton.Pos> list
		) {
			super(i, j, 24, 24, CommonComponents.EMPTY);
			this.slots = list;
			this.recipe = recipeHolder;
			this.isCraftable = bl;
		}

		protected static OverlayRecipeComponent.OverlayRecipeButton.Pos createGridPos(int i, int j, List<ItemStack> list) {
			return new OverlayRecipeComponent.OverlayRecipeButton.Pos(3 + i * 7, 3 + j * 7, list);
		}

		protected abstract ResourceLocation getSprite(boolean bl);

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
			this.defaultButtonNarrationText(narrationElementOutput);
		}

		@Override
		public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
			guiGraphics.blitSprite(RenderType::guiTextured, this.getSprite(this.isCraftable), this.getX(), this.getY(), this.width, this.height);
			float g = (float)(this.getX() + 2);
			float h = (float)(this.getY() + 2);
			float k = 150.0F;

			for (OverlayRecipeComponent.OverlayRecipeButton.Pos pos : this.slots) {
				guiGraphics.pose().pushPose();
				guiGraphics.pose().translate(g + (float)pos.x, h + (float)pos.y, 150.0F);
				guiGraphics.pose().scale(0.375F, 0.375F, 1.0F);
				guiGraphics.pose().translate(-8.0F, -8.0F, 0.0F);
				guiGraphics.renderItem(pos.selectIngredient(OverlayRecipeComponent.this.slotSelectTime.currentIndex()), 0, 0);
				guiGraphics.pose().popPose();
			}
		}

		@Environment(EnvType.CLIENT)
		protected static record Pos(int x, int y, List<ItemStack> ingredients) {

			public Pos(int x, int y, List<ItemStack> ingredients) {
				if (ingredients.isEmpty()) {
					throw new IllegalArgumentException("Ingredient list must be non-empty");
				} else {
					this.x = x;
					this.y = y;
					this.ingredients = ingredients;
				}
			}

			public ItemStack selectIngredient(int i) {
				return (ItemStack)this.ingredients.get(i % this.ingredients.size());
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class OverlaySmeltingRecipeButton extends OverlayRecipeComponent.OverlayRecipeButton {
		private static final ResourceLocation ENABLED_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/furnace_overlay");
		private static final ResourceLocation HIGHLIGHTED_ENABLED_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/furnace_overlay_highlighted");
		private static final ResourceLocation DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/furnace_overlay_disabled");
		private static final ResourceLocation HIGHLIGHTED_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("recipe_book/furnace_overlay_disabled_highlighted");

		public OverlaySmeltingRecipeButton(final int i, final int j, final RecipeHolder<?> recipeHolder, final boolean bl) {
			super(i, j, recipeHolder, bl, calculateIngredientsPositions(recipeHolder));
		}

		private static List<OverlayRecipeComponent.OverlayRecipeButton.Pos> calculateIngredientsPositions(RecipeHolder<?> recipeHolder) {
			return (List<OverlayRecipeComponent.OverlayRecipeButton.Pos>)((Optional)recipeHolder.value().placementInfo().slotInfo().getFirst())
				.map(slotInfo -> List.of(createGridPos(1, 1, slotInfo.possibleItems())))
				.orElse(List.of());
		}

		@Override
		protected ResourceLocation getSprite(boolean bl) {
			if (bl) {
				return this.isHoveredOrFocused() ? HIGHLIGHTED_ENABLED_SPRITE : ENABLED_SPRITE;
			} else {
				return this.isHoveredOrFocused() ? HIGHLIGHTED_DISABLED_SPRITE : DISABLED_SPRITE;
			}
		}
	}
}
