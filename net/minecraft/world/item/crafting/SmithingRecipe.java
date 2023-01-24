/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;

public interface SmithingRecipe
extends Recipe<Container> {
    @Override
    default public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    @Override
    default public boolean canCraftInDimensions(int i, int j) {
        return i >= 3 && j >= 1;
    }

    @Override
    default public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.SMITHING_TABLE);
    }

    public boolean isTemplateIngredient(ItemStack var1);

    public boolean isBaseIngredient(ItemStack var1);

    public boolean isAdditionIngredient(ItemStack var1);
}

