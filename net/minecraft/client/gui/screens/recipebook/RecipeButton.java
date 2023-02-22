/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

@Environment(value=EnvType.CLIENT)
public class RecipeButton
extends AbstractWidget {
    private static final ResourceLocation RECIPE_BOOK_LOCATION = new ResourceLocation("textures/gui/recipe_book.png");
    private static final float ANIMATION_TIME = 15.0f;
    private static final int BACKGROUND_SIZE = 25;
    public static final int TICKS_TO_SWAP = 30;
    private static final Component MORE_RECIPES_TOOLTIP = Component.translatable("gui.recipebook.moreRecipes");
    private RecipeBookMenu<?> menu;
    private RecipeBook book;
    private RecipeCollection collection;
    private float time;
    private float animationTime;
    private int currentIndex;

    public RecipeButton() {
        super(0, 0, 25, 25, CommonComponents.EMPTY);
    }

    public void init(RecipeCollection recipeCollection, RecipeBookPage recipeBookPage) {
        this.collection = recipeCollection;
        this.menu = (RecipeBookMenu)recipeBookPage.getMinecraft().player.containerMenu;
        this.book = recipeBookPage.getRecipeBook();
        List<Recipe<?>> list = recipeCollection.getRecipes(this.book.isFiltering(this.menu));
        for (Recipe<?> recipe : list) {
            if (!this.book.willHighlight(recipe)) continue;
            recipeBookPage.recipesShown(list);
            this.animationTime = 15.0f;
            break;
        }
    }

    public RecipeCollection getCollection() {
        return this.collection;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int i, int j, float f) {
        boolean bl;
        if (!Screen.hasControlDown()) {
            this.time += f;
        }
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, RECIPE_BOOK_LOCATION);
        int k = 29;
        if (!this.collection.hasCraftable()) {
            k += 25;
        }
        int l = 206;
        if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
            l += 25;
        }
        boolean bl2 = bl = this.animationTime > 0.0f;
        if (bl) {
            float g = 1.0f + 0.1f * (float)Math.sin(this.animationTime / 15.0f * (float)Math.PI);
            poseStack.pushPose();
            poseStack.translate(this.getX() + 8, this.getY() + 12, 0.0f);
            poseStack.scale(g, g, 1.0f);
            poseStack.translate(-(this.getX() + 8), -(this.getY() + 12), 0.0f);
            this.animationTime -= f;
        }
        RecipeButton.blit(poseStack, this.getX(), this.getY(), k, l, this.width, this.height);
        List<Recipe<?>> list = this.getOrderedRecipes();
        this.currentIndex = Mth.floor(this.time / 30.0f) % list.size();
        ItemStack itemStack = list.get(this.currentIndex).getResultItem(this.collection.registryAccess());
        int m = 4;
        if (this.collection.hasSingleResultItem() && this.getOrderedRecipes().size() > 1) {
            minecraft.getItemRenderer().renderAndDecorateItem(poseStack, itemStack, this.getX() + m + 1, this.getY() + m + 1, 0, 10);
            --m;
        }
        minecraft.getItemRenderer().renderAndDecorateFakeItem(poseStack, itemStack, this.getX() + m, this.getY() + m);
        if (bl) {
            poseStack.popPose();
        }
    }

    private List<Recipe<?>> getOrderedRecipes() {
        List<Recipe<?>> list = this.collection.getDisplayRecipes(true);
        if (!this.book.isFiltering(this.menu)) {
            list.addAll(this.collection.getDisplayRecipes(false));
        }
        return list;
    }

    public boolean isOnlyOption() {
        return this.getOrderedRecipes().size() == 1;
    }

    public Recipe<?> getRecipe() {
        List<Recipe<?>> list = this.getOrderedRecipes();
        return list.get(this.currentIndex);
    }

    public List<Component> getTooltipText(Screen screen) {
        ItemStack itemStack = this.getOrderedRecipes().get(this.currentIndex).getResultItem(this.collection.registryAccess());
        ArrayList<Component> list = Lists.newArrayList(screen.getTooltipFromItem(itemStack));
        if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
            list.add(MORE_RECIPES_TOOLTIP);
        }
        return list;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        ItemStack itemStack = this.getOrderedRecipes().get(this.currentIndex).getResultItem(this.collection.registryAccess());
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)Component.translatable("narration.recipe", itemStack.getHoverName()));
        if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
            narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"), Component.translatable("narration.recipe.usage.more"));
        } else {
            narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.button.usage.hovered"));
        }
    }

    @Override
    public int getWidth() {
        return 25;
    }

    @Override
    protected boolean isValidClickButton(int i) {
        return i == 0 || i == 1;
    }
}

