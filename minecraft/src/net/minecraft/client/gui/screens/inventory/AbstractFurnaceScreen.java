package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.AbstractFurnaceRecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

@Environment(EnvType.CLIENT)
public abstract class AbstractFurnaceScreen<T extends AbstractFurnaceMenu> extends AbstractContainerScreen<T> implements RecipeUpdateListener {
	public final AbstractFurnaceRecipeBookComponent recipeBookComponent;
	private boolean widthTooNarrow;
	private final ResourceLocation texture;
	private final ResourceLocation litProgressSprite;
	private final ResourceLocation burnProgressSprite;

	public AbstractFurnaceScreen(
		T abstractFurnaceMenu,
		AbstractFurnaceRecipeBookComponent abstractFurnaceRecipeBookComponent,
		Inventory inventory,
		Component component,
		ResourceLocation resourceLocation,
		ResourceLocation resourceLocation2,
		ResourceLocation resourceLocation3
	) {
		super(abstractFurnaceMenu, inventory, component);
		this.recipeBookComponent = abstractFurnaceRecipeBookComponent;
		this.texture = resourceLocation;
		this.litProgressSprite = resourceLocation2;
		this.burnProgressSprite = resourceLocation3;
	}

	@Override
	public void init() {
		super.init();
		this.widthTooNarrow = this.width < 379;
		this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
		this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
		this.addRenderableWidget(new ImageButton(this.leftPos + 20, this.height / 2 - 49, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, button -> {
			this.recipeBookComponent.toggleVisibility();
			this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
			button.setPosition(this.leftPos + 20, this.height / 2 - 49);
		}));
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
	}

	@Override
	public void containerTick() {
		super.containerTick();
		this.recipeBookComponent.tick();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
			this.renderBackground(guiGraphics, i, j, f);
			this.recipeBookComponent.render(guiGraphics, i, j, f);
		} else {
			super.render(guiGraphics, i, j, f);
			this.recipeBookComponent.render(guiGraphics, i, j, f);
			this.recipeBookComponent.renderGhostRecipe(guiGraphics, this.leftPos, this.topPos, true, f);
		}

		this.renderTooltip(guiGraphics, i, j);
		this.recipeBookComponent.renderTooltip(guiGraphics, this.leftPos, this.topPos, i, j);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
		int k = this.leftPos;
		int l = this.topPos;
		guiGraphics.blit(this.texture, k, l, 0, 0, this.imageWidth, this.imageHeight);
		if (this.menu.isLit()) {
			int m = 14;
			int n = Mth.ceil(this.menu.getLitProgress() * 13.0F) + 1;
			guiGraphics.blitSprite(this.litProgressSprite, 14, 14, 0, 14 - n, k + 56, l + 36 + 14 - n, 14, n);
		}

		int m = 24;
		int n = Mth.ceil(this.menu.getBurnProgress() * 24.0F);
		guiGraphics.blitSprite(this.burnProgressSprite, 24, 16, 0, 0, k + 79, l + 34, n, 16);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (this.recipeBookComponent.mouseClicked(d, e, i)) {
			return true;
		} else {
			return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? true : super.mouseClicked(d, e, i);
		}
	}

	@Override
	protected void slotClicked(Slot slot, int i, int j, ClickType clickType) {
		super.slotClicked(slot, i, j, clickType);
		this.recipeBookComponent.slotClicked(slot);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		return this.recipeBookComponent.keyPressed(i, j, k) ? false : super.keyPressed(i, j, k);
	}

	@Override
	protected boolean hasClickedOutside(double d, double e, int i, int j, int k) {
		boolean bl = d < (double)i || e < (double)j || d >= (double)(i + this.imageWidth) || e >= (double)(j + this.imageHeight);
		return this.recipeBookComponent.hasClickedOutside(d, e, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, k) && bl;
	}

	@Override
	public boolean charTyped(char c, int i) {
		return this.recipeBookComponent.charTyped(c, i) ? true : super.charTyped(c, i);
	}

	@Override
	public void recipesUpdated() {
		this.recipeBookComponent.recipesUpdated();
	}

	@Override
	public RecipeBookComponent getRecipeBookComponent() {
		return this.recipeBookComponent;
	}
}
