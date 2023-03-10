/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Environment(value=EnvType.CLIENT)
public class InventoryScreen
extends EffectRenderingInventoryScreen<InventoryMenu>
implements RecipeUpdateListener {
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
    private float xMouse;
    private float yMouse;
    private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
    private boolean recipeBookComponentInitialized;
    private boolean widthTooNarrow;
    private boolean buttonClicked;

    public InventoryScreen(Player player) {
        super(player.inventoryMenu, player.getInventory(), Component.translatable("container.crafting"));
        this.passEvents = true;
        this.titleLabelX = 97;
    }

    @Override
    public void containerTick() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get()));
            return;
        }
        this.recipeBookComponent.tick();
    }

    @Override
    protected void init() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get()));
            return;
        }
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, (RecipeBookMenu)this.menu);
        this.recipeBookComponentInitialized = true;
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        this.addRenderableWidget(new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, button -> {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            button.setPosition(this.leftPos + 104, this.height / 2 - 22);
            this.buttonClicked = true;
        }));
        this.addWidget(this.recipeBookComponent);
        this.setInitialFocus(this.recipeBookComponent);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int i, int j) {
        this.font.draw(poseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 0x404040);
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
            this.recipeBookComponent.renderGhostRecipe(poseStack, this.leftPos, this.topPos, false, f);
        }
        this.renderTooltip(poseStack, i, j);
        this.recipeBookComponent.renderTooltip(poseStack, this.leftPos, this.topPos, i, j);
        this.xMouse = i;
        this.yMouse = j;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
        int k = this.leftPos;
        int l = this.topPos;
        InventoryScreen.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
        InventoryScreen.renderEntityInInventoryFollowsMouse(poseStack, k + 51, l + 75, 30, (float)(k + 51) - this.xMouse, (float)(l + 75 - 50) - this.yMouse, this.minecraft.player);
    }

    public static void renderEntityInInventoryFollowsMouse(PoseStack poseStack, int i, int j, int k, float f, float g, LivingEntity livingEntity) {
        float h = (float)Math.atan(f / 40.0f);
        float l = (float)Math.atan(g / 40.0f);
        Quaternionf quaternionf = new Quaternionf().rotateZ((float)Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(l * 20.0f * ((float)Math.PI / 180));
        quaternionf.mul(quaternionf2);
        float m = livingEntity.yBodyRot;
        float n = livingEntity.getYRot();
        float o = livingEntity.getXRot();
        float p = livingEntity.yHeadRotO;
        float q = livingEntity.yHeadRot;
        livingEntity.yBodyRot = 180.0f + h * 20.0f;
        livingEntity.setYRot(180.0f + h * 40.0f);
        livingEntity.setXRot(-l * 20.0f);
        livingEntity.yHeadRot = livingEntity.getYRot();
        livingEntity.yHeadRotO = livingEntity.getYRot();
        InventoryScreen.renderEntityInInventory(poseStack, i, j, k, quaternionf, quaternionf2, livingEntity);
        livingEntity.yBodyRot = m;
        livingEntity.setYRot(n);
        livingEntity.setXRot(o);
        livingEntity.yHeadRotO = p;
        livingEntity.yHeadRot = q;
    }

    public static void renderEntityInInventory(PoseStack poseStack, int i, int j, int k, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, LivingEntity livingEntity) {
        double d = 1000.0;
        PoseStack poseStack2 = RenderSystem.getModelViewStack();
        poseStack2.pushPose();
        poseStack2.translate(0.0, 0.0, 1000.0);
        RenderSystem.applyModelViewMatrix();
        poseStack.pushPose();
        poseStack.translate((double)i, (double)j, -950.0);
        poseStack.mulPoseMatrix(new Matrix4f().scaling(k, k, -k));
        poseStack.mulPose(quaternionf);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (quaternionf2 != null) {
            quaternionf2.conjugate();
            entityRenderDispatcher.overrideCameraOrientation(quaternionf2);
        }
        entityRenderDispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(livingEntity, 0.0, 0.0, 0.0, 0.0f, 1.0f, poseStack, bufferSource, 0xF000F0));
        bufferSource.endBatch();
        entityRenderDispatcher.setRenderShadow(true);
        poseStack.popPose();
        Lighting.setupFor3DItems();
        poseStack2.popPose();
        RenderSystem.applyModelViewMatrix();
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
        }
        if (this.widthTooNarrow && this.recipeBookComponent.isVisible()) {
            return false;
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        }
        return super.mouseReleased(d, e, i);
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

