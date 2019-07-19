/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.AbstractFurnaceRecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractFurnaceScreen<T extends AbstractFurnaceMenu>
extends AbstractContainerScreen<T>
implements RecipeUpdateListener {
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
    public final AbstractFurnaceRecipeBookComponent recipeBookComponent;
    private boolean widthTooNarrow;
    private final ResourceLocation texture;

    public AbstractFurnaceScreen(T abstractFurnaceMenu, AbstractFurnaceRecipeBookComponent abstractFurnaceRecipeBookComponent, Inventory inventory, Component component, ResourceLocation resourceLocation) {
        super(abstractFurnaceMenu, inventory, component);
        this.recipeBookComponent = abstractFurnaceRecipeBookComponent;
        this.texture = resourceLocation;
    }

    @Override
    public void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, (RecipeBookMenu)this.menu);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.widthTooNarrow, this.width, this.imageWidth);
        this.addButton(new ImageButton(this.leftPos + 20, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, button -> {
            this.recipeBookComponent.initVisuals(this.widthTooNarrow);
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.widthTooNarrow, this.width, this.imageWidth);
            ((ImageButton)button).setPosition(this.leftPos + 20, this.height / 2 - 49);
        }));
    }

    @Override
    public void tick() {
        super.tick();
        this.recipeBookComponent.tick();
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(f, i, j);
            this.recipeBookComponent.render(i, j, f);
        } else {
            this.recipeBookComponent.render(i, j, f);
            super.render(i, j, f);
            this.recipeBookComponent.renderGhostRecipe(this.leftPos, this.topPos, true, f);
        }
        this.renderTooltip(i, j);
        this.recipeBookComponent.renderTooltip(this.leftPos, this.topPos, i, j);
    }

    @Override
    protected void renderLabels(int i, int j) {
        String string = this.title.getColoredString();
        this.font.draw(string, this.imageWidth / 2 - this.font.width(string) / 2, 6.0f, 0x404040);
        this.font.draw(this.inventory.getDisplayName().getColoredString(), 8.0f, this.imageHeight - 96 + 2, 0x404040);
    }

    @Override
    protected void renderBg(float f, int i, int j) {
        int m;
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bind(this.texture);
        int k = this.leftPos;
        int l = this.topPos;
        this.blit(k, l, 0, 0, this.imageWidth, this.imageHeight);
        if (((AbstractFurnaceMenu)this.menu).isLit()) {
            m = ((AbstractFurnaceMenu)this.menu).getLitProgress();
            this.blit(k + 56, l + 36 + 12 - m, 176, 12 - m, 14, m + 1);
        }
        m = ((AbstractFurnaceMenu)this.menu).getBurnProgress();
        this.blit(k + 79, l + 34, 176, 14, m + 1, 16);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (this.recipeBookComponent.mouseClicked(d, e, i)) {
            return true;
        }
        if (this.widthTooNarrow && this.recipeBookComponent.isVisible()) {
            return true;
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    protected void slotClicked(Slot slot, int i, int j, ClickType clickType) {
        super.slotClicked(slot, i, j, clickType);
        this.recipeBookComponent.slotClicked(slot);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (this.recipeBookComponent.keyPressed(i, j, k)) {
            return false;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    protected boolean hasClickedOutside(double d, double e, int i, int j, int k) {
        boolean bl = d < (double)i || e < (double)j || d >= (double)(i + this.imageWidth) || e >= (double)(j + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside(d, e, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, k) && bl;
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (this.recipeBookComponent.charTyped(c, i)) {
            return true;
        }
        return super.charTyped(c, i);
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }

    @Override
    public void removed() {
        this.recipeBookComponent.removed();
        super.removed();
    }
}

