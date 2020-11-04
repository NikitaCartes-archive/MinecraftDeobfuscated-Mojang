/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class MapCloningRecipe
extends CustomRecipe {
    public MapCloningRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        int i = 0;
        ItemStack itemStack = ItemStack.EMPTY;
        for (int j = 0; j < craftingContainer.getContainerSize(); ++j) {
            ItemStack itemStack2 = craftingContainer.getItem(j);
            if (itemStack2.isEmpty()) continue;
            if (itemStack2.is(Items.FILLED_MAP)) {
                if (!itemStack.isEmpty()) {
                    return false;
                }
                itemStack = itemStack2;
                continue;
            }
            if (itemStack2.is(Items.MAP)) {
                ++i;
                continue;
            }
            return false;
        }
        return !itemStack.isEmpty() && i > 0;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer) {
        int i = 0;
        ItemStack itemStack = ItemStack.EMPTY;
        for (int j = 0; j < craftingContainer.getContainerSize(); ++j) {
            ItemStack itemStack2 = craftingContainer.getItem(j);
            if (itemStack2.isEmpty()) continue;
            if (itemStack2.is(Items.FILLED_MAP)) {
                if (!itemStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                itemStack = itemStack2;
                continue;
            }
            if (itemStack2.is(Items.MAP)) {
                ++i;
                continue;
            }
            return ItemStack.EMPTY;
        }
        if (itemStack.isEmpty() || i < 1) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack3 = itemStack.copy();
        itemStack3.setCount(i + 1);
        return itemStack3;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean canCraftInDimensions(int i, int j) {
        return i >= 3 && j >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.MAP_CLONING;
    }
}

