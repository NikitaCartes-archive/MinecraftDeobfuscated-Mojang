/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.recipebook;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

@Environment(value=EnvType.CLIENT)
public class RecipeBookTabButton
extends StateSwitchingButton {
    private final RecipeBookCategories category;
    private static final float ANIMATION_TIME = 15.0f;
    private float animationTime;

    public RecipeBookTabButton(RecipeBookCategories recipeBookCategories) {
        super(0, 0, 35, 27, false);
        this.category = recipeBookCategories;
        this.initTextureValues(153, 2, 35, 0, RecipeBookComponent.RECIPE_BOOK_LOCATION);
    }

    public void startAnimation(Minecraft minecraft) {
        ClientRecipeBook clientRecipeBook = minecraft.player.getRecipeBook();
        List<RecipeCollection> list = clientRecipeBook.getCollection(this.category);
        if (!(minecraft.player.containerMenu instanceof RecipeBookMenu)) {
            return;
        }
        for (RecipeCollection recipeCollection : list) {
            for (Recipe<?> recipe : recipeCollection.getRecipes(clientRecipeBook.isFiltering((RecipeBookMenu)minecraft.player.containerMenu))) {
                if (!clientRecipeBook.willHighlight(recipe)) continue;
                this.animationTime = 15.0f;
                return;
            }
        }
    }

    @Override
    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        if (this.animationTime > 0.0f) {
            float g = 1.0f + 0.1f * (float)Math.sin(this.animationTime / 15.0f * (float)Math.PI);
            poseStack.pushPose();
            poseStack.translate(this.getX() + 8, this.getY() + 12, 0.0f);
            poseStack.scale(1.0f, g, 1.0f);
            poseStack.translate(-(this.getX() + 8), -(this.getY() + 12), 0.0f);
        }
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.resourceLocation);
        RenderSystem.disableDepthTest();
        int k = this.xTexStart;
        int l = this.yTexStart;
        if (this.isStateTriggered) {
            k += this.xDiffTex;
        }
        if (this.isHoveredOrFocused()) {
            l += this.yDiffTex;
        }
        int m = this.getX();
        if (this.isStateTriggered) {
            m -= 2;
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.blit(poseStack, m, this.getY(), k, l, this.width, this.height);
        RenderSystem.enableDepthTest();
        this.renderIcon(minecraft.getItemRenderer());
        if (this.animationTime > 0.0f) {
            poseStack.popPose();
            this.animationTime -= f;
        }
    }

    private void renderIcon(ItemRenderer itemRenderer) {
        int i;
        List<ItemStack> list = this.category.getIconItems();
        int n = i = this.isStateTriggered ? -2 : 0;
        if (list.size() == 1) {
            itemRenderer.renderAndDecorateFakeItem(list.get(0), this.getX() + 9 + i, this.getY() + 5);
        } else if (list.size() == 2) {
            itemRenderer.renderAndDecorateFakeItem(list.get(0), this.getX() + 3 + i, this.getY() + 5);
            itemRenderer.renderAndDecorateFakeItem(list.get(1), this.getX() + 14 + i, this.getY() + 5);
        }
    }

    public RecipeBookCategories getCategory() {
        return this.category;
    }

    public boolean updateVisibility(ClientRecipeBook clientRecipeBook) {
        List<RecipeCollection> list = clientRecipeBook.getCollection(this.category);
        this.visible = false;
        if (list != null) {
            for (RecipeCollection recipeCollection : list) {
                if (!recipeCollection.hasKnownRecipes() || !recipeCollection.hasFitting()) continue;
                this.visible = true;
                break;
            }
        }
        return this.visible;
    }
}

