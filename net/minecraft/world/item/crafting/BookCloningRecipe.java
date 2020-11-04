/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class BookCloningRecipe
extends CustomRecipe {
    public BookCloningRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        int i = 0;
        ItemStack itemStack = ItemStack.EMPTY;
        for (int j = 0; j < craftingContainer.getContainerSize(); ++j) {
            ItemStack itemStack2 = craftingContainer.getItem(j);
            if (itemStack2.isEmpty()) continue;
            if (itemStack2.is(Items.WRITTEN_BOOK)) {
                if (!itemStack.isEmpty()) {
                    return false;
                }
                itemStack = itemStack2;
                continue;
            }
            if (itemStack2.is(Items.WRITABLE_BOOK)) {
                ++i;
                continue;
            }
            return false;
        }
        return !itemStack.isEmpty() && itemStack.hasTag() && i > 0;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer) {
        int i = 0;
        ItemStack itemStack = ItemStack.EMPTY;
        for (int j = 0; j < craftingContainer.getContainerSize(); ++j) {
            ItemStack itemStack2 = craftingContainer.getItem(j);
            if (itemStack2.isEmpty()) continue;
            if (itemStack2.is(Items.WRITTEN_BOOK)) {
                if (!itemStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                itemStack = itemStack2;
                continue;
            }
            if (itemStack2.is(Items.WRITABLE_BOOK)) {
                ++i;
                continue;
            }
            return ItemStack.EMPTY;
        }
        if (itemStack.isEmpty() || !itemStack.hasTag() || i < 1 || WrittenBookItem.getGeneration(itemStack) >= 2) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack3 = new ItemStack(Items.WRITTEN_BOOK, i);
        CompoundTag compoundTag = itemStack.getTag().copy();
        compoundTag.putInt("generation", WrittenBookItem.getGeneration(itemStack) + 1);
        itemStack3.setTag(compoundTag);
        return itemStack3;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer craftingContainer) {
        NonNullList<ItemStack> nonNullList = NonNullList.withSize(craftingContainer.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < nonNullList.size(); ++i) {
            ItemStack itemStack = craftingContainer.getItem(i);
            if (itemStack.getItem().hasCraftingRemainingItem()) {
                nonNullList.set(i, new ItemStack(itemStack.getItem().getCraftingRemainingItem()));
                continue;
            }
            if (!(itemStack.getItem() instanceof WrittenBookItem)) continue;
            ItemStack itemStack2 = itemStack.copy();
            itemStack2.setCount(1);
            nonNullList.set(i, itemStack2);
            break;
        }
        return nonNullList;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.BOOK_CLONING;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean canCraftInDimensions(int i, int j) {
        return i >= 3 && j >= 3;
    }
}

