package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;

@Environment(EnvType.CLIENT)
public class InventoryScreen extends EffectRenderingInventoryScreen<InventoryMenu> implements RecipeUpdateListener {
	private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
	private float xMouse;
	private float yMouse;
	private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
	private boolean recipeBookComponentInitialized;
	private boolean widthTooNarrow;
	private boolean buttonClicked;

	public InventoryScreen(Player player) {
		super(player.inventoryMenu, player.inventory, new TranslatableComponent("container.crafting"));
		this.passEvents = true;
	}

	@Override
	public void tick() {
		if (this.minecraft.gameMode.hasInfiniteItems()) {
			this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player));
		} else {
			this.recipeBookComponent.tick();
		}
	}

	@Override
	protected void init() {
		if (this.minecraft.gameMode.hasInfiniteItems()) {
			this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player));
		} else {
			super.init();
			this.widthTooNarrow = this.width < 379;
			this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
			this.recipeBookComponentInitialized = true;
			this.leftPos = this.recipeBookComponent.updateScreenPosition(this.widthTooNarrow, this.width, this.imageWidth);
			this.children.add(this.recipeBookComponent);
			this.setInitialFocus(this.recipeBookComponent);
			this.addButton(new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, button -> {
				this.recipeBookComponent.initVisuals(this.widthTooNarrow);
				this.recipeBookComponent.toggleVisibility();
				this.leftPos = this.recipeBookComponent.updateScreenPosition(this.widthTooNarrow, this.width, this.imageWidth);
				((ImageButton)button).setPosition(this.leftPos + 104, this.height / 2 - 22);
				this.buttonClicked = true;
			}));
		}
	}

	@Override
	protected void renderLabels(int i, int j) {
		this.font.draw(this.title.getColoredString(), 97.0F, 8.0F, 4210752);
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.doRenderEffects = !this.recipeBookComponent.isVisible();
		if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
			this.renderBg(f, i, j);
			this.recipeBookComponent.render(i, j, f);
		} else {
			this.recipeBookComponent.render(i, j, f);
			super.render(i, j, f);
			this.recipeBookComponent.renderGhostRecipe(this.leftPos, this.topPos, false, f);
		}

		this.renderTooltip(i, j);
		this.recipeBookComponent.renderTooltip(this.leftPos, this.topPos, i, j);
		this.xMouse = (float)i;
		this.yMouse = (float)j;
		this.magicalSpecialHackyFocus(this.recipeBookComponent);
	}

	@Override
	protected void renderBg(float f, int i, int j) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(INVENTORY_LOCATION);
		int k = this.leftPos;
		int l = this.topPos;
		this.blit(k, l, 0, 0, this.imageWidth, this.imageHeight);
		renderEntityInInventory(k + 51, l + 75, 30, (float)(k + 51) - this.xMouse, (float)(l + 75 - 50) - this.yMouse, this.minecraft.player);
	}

	public static void renderEntityInInventory(int i, int j, int k, float f, float g, LivingEntity livingEntity) {
		RenderSystem.pushMatrix();
		RenderSystem.scalef(-1.0F, 1.0F, 1.0F);
		PoseStack poseStack = new PoseStack();
		poseStack.translate((double)(-i), (double)j, 50.0);
		poseStack.scale((float)k, (float)k, (float)k);
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
		poseStack.mulPose(Vector3f.XP.rotationDegrees(-((float)Math.atan((double)(g / 40.0F))) * 20.0F));
		float h = livingEntity.yBodyRot;
		float l = livingEntity.yRot;
		float m = livingEntity.xRot;
		float n = livingEntity.yHeadRotO;
		float o = livingEntity.yHeadRot;
		livingEntity.yBodyRot = (float)Math.atan((double)(f / 40.0F)) * 20.0F;
		livingEntity.yRot = (float)Math.atan((double)(f / 40.0F)) * 40.0F;
		livingEntity.xRot = -((float)Math.atan((double)(g / 40.0F))) * 20.0F;
		livingEntity.yHeadRot = livingEntity.yRot;
		livingEntity.yHeadRotO = livingEntity.yRot;
		EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		entityRenderDispatcher.setRenderShadow(false);
		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		entityRenderDispatcher.render(livingEntity, 0.0, 0.0, 0.0, 0.0F, 1.0F, poseStack, bufferSource, 15728880);
		bufferSource.endBatch(-i, j, 1000);
		entityRenderDispatcher.setRenderShadow(true);
		livingEntity.yBodyRot = h;
		livingEntity.yRot = l;
		livingEntity.xRot = m;
		livingEntity.yHeadRotO = n;
		livingEntity.yHeadRot = o;
		RenderSystem.popMatrix();
	}

	@Override
	protected boolean isHovering(int i, int j, int k, int l, double d, double e) {
		return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(i, j, k, l, d, e);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (this.recipeBookComponent.mouseClicked(d, e, i)) {
			return true;
		} else {
			return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? false : super.mouseClicked(d, e, i);
		}
	}

	@Override
	public boolean mouseReleased(double d, double e, int i) {
		if (this.buttonClicked) {
			this.buttonClicked = false;
			return true;
		} else {
			return super.mouseReleased(d, e, i);
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
		if (this.recipeBookComponentInitialized) {
			this.recipeBookComponent.removed();
		}

		super.removed();
	}

	@Override
	public RecipeBookComponent getRecipeBookComponent() {
		return this.recipeBookComponent;
	}
}
