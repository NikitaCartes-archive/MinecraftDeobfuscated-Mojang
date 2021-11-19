package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.Recipe;

@Environment(EnvType.CLIENT)
public class RecipeBookPage {
	public static final int ITEMS_PER_PAGE = 20;
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
	private Recipe<?> lastClickedRecipe;
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
		this.forwardButton.initTextureValues(1, 208, 13, 18, RecipeBookComponent.RECIPE_BOOK_LOCATION);
		this.backButton = new StateSwitchingButton(i + 38, j + 137, 12, 17, true);
		this.backButton.initTextureValues(1, 208, 13, 18, RecipeBookComponent.RECIPE_BOOK_LOCATION);
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

	public void render(PoseStack poseStack, int i, int j, int k, int l, float f) {
		if (this.totalPages > 1) {
			String string = this.currentPage + 1 + "/" + this.totalPages;
			int m = this.minecraft.font.width(string);
			this.minecraft.font.draw(poseStack, string, (float)(i - m / 2 + 73), (float)(j + 141), -1);
		}

		this.hoveredButton = null;

		for (RecipeButton recipeButton : this.buttons) {
			recipeButton.render(poseStack, k, l, f);
			if (recipeButton.visible && recipeButton.isHoveredOrFocused()) {
				this.hoveredButton = recipeButton;
			}
		}

		this.backButton.render(poseStack, k, l, f);
		this.forwardButton.render(poseStack, k, l, f);
		this.overlay.render(poseStack, k, l, f);
	}

	public void renderTooltip(PoseStack poseStack, int i, int j) {
		if (this.minecraft.screen != null && this.hoveredButton != null && !this.overlay.isVisible()) {
			this.minecraft.screen.renderComponentTooltip(poseStack, this.hoveredButton.getTooltipText(this.minecraft.screen), i, j);
		}
	}

	@Nullable
	public Recipe<?> getLastClickedRecipe() {
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
							.init(this.minecraft, recipeButton.getCollection(), recipeButton.x, recipeButton.y, j + l / 2, k + 13 + m / 2, (float)recipeButton.getWidth());
					}

					return true;
				}
			}

			return false;
		}
	}

	public void recipesShown(List<Recipe<?>> list) {
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
