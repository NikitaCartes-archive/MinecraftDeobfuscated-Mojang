package net.minecraft.client.gui.screens.inventory;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.PoisonousPotatoCutterMenu;
import net.minecraft.world.item.crafting.PoisonousPotatoCutterRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

@Environment(EnvType.CLIENT)
public class PoisonousPotatoCutterScreen extends AbstractContainerScreen<PoisonousPotatoCutterMenu> {
	private static final ResourceLocation SCROLLER_SPRITE = new ResourceLocation("container/stonecutter/scroller");
	private static final ResourceLocation SCROLLER_DISABLED_SPRITE = new ResourceLocation("container/stonecutter/scroller_disabled");
	private static final ResourceLocation RECIPE_SELECTED_SPRITE = new ResourceLocation("container/stonecutter/recipe_selected");
	private static final ResourceLocation RECIPE_HIGHLIGHTED_SPRITE = new ResourceLocation("container/stonecutter/recipe_highlighted");
	private static final ResourceLocation RECIPE_SPRITE = new ResourceLocation("container/stonecutter/recipe");
	private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/stonecutter.png");
	private static final int SCROLLER_WIDTH = 12;
	private static final int SCROLLER_HEIGHT = 15;
	private static final int RECIPES_COLUMNS = 4;
	private static final int RECIPES_ROWS = 3;
	private static final int RECIPES_IMAGE_SIZE_WIDTH = 16;
	private static final int RECIPES_IMAGE_SIZE_HEIGHT = 18;
	private static final int SCROLLER_FULL_HEIGHT = 54;
	private static final int RECIPES_X = 52;
	private static final int RECIPES_Y = 14;
	private float scrollOffs;
	private boolean scrolling;
	private int startIndex;
	private boolean displayRecipes;

	public PoisonousPotatoCutterScreen(PoisonousPotatoCutterMenu poisonousPotatoCutterMenu, Inventory inventory, Component component) {
		super(poisonousPotatoCutterMenu, inventory, component);
		poisonousPotatoCutterMenu.registerUpdateListener(this::containerChanged);
		this.titleLabelY--;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.renderTooltip(guiGraphics, i, j);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
		int k = this.leftPos;
		int l = this.topPos;
		guiGraphics.blit(BG_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
		int m = (int)(41.0F * this.scrollOffs);
		ResourceLocation resourceLocation = this.isScrollBarActive() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
		guiGraphics.blitSprite(resourceLocation, k + 119, l + 15 + m, 12, 15);
		int n = this.leftPos + 52;
		int o = this.topPos + 14;
		int p = this.startIndex + 12;
		this.renderButtons(guiGraphics, i, j, n, o, p);
		this.renderRecipes(guiGraphics, n, o, p);
	}

	@Override
	protected void renderTooltip(GuiGraphics guiGraphics, int i, int j) {
		super.renderTooltip(guiGraphics, i, j);
		if (this.displayRecipes) {
			int k = this.leftPos + 52;
			int l = this.topPos + 14;
			int m = this.startIndex + 12;
			List<RecipeHolder<PoisonousPotatoCutterRecipe>> list = this.menu.getRecipes();

			for (int n = this.startIndex; n < m && n < this.menu.getNumRecipes(); n++) {
				int o = n - this.startIndex;
				int p = k + o % 4 * 16;
				int q = l + o / 4 * 18 + 2;
				if (i >= p && i < p + 16 && j >= q && j < q + 18) {
					guiGraphics.renderTooltip(
						this.font, ((PoisonousPotatoCutterRecipe)((RecipeHolder)list.get(n)).value()).getResultItem(this.minecraft.level.registryAccess()), i, j
					);
				}
			}
		}
	}

	private void renderButtons(GuiGraphics guiGraphics, int i, int j, int k, int l, int m) {
		for (int n = this.startIndex; n < m && n < this.menu.getNumRecipes(); n++) {
			int o = n - this.startIndex;
			int p = k + o % 4 * 16;
			int q = o / 4;
			int r = l + q * 18 + 2;
			ResourceLocation resourceLocation;
			if (n == this.menu.getSelectedRecipeIndex()) {
				resourceLocation = RECIPE_SELECTED_SPRITE;
			} else if (i >= p && j >= r && i < p + 16 && j < r + 18) {
				resourceLocation = RECIPE_HIGHLIGHTED_SPRITE;
			} else {
				resourceLocation = RECIPE_SPRITE;
			}

			guiGraphics.blitSprite(resourceLocation, p, r - 1, 16, 18);
		}
	}

	private void renderRecipes(GuiGraphics guiGraphics, int i, int j, int k) {
		List<RecipeHolder<PoisonousPotatoCutterRecipe>> list = this.menu.getRecipes();

		for (int l = this.startIndex; l < k && l < this.menu.getNumRecipes(); l++) {
			int m = l - this.startIndex;
			int n = i + m % 4 * 16;
			int o = m / 4;
			int p = j + o * 18 + 2;
			guiGraphics.renderItem(((PoisonousPotatoCutterRecipe)((RecipeHolder)list.get(l)).value()).getResultItem(this.minecraft.level.registryAccess()), n, p);
		}
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		this.scrolling = false;
		if (this.displayRecipes) {
			int j = this.leftPos + 52;
			int k = this.topPos + 14;
			int l = this.startIndex + 12;

			for (int m = this.startIndex; m < l; m++) {
				int n = m - this.startIndex;
				double f = d - (double)(j + n % 4 * 16);
				double g = e - (double)(k + n / 4 * 18);
				if (f >= 0.0 && g >= 0.0 && f < 16.0 && g < 18.0 && this.menu.clickMenuButton(this.minecraft.player, m)) {
					Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
					this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, m);
					return true;
				}
			}

			j = this.leftPos + 119;
			k = this.topPos + 9;
			if (d >= (double)j && d < (double)(j + 12) && e >= (double)k && e < (double)(k + 54)) {
				this.scrolling = true;
			}
		}

		return super.mouseClicked(d, e, i);
	}

	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		if (this.scrolling && this.isScrollBarActive()) {
			int j = this.topPos + 14;
			int k = j + 54;
			this.scrollOffs = ((float)e - (float)j - 7.5F) / ((float)(k - j) - 15.0F);
			this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
			this.startIndex = (int)((double)(this.scrollOffs * (float)this.getOffscreenRows()) + 0.5) * 4;
			return true;
		} else {
			return super.mouseDragged(d, e, i, f, g);
		}
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f, double g) {
		if (this.isScrollBarActive()) {
			int i = this.getOffscreenRows();
			float h = (float)g / (float)i;
			this.scrollOffs = Mth.clamp(this.scrollOffs - h, 0.0F, 1.0F);
			this.startIndex = (int)((double)(this.scrollOffs * (float)i) + 0.5) * 4;
		}

		return true;
	}

	private boolean isScrollBarActive() {
		return this.displayRecipes && this.menu.getNumRecipes() > 12;
	}

	protected int getOffscreenRows() {
		return (this.menu.getNumRecipes() + 4 - 1) / 4 - 3;
	}

	private void containerChanged() {
		this.displayRecipes = this.menu.hasInputItem();
		if (!this.displayRecipes) {
			this.scrollOffs = 0.0F;
			this.startIndex = 0;
		}
	}
}
