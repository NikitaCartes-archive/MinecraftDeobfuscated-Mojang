/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

@Environment(value=EnvType.CLIENT)
public class RecipeCollection {
    private final List<Recipe<?>> recipes = Lists.newArrayList();
    private final Set<Recipe<?>> craftable = Sets.newHashSet();
    private final Set<Recipe<?>> fitsDimensions = Sets.newHashSet();
    private final Set<Recipe<?>> known = Sets.newHashSet();
    private boolean singleResultItem = true;

    public boolean hasKnownRecipes() {
        return !this.known.isEmpty();
    }

    public void updateKnownRecipes(RecipeBook recipeBook) {
        for (Recipe<?> recipe : this.recipes) {
            if (!recipeBook.contains(recipe)) continue;
            this.known.add(recipe);
        }
    }

    public void canCraft(StackedContents stackedContents, int i, int j, RecipeBook recipeBook) {
        for (int k = 0; k < this.recipes.size(); ++k) {
            boolean bl;
            Recipe<?> recipe = this.recipes.get(k);
            boolean bl2 = bl = recipe.canCraftInDimensions(i, j) && recipeBook.contains(recipe);
            if (bl) {
                this.fitsDimensions.add(recipe);
            } else {
                this.fitsDimensions.remove(recipe);
            }
            if (bl && stackedContents.canCraft(recipe, null)) {
                this.craftable.add(recipe);
                continue;
            }
            this.craftable.remove(recipe);
        }
    }

    public boolean isCraftable(Recipe<?> recipe) {
        return this.craftable.contains(recipe);
    }

    public boolean hasCraftable() {
        return !this.craftable.isEmpty();
    }

    public boolean hasFitting() {
        return !this.fitsDimensions.isEmpty();
    }

    public List<Recipe<?>> getRecipes() {
        return this.recipes;
    }

    public List<Recipe<?>> getRecipes(boolean bl) {
        ArrayList<Recipe<?>> list = Lists.newArrayList();
        Set<Recipe<?>> set = bl ? this.craftable : this.fitsDimensions;
        for (Recipe<?> recipe : this.recipes) {
            if (!set.contains(recipe)) continue;
            list.add(recipe);
        }
        return list;
    }

    public List<Recipe<?>> getDisplayRecipes(boolean bl) {
        ArrayList<Recipe<?>> list = Lists.newArrayList();
        for (Recipe<?> recipe : this.recipes) {
            if (!this.fitsDimensions.contains(recipe) || this.craftable.contains(recipe) != bl) continue;
            list.add(recipe);
        }
        return list;
    }

    public void add(Recipe<?> recipe) {
        this.recipes.add(recipe);
        if (this.singleResultItem) {
            ItemStack itemStack2;
            ItemStack itemStack = this.recipes.get(0).getResultItem();
            this.singleResultItem = ItemStack.isSame(itemStack, itemStack2 = recipe.getResultItem()) && ItemStack.tagMatches(itemStack, itemStack2);
        }
    }

    public boolean hasSingleResultItem() {
        return this.singleResultItem;
    }
}

