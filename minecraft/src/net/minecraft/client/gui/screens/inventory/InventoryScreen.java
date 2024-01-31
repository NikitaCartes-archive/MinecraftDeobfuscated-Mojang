package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class InventoryScreen extends EffectRenderingInventoryScreen<InventoryMenu> implements RecipeUpdateListener {
	private float xMouse;
	private float yMouse;
	private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
	private boolean widthTooNarrow;
	private boolean buttonClicked;

	public InventoryScreen(Player player) {
		super(player.inventoryMenu, player.getInventory(), Component.translatable("container.crafting"));
		this.titleLabelX = 97;
	}

	@Override
	public void containerTick() {
		if (this.minecraft.gameMode.hasInfiniteItems()) {
			this.minecraft
				.setScreen(
					new CreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get())
				);
		} else {
			this.recipeBookComponent.tick();
		}
	}

	@Override
	protected void init() {
		if (this.minecraft.gameMode.hasInfiniteItems()) {
			this.minecraft
				.setScreen(
					new CreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get())
				);
		} else {
			super.init();
			this.widthTooNarrow = this.width < 379;
			this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
			this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
			this.addRenderableWidget(new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, button -> {
				this.recipeBookComponent.toggleVisibility();
				this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
				button.setPosition(this.leftPos + 104, this.height / 2 - 22);
				this.buttonClicked = true;
			}));
			this.addWidget(this.recipeBookComponent);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int i, int j) {
		guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
			this.renderBackground(guiGraphics, i, j, f);
			this.recipeBookComponent.render(guiGraphics, i, j, f);
		} else {
			super.render(guiGraphics, i, j, f);
			this.recipeBookComponent.render(guiGraphics, i, j, f);
			this.recipeBookComponent.renderGhostRecipe(guiGraphics, this.leftPos, this.topPos, false, f);
		}

		this.renderTooltip(guiGraphics, i, j);
		this.recipeBookComponent.renderTooltip(guiGraphics, this.leftPos, this.topPos, i, j);
		this.xMouse = (float)i;
		this.yMouse = (float)j;
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
		int k = this.leftPos;
		int l = this.topPos;
		guiGraphics.blit(INVENTORY_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
		renderEntityInInventoryFollowsMouse(guiGraphics, k + 26, l + 8, k + 75, l + 78, 30, 0.0625F, this.xMouse, this.yMouse, this.minecraft.player);
	}

	public static void renderEntityInInventoryFollowsMouse(
		GuiGraphics guiGraphics, int i, int j, int k, int l, int m, float f, float g, float h, LivingEntity livingEntity
	) {
		float n = (float)(i + k) / 2.0F;
		float o = (float)(j + l) / 2.0F;
		guiGraphics.enableScissor(i, j, k, l);
		float p = (float)Math.atan((double)((n - g) / 40.0F));
		float q = (float)Math.atan((double)((o - h) / 40.0F));
		Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf quaternionf2 = new Quaternionf().rotateX(q * 20.0F * (float) (Math.PI / 180.0));
		quaternionf.mul(quaternionf2);
		float r = livingEntity.yBodyRot;
		float s = livingEntity.getYRot();
		float t = livingEntity.getXRot();
		float u = livingEntity.yHeadRotO;
		float v = livingEntity.yHeadRot;
		livingEntity.yBodyRot = 180.0F + p * 20.0F;
		livingEntity.setYRot(180.0F + p * 40.0F);
		livingEntity.setXRot(-q * 20.0F);
		livingEntity.yHeadRot = livingEntity.getYRot();
		livingEntity.yHeadRotO = livingEntity.getYRot();
		float w = livingEntity.getScale();
		Vector3f vector3f = new Vector3f(0.0F, livingEntity.getBbHeight() / 2.0F + f * w, 0.0F);
		float x = (float)m / w;
		renderEntityInInventory(guiGraphics, n, o, x, vector3f, quaternionf, quaternionf2, livingEntity);
		livingEntity.yBodyRot = r;
		livingEntity.setYRot(s);
		livingEntity.setXRot(t);
		livingEntity.yHeadRotO = u;
		livingEntity.yHeadRot = v;
		guiGraphics.disableScissor();
	}

	public static void renderEntityInInventory(
		GuiGraphics guiGraphics, float f, float g, float h, Vector3f vector3f, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, LivingEntity livingEntity
	) {
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate((double)f, (double)g, 50.0);
		guiGraphics.pose().scale(h, h, -h);
		guiGraphics.pose().translate(vector3f.x, vector3f.y, vector3f.z);
		guiGraphics.pose().mulPose(quaternionf);
		Lighting.setupForEntityInInventory();
		EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		if (quaternionf2 != null) {
			quaternionf2.conjugate();
			entityRenderDispatcher.overrideCameraOrientation(quaternionf2);
		}

		entityRenderDispatcher.setRenderShadow(false);
		RenderSystem.runAsFancy(
			() -> entityRenderDispatcher.render(livingEntity, 0.0, 0.0, 0.0, 0.0F, 1.0F, guiGraphics.pose(), guiGraphics.bufferSource(), 15728880)
		);
		guiGraphics.flush();
		entityRenderDispatcher.setRenderShadow(true);
		guiGraphics.pose().popPose();
		Lighting.setupFor3DItems();
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
	public RecipeBookComponent getRecipeBookComponent() {
		return this.recipeBookComponent;
	}
}
