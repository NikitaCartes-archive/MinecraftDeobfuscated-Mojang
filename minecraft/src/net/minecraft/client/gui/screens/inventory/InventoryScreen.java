package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.GameRenderer;
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
		super(player.inventoryMenu, player.getInventory(), new TranslatableComponent("container.crafting"));
		this.passEvents = true;
		this.titleLabelX = 97;
	}

	@Override
	public void containerTick() {
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
			this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
			this.addRenderableWidget(new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, button -> {
				this.recipeBookComponent.toggleVisibility();
				this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
				((ImageButton)button).setPosition(this.leftPos + 104, this.height / 2 - 22);
				this.buttonClicked = true;
			}));
			this.addWidget(this.recipeBookComponent);
			this.setInitialFocus(this.recipeBookComponent);
		}
	}

	@Override
	protected void renderLabels(PoseStack poseStack, int i, int j) {
		this.font.draw(poseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.doRenderEffects = !this.recipeBookComponent.isVisible();
		if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
			this.renderBg(poseStack, f, i, j);
			this.recipeBookComponent.render(poseStack, i, j, f);
		} else {
			this.recipeBookComponent.render(poseStack, i, j, f);
			super.render(poseStack, i, j, f);
			this.recipeBookComponent.renderGhostRecipe(poseStack, this.leftPos, this.topPos, false, f);
		}

		this.renderTooltip(poseStack, i, j);
		this.recipeBookComponent.renderTooltip(poseStack, this.leftPos, this.topPos, i, j);
		this.xMouse = (float)i;
		this.yMouse = (float)j;
	}

	@Override
	protected void renderBg(PoseStack poseStack, float f, int i, int j) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
		int k = this.leftPos;
		int l = this.topPos;
		this.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
		renderEntityInInventory(k + 51, l + 75, 30, (float)(k + 51) - this.xMouse, (float)(l + 75 - 50) - this.yMouse, this.minecraft.player);
	}

	public static void renderEntityInInventory(int i, int j, int k, float f, float g, LivingEntity livingEntity) {
		float h = (float)Math.atan((double)(f / 40.0F));
		float l = (float)Math.atan((double)(g / 40.0F));
		PoseStack poseStack = RenderSystem.getModelViewStack();
		poseStack.pushPose();
		poseStack.translate((double)i, (double)j, 1050.0);
		poseStack.scale(1.0F, 1.0F, -1.0F);
		RenderSystem.applyModelViewMatrix();
		PoseStack poseStack2 = new PoseStack();
		poseStack2.translate(0.0, 0.0, 1000.0);
		poseStack2.scale((float)k, (float)k, (float)k);
		Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
		Quaternion quaternion2 = Vector3f.XP.rotationDegrees(l * 20.0F);
		quaternion.mul(quaternion2);
		poseStack2.mulPose(quaternion);
		float m = livingEntity.yBodyRot;
		float n = livingEntity.getYRot();
		float o = livingEntity.getXRot();
		float p = livingEntity.yHeadRotO;
		float q = livingEntity.yHeadRot;
		livingEntity.yBodyRot = 180.0F + h * 20.0F;
		livingEntity.setYRot(180.0F + h * 40.0F);
		livingEntity.setXRot(-l * 20.0F);
		livingEntity.yHeadRot = livingEntity.getYRot();
		livingEntity.yHeadRotO = livingEntity.getYRot();
		Lighting.setupForEntityInInventory();
		EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		quaternion2.conj();
		entityRenderDispatcher.overrideCameraOrientation(quaternion2);
		entityRenderDispatcher.setRenderShadow(false);
		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(livingEntity, 0.0, 0.0, 0.0, 0.0F, 1.0F, poseStack2, bufferSource, 15728880));
		bufferSource.endBatch();
		entityRenderDispatcher.setRenderShadow(true);
		livingEntity.yBodyRot = m;
		livingEntity.setYRot(n);
		livingEntity.setXRot(o);
		livingEntity.yHeadRotO = p;
		livingEntity.yHeadRot = q;
		poseStack.popPose();
		RenderSystem.applyModelViewMatrix();
		Lighting.setupFor3DItems();
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
