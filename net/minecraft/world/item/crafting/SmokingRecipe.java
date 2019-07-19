/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;

public class SmokingRecipe
extends AbstractCookingRecipe {
    public SmokingRecipe(ResourceLocation resourceLocation, String string, Ingredient ingredient, ItemStack itemStack, float f, int i) {
        super(RecipeType.SMOKING, resourceLocation, string, ingredient, itemStack, f, i);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.SMOKER);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SMOKING_RECIPE;
    }
}

