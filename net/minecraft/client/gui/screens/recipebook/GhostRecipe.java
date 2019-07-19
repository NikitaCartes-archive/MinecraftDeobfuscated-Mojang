/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GhostRecipe {
    private Recipe<?> recipe;
    private final List<GhostIngredient> ingredients = Lists.newArrayList();
    private float time;

    public void clear() {
        this.recipe = null;
        this.ingredients.clear();
        this.time = 0.0f;
    }

    public void addIngredient(Ingredient ingredient, int i, int j) {
        this.ingredients.add(new GhostIngredient(ingredient, i, j));
    }

    public GhostIngredient get(int i) {
        return this.ingredients.get(i);
    }

    public int size() {
        return this.ingredients.size();
    }

    @Nullable
    public Recipe<?> getRecipe() {
        return this.recipe;
    }

    public void setRecipe(Recipe<?> recipe) {
        this.recipe = recipe;
    }

    public void render(Minecraft minecraft, int i, int j, boolean bl, float f) {
        if (!Screen.hasControlDown()) {
            this.time += f;
        }
        Lighting.turnOnGui();
        GlStateManager.disableLighting();
        for (int k = 0; k < this.ingredients.size(); ++k) {
            GhostIngredient ghostIngredient = this.ingredients.get(k);
            int l = ghostIngredient.getX() + i;
            int m = ghostIngredient.getY() + j;
            if (k == 0 && bl) {
                GuiComponent.fill(l - 4, m - 4, l + 20, m + 20, 0x30FF0000);
            } else {
                GuiComponent.fill(l, m, l + 16, m + 16, 0x30FF0000);
            }
            ItemStack itemStack = ghostIngredient.getItem();
            ItemRenderer itemRenderer = minecraft.getItemRenderer();
            itemRenderer.renderAndDecorateItem(minecraft.player, itemStack, l, m);
            GlStateManager.depthFunc(516);
            GuiComponent.fill(l, m, l + 16, m + 16, 0x30FFFFFF);
            GlStateManager.depthFunc(515);
            if (k == 0) {
                itemRenderer.renderGuiItemDecorations(minecraft.font, itemStack, l, m);
            }
            GlStateManager.enableLighting();
        }
        Lighting.turnOff();
    }

    @Environment(value=EnvType.CLIENT)
    public class GhostIngredient {
        private final Ingredient ingredient;
        private final int x;
        private final int y;

        public GhostIngredient(Ingredient ingredient, int i, int j) {
            this.ingredient = ingredient;
            this.x = i;
            this.y = j;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public ItemStack getItem() {
            ItemStack[] itemStacks = this.ingredient.getItems();
            return itemStacks[Mth.floor(GhostRecipe.this.time / 30.0f) % itemStacks.length];
        }
    }
}

