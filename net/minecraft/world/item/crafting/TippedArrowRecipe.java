/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TippedArrowRecipe
extends CustomRecipe {
    public TippedArrowRecipe(ResourceLocation resourceLocation, CraftingBookCategory craftingBookCategory) {
        super(resourceLocation, craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        if (craftingContainer.getWidth() != 3 || craftingContainer.getHeight() != 3) {
            return false;
        }
        for (int i = 0; i < craftingContainer.getWidth(); ++i) {
            for (int j = 0; j < craftingContainer.getHeight(); ++j) {
                ItemStack itemStack = craftingContainer.getItem(i + j * craftingContainer.getWidth());
                if (itemStack.isEmpty()) {
                    return false;
                }
                if (!(i == 1 && j == 1 ? !itemStack.is(Items.LINGERING_POTION) : !itemStack.is(Items.ARROW))) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
        ItemStack itemStack = craftingContainer.getItem(1 + craftingContainer.getWidth());
        if (!itemStack.is(Items.LINGERING_POTION)) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack2 = new ItemStack(Items.TIPPED_ARROW, 8);
        PotionUtils.setPotion(itemStack2, PotionUtils.getPotion(itemStack));
        PotionUtils.setCustomEffects(itemStack2, PotionUtils.getCustomEffects(itemStack));
        return itemStack2;
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return i >= 2 && j >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.TIPPED_ARROW;
    }
}

