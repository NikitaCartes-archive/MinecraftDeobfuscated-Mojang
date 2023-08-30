package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.RecipeHolder;

@Environment(EnvType.CLIENT)
public class RecipeBookPage {
	public static final int ITEMS_PER_PAGE = 20;
	private static final WidgetSprites PAGE_FORWARD_SPRITES = new WidgetSprites(
		new ResourceLocation("recipe_book/page_forward"), new ResourceLocation("recipe_book/page_forward_highlighted")
	);
	private static final WidgetSprites PAGE_BACKWARD_SPRITES = new WidgetSprites(
		new ResourceLocation("recipe_book/page_backward"), new ResourceLocation("recipe_book/page_backward_highlighted")
	);
	private final List<RecipeButton> buttons = Lists.<RecipeButton>newArrayListWithCapacity(20);
	@Nullable
	private RecipeButton hoveredButton;
	private final OverlayRecipeComponent overlay = new OverlayRecipeComponent();
	private Minecraft minecraft;
	private final List<RecipeShownListener> showListeners = Lists.<RecipeShownListener>newArrayList();
	private List<RecipeCollection> recipeCollections = ImmutableList.of();
	private StateSwitchingButton forwardButton;
	private StateSwitchingButton backButton;
	private int totalPages;
	private int currentPage;
	private RecipeBook recipeBook;
	@Nullable
	private RecipeHolder<?> lastClickedRecipe;
	@Nullable
	private RecipeCollection lastClickedRecipeCollection;

	public RecipeBookPage() {
		for (int i = 0; i < 20; i++) {
			this.buttons.add(new RecipeButton());
		}
	}

	public void init(Minecraft minecraft, int i, int j) {
		this.minecraft = minecraft;
		this.recipeBook = minecraft.player.getRecipeBook();

		for (int k = 0; k < this.buttons.size(); k++) {
			((RecipeButton)this.buttons.get(k)).setPosition(i + 11 + 25 * (k % 5), j + 31 + 25 * (k / 5));
		}

		this.forwardButton = new StateSwitchingButton(i + 93, j + 137, 12, 17, false);
		this.forwardButton.initTextureValues(PAGE_FORWARD_SPRITES);
		this.backButton = new StateSwitchingButton(i + 38, j + 137, 12, 17, true);
		this.backButton.initTextureValues(PAGE_BACKWARD_SPRITES);
	}

	public void addListener(RecipeBookComponent recipeBookComponent) {
		this.showListeners.remove(recipeBookComponent);
		this.showListeners.add(recipeBookComponent);
	}

	public void updateCollections(List<RecipeCollection> list, boolean bl) {
		this.recipeCollections = list;
		this.totalPages = (int)Math.ceil((double)list.size() / 20.0);
		if (this.totalPages <= this.currentPage || bl) {
			this.currentPage = 0;
		}

		this.updateButtonsForPage();
	}

	private void updateButtonsForPage() {
		int i = 20 * this.currentPage;

		for (int j = 0; j < this.buttons.size(); j++) {
			RecipeButton recipeButton = (RecipeButton)this.buttons.get(j);
			if (i + j < this.recipeCollections.size()) {
				RecipeCollection recipeCollection = (RecipeCollection)this.recipeCollections.get(i + j);
				recipeButton.init(recipeCollection, this);
				recipeButton.visible = true;
			} else {
				recipeButton.visible = false;
			}
		}

		this.updateArrowButtons();
	}

	private void updateArrowButtons() {
		this.forwardButton.visible = this.totalPages > 1 && this.currentPage < this.totalPages - 1;
		this.backButton.visible = this.totalPages > 1 && this.currentPage > 0;
	}

	public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, float f) {
		if (this.totalPages > 1) {
			String string = this.currentPage + 1 + "/" + this.totalPages;
			int m = this.minecraft.font.width(string);
			guiGraphics.drawString(this.minecraft.font, string, i - m / 2 + 73, j + 141, -1, false);
		}

		this.hoveredButton = null;

		for (RecipeButton recipeButton : this.buttons) {
			recipeButton.render(guiGraphics, k, l, f);
			if (recipeButton.visible && recipeButton.isHoveredOrFocused()) {
				this.hoveredButton = recipeButton;
			}
		}

		this.backButton.render(guiGraphics, k, l, f);
		this.forwardButton.render(guiGraphics, k, l, f);
		this.overlay.render(guiGraphics, k, l, f);
	}

	public void renderTooltip(GuiGraphics guiGraphics, int i, int j) {
		if (this.minecraft.screen != null && this.hoveredButton != null && !this.overlay.isVisible()) {
			guiGraphics.renderComponentTooltip(this.minecraft.font, this.hoveredButton.getTooltipText(), i, j);
		}
	}

	@Nullable
	public RecipeHolder<?> getLastClickedRecipe() {
		return this.lastClickedRecipe;
	}

	@Nullable
	public RecipeCollection getLastClickedRecipeCollection() {
		return this.lastClickedRecipeCollection;
	}

	public void setInvisible() {
		this.overlay.setVisible(false);
	}

	public boolean mouseClicked(double d, double e, int i, int j, int k, int l, int m) {
		this.lastClickedRecipe = null;
		this.lastClickedRecipeCollection = null;
		if (this.overlay.isVisible()) {
			if (this.overlay.mouseClicked(d, e, i)) {
				this.lastClickedRecipe = this.overlay.getLastRecipeClicked();
				this.lastClickedRecipeCollection = this.overlay.getRecipeCollection();
			} else {
				this.overlay.setVisible(false);
			}

			return true;
		} else if (this.forwardButton.mouseClicked(d, e, i)) {
			this.currentPage++;
			this.updateButtonsForPage();
			return true;
		} else if (this.backButton.mouseClicked(d, e, i)) {
			this.currentPage--;
			this.updateButtonsForPage();
			return true;
		} else {
			for (RecipeButton recipeButton : this.buttons) {
				if (recipeButton.mouseClicked(d, e, i)) {
					if (i == 0) {
						this.lastClickedRecipe = recipeButton.getRecipe();
						this.lastClickedRecipeCollection = recipeButton.getCollection();
					} else if (i == 1 && !this.overlay.isVisible() && !recipeButton.isOnlyOption()) {
						this.overlay
							.init(this.minecraft, recipeButton.getCollection(), recipeButton.getX(), recipeButton.getY(), j + l / 2, k + 13 + m / 2, (float)recipeButton.getWidth());
					}

					return true;
				}
			}

			return false;
		}
	}

	public void recipesShown(List<RecipeHolder<?>> list) {
		for (RecipeShownListener recipeShownListener : this.showListeners) {
			recipeShownListener.recipesShown(list);
		}
	}

	public Minecraft getMinecraft() {
		return this.minecraft;
	}

	public RecipeBook getRecipeBook() {
		return this.recipeBook;
	}

	protected void listButtons(Consumer<AbstractWidget> consumer) {
		consumer.accept(this.forwardButton);
		consumer.accept(this.backButton);
		this.buttons.forEach(consumer);
	}
}
