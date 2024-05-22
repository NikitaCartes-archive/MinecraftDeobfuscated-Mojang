package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;

@Environment(EnvType.CLIENT)
public class CraftingScreen extends AbstractContainerScreen<CraftingMenu> implements RecipeUpdateListener {
	private static final ResourceLocation CRAFTING_TABLE_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");
	private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
	private boolean widthTooNarrow;

	public CraftingScreen(CraftingMenu craftingMenu, Inventory inventory, Component component) {
		super(craftingMenu, inventory, component);
	}

	@Override
	protected void init() {
		super.init();
		this.widthTooNarrow = this.width < 379;
		this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
		this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
		this.addRenderableWidget(new ImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, button -> {
			this.recipeBookComponent.toggleVisibility();
			this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
			button.setPosition(this.leftPos + 5, this.height / 2 - 49);
		}));
		this.addWidget(this.recipeBookComponent);
		this.titleLabelX = 29;
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
		int l = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(CRAFTING_TABLE_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		return this.recipeBookComponent.keyPressed(i, j, k) ? true : super.keyPressed(i, j, k);
	}

	@Override
	public boolean charTyped(char c, int i) {
		return this.recipeBookComponent.charTyped(c, i) ? true : super.charTyped(c, i);
	}

	@Override
	protected boolean isHovering(int i, int j, int k, int l, double d, double e) {
		return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(i, j, k, l, d, e);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (this.recipeBookComponent.mouseClicked(d, e, i)) {
			this.setFocused(this.recipeBookComponent);
			return true;
		} else {
			return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? true : super.mouseClicked(d, e, i);
		}
	}

	@Override
	protected boolean hasClickedOutside(double d, double e, int i, int j, int k) {
		boolean bl = d < (double)i || e < (double)j || d >= (double)(i + this.imageWidth) || e >= (double)(j + this.imageHeight);
		return this.recipeBookComponent.hasClickedOutside(d, e, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, k) && bl;
	}

	@Override
	protected void slotClicked(Slot slot, int i, int j, ClickType clickType) {
		super.slotClicked(slot, i, j, clickType);
		this.recipeBookComponent.slotClicked(slot);
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
