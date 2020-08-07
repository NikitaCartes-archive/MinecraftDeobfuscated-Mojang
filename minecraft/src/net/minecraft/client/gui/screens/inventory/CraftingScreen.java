package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
	private static final ResourceLocation CRAFTING_TABLE_LOCATION = new ResourceLocation("textures/gui/container/crafting_table.png");
	private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
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
		this.leftPos = this.recipeBookComponent.updateScreenPosition(this.widthTooNarrow, this.width, this.imageWidth);
		this.children.add(this.recipeBookComponent);
		this.setInitialFocus(this.recipeBookComponent);
		this.addButton(new ImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, button -> {
			this.recipeBookComponent.initVisuals(this.widthTooNarrow);
			this.recipeBookComponent.toggleVisibility();
			this.leftPos = this.recipeBookComponent.updateScreenPosition(this.widthTooNarrow, this.width, this.imageWidth);
			((ImageButton)button).setPosition(this.leftPos + 5, this.height / 2 - 49);
		}));
		this.titleLabelX = 29;
	}

	@Override
	public void tick() {
		super.tick();
		this.recipeBookComponent.tick();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
			this.renderBg(poseStack, f, i, j);
			this.recipeBookComponent.render(poseStack, i, j, f);
		} else {
			this.recipeBookComponent.render(poseStack, i, j, f);
			super.render(poseStack, i, j, f);
			this.recipeBookComponent.renderGhostRecipe(poseStack, this.leftPos, this.topPos, true, f);
		}

		this.renderTooltip(poseStack, i, j);
		this.recipeBookComponent.renderTooltip(poseStack, this.leftPos, this.topPos, i, j);
	}

	@Override
	protected void renderBg(PoseStack poseStack, float f, int i, int j) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(CRAFTING_TABLE_LOCATION);
		int k = this.leftPos;
		int l = (this.height - this.imageHeight) / 2;
		this.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
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
	public void removed() {
		this.recipeBookComponent.removed();
		super.removed();
	}

	@Override
	public RecipeBookComponent getRecipeBookComponent() {
		return this.recipeBookComponent;
	}
}
