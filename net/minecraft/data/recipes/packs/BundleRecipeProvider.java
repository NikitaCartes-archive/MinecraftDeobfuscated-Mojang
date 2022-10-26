/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.recipes.packs;

import java.util.function.Consumer;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

public class BundleRecipeProvider
extends RecipeProvider {
    public BundleRecipeProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.BUNDLE).define(Character.valueOf('#'), Items.RABBIT_HIDE).define(Character.valueOf('-'), Items.STRING).pattern("-#-").pattern("# #").pattern("###").unlockedBy("has_string", BundleRecipeProvider.has(Items.STRING)).save(consumer);
    }
}

