/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;

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
        super(player.inventoryMenu, player.inventory, new TranslatableComponent("container.crafting", new Object[0]));
        this.passEvents = true;
    }

    @Override
    public void tick() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player));
            return;
        }
        this.recipeBookComponent.tick();
    }

    @Override
    protected void init() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player));
            return;
        }
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, (RecipeBookMenu)this.menu);
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

    @Override
    protected void renderLabels(int i, int j) {
        this.font.draw(this.title.getColoredString(), 97.0f, 8.0f, 0x404040);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        boolean bl = this.doRenderEffects = !this.recipeBookComponent.isVisible();
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
        this.xMouse = i;
        this.yMouse = j;
        this.magicalSpecialHackyFocus(this.recipeBookComponent);
    }

    @Override
    protected void renderBg(float f, int i, int j) {
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bind(INVENTORY_LOCATION);
        int k = this.leftPos;
        int l = this.topPos;
        this.blit(k, l, 0, 0, this.imageWidth, this.imageHeight);
        InventoryScreen.renderPlayerModel(k + 51, l + 75, 30, (float)(k + 51) - this.xMouse, (float)(l + 75 - 50) - this.yMouse, this.minecraft.player);
    }

    public static void renderPlayerModel(int i, int j, int k, float f, float g, LivingEntity livingEntity) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translatef(i, j, 50.0f);
        GlStateManager.scalef(-k, k, k);
        GlStateManager.rotatef(180.0f, 0.0f, 0.0f, 1.0f);
        float h = livingEntity.yBodyRot;
        float l = livingEntity.yRot;
        float m = livingEntity.xRot;
        float n = livingEntity.yHeadRotO;
        float o = livingEntity.yHeadRot;
        GlStateManager.rotatef(135.0f, 0.0f, 1.0f, 0.0f);
        Lighting.turnOn();
        GlStateManager.rotatef(-135.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotatef(-((float)Math.atan(g / 40.0f)) * 20.0f, 1.0f, 0.0f, 0.0f);
        livingEntity.yBodyRot = (float)Math.atan(f / 40.0f) * 20.0f;
        livingEntity.yRot = (float)Math.atan(f / 40.0f) * 40.0f;
        livingEntity.xRot = -((float)Math.atan(g / 40.0f)) * 20.0f;
        livingEntity.yHeadRot = livingEntity.yRot;
        livingEntity.yHeadRotO = livingEntity.yRot;
        GlStateManager.translatef(0.0f, 0.0f, 0.0f);
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        entityRenderDispatcher.setPlayerRotY(180.0f);
        entityRenderDispatcher.setRenderShadow(false);
        entityRenderDispatcher.render(livingEntity, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
        entityRenderDispatcher.setRenderShadow(true);
        livingEntity.yBodyRot = h;
        livingEntity.yRot = l;
        livingEntity.xRot = m;
        livingEntity.yHeadRotO = n;
        livingEntity.yHeadRot = o;
        GlStateManager.popMatrix();
        Lighting.turnOff();
        GlStateManager.disableRescaleNormal();
        GlStateManager.activeTexture(GLX.GL_TEXTURE1);
        GlStateManager.disableTexture();
        GlStateManager.activeTexture(GLX.GL_TEXTURE0);
    }

    @Override
    protected boolean isHovering(int i, int j, int k, int l, double d, double e) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(i, j, k, l, d, e);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (this.recipeBookComponent.mouseClicked(d, e, i)) {
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

