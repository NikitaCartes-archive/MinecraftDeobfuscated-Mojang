package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

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
	private RecipeDisplayId lastRecipeClicked;
	final SlotSelectTime slotSelectTime;
	private final boolean isFurnaceMenu;

	public OverlayRecipeComponent(SlotSelectTime slotSelectTime, boolean bl) {
		this.slotSelectTime = slotSelectTime;
		this.isFurnaceMenu = bl;
	}

	public void init(RecipeCollection recipeCollection, ContextMap contextMap, boolean bl, int i, int j, int k, int l, float f) {
		this.collection = recipeCollection;
		List<RecipeDisplayEntry> list = recipeCollection.getSelectedRecipes(RecipeCollection.CraftableStatus.CRAFTABLE);
		List<RecipeDisplayEntry> list2 = bl ? Collections.emptyList() : recipeCollection.getSelectedRecipes(RecipeCollection.CraftableStatus.NOT_CRAFTABLE);
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
			RecipeDisplayEntry recipeDisplayEntry = bl2 ? (RecipeDisplayEntry)list.get(u) : (RecipeDisplayEntry)list2.get(u - m);
			int v = this.x + 4 + 25 * (u % o);
			int w = this.y + 5 + 25 * (u / o);
			if (this.isFurnaceMenu) {
				this.recipeButtons
					.add(new OverlayRecipeComponent.OverlaySmeltingRecipeButton(v, w, recipeDisplayEntry.id(), recipeDisplayEntry.display(), contextMap, bl2));
			} else {
				this.recipeButtons
					.add(new OverlayRecipeComponent.OverlayCraftingRecipeButton(v, w, recipeDisplayEntry.id(), recipeDisplayEntry.display(), contextMap, bl2));
			}
		}

		this.lastRecipeClicked = null;
	}

	public RecipeCollection getRecipeCollection() {
		return this.collection;
	}

	@Nullable
	public RecipeDisplayId getLastRecipeClicked() {
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
		private static final int GRID_WIDTH = 3;
		private static final int GRID_HEIGHT = 3;

		public OverlayCraftingRecipeButton(
			final int i, final int j, final RecipeDisplayId recipeDisplayId, final RecipeDisplay recipeDisplay, final ContextMap contextMap, final boolean bl
		) {
			super(i, j, recipeDisplayId, bl, calculateIngredientsPositions(recipeDisplay, contextMap));
		}

		private static List<OverlayRecipeComponent.OverlayRecipeButton.Pos> calculateIngredientsPositions(RecipeDisplay recipeDisplay, ContextMap contextMap) {
			List<OverlayRecipeComponent.OverlayRecipeButton.Pos> list = new ArrayList();
			Objects.requireNonNull(recipeDisplay);
			switch (recipeDisplay) {
				case ShapedCraftingRecipeDisplay shapedCraftingRecipeDisplay:
					PlaceRecipeHelper.placeRecipe(
						3, 3, shapedCraftingRecipeDisplay.width(), shapedCraftingRecipeDisplay.height(), shapedCraftingRecipeDisplay.ingredients(), (slotDisplay, ix, j, k) -> {
							List<ItemStack> list2x = slotDisplay.resolveForStacks(contextMap);
							if (!list2x.isEmpty()) {
								list.add(createGridPos(j, k, list2x));
							}
						}
					);
					break;
				case ShapelessCraftingRecipeDisplay shapelessCraftingRecipeDisplay:
					label19: {
						List<SlotDisplay> list2 = shapelessCraftingRecipeDisplay.ingredients();

						for (int i = 0; i < list2.size(); i++) {
							List<ItemStack> list3 = ((SlotDisplay)list2.get(i)).resolveForStacks(contextMap);
							if (!list3.isEmpty()) {
								list.add(createGridPos(i % 3, i / 3, list3));
							}
						}
						break label19;
					}
			}

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
		final RecipeDisplayId recipe;
		private final boolean isCraftable;
		private final List<OverlayRecipeComponent.OverlayRecipeButton.Pos> slots;

		public OverlayRecipeButton(
			final int i, final int j, final RecipeDisplayId recipeDisplayId, final boolean bl, final List<OverlayRecipeComponent.OverlayRecipeButton.Pos> list
		) {
			super(i, j, 24, 24, CommonComponents.EMPTY);
			this.slots = list;
			this.recipe = recipeDisplayId;
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

		public OverlaySmeltingRecipeButton(
			final int i, final int j, final RecipeDisplayId recipeDisplayId, final RecipeDisplay recipeDisplay, final ContextMap contextMap, final boolean bl
		) {
			super(i, j, recipeDisplayId, bl, calculateIngredientsPositions(recipeDisplay, contextMap));
		}

		private static List<OverlayRecipeComponent.OverlayRecipeButton.Pos> calculateIngredientsPositions(RecipeDisplay recipeDisplay, ContextMap contextMap) {
			if (recipeDisplay instanceof FurnaceRecipeDisplay furnaceRecipeDisplay) {
				List<ItemStack> list = furnaceRecipeDisplay.ingredient().resolveForStacks(contextMap);
				if (!list.isEmpty()) {
					return List.of(createGridPos(1, 1, list));
				}
			}

			return List.of();
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
