/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;

public class BannerDuplicateRecipe
extends CustomRecipe {
    public BannerDuplicateRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        DyeColor dyeColor = null;
        ItemStack itemStack = null;
        ItemStack itemStack2 = null;
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemStack3 = craftingContainer.getItem(i);
            Item item = itemStack3.getItem();
            if (!(item instanceof BannerItem)) continue;
            BannerItem bannerItem = (BannerItem)item;
            if (dyeColor == null) {
                dyeColor = bannerItem.getColor();
            } else if (dyeColor != bannerItem.getColor()) {
                return false;
            }
            int j = BannerBlockEntity.getPatternCount(itemStack3);
            if (j > 6) {
                return false;
            }
            if (j > 0) {
                if (itemStack == null) {
                    itemStack = itemStack3;
                    continue;
                }
                return false;
            }
            if (itemStack2 == null) {
                itemStack2 = itemStack3;
                continue;
            }
            return false;
        }
        return itemStack != null && itemStack2 != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer) {
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            int j;
            ItemStack itemStack = craftingContainer.getItem(i);
            if (itemStack.isEmpty() || (j = BannerBlockEntity.getPatternCount(itemStack)) <= 0 || j > 6) continue;
            ItemStack itemStack2 = itemStack.copy();
            itemStack2.setCount(1);
            return itemStack2;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer craftingContainer) {
        NonNullList<ItemStack> nonNullList = NonNullList.withSize(craftingContainer.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < nonNullList.size(); ++i) {
            ItemStack itemStack = craftingContainer.getItem(i);
            if (itemStack.isEmpty()) continue;
            if (itemStack.getItem().hasCraftingRemainingItem()) {
                nonNullList.set(i, new ItemStack(itemStack.getItem().getCraftingRemainingItem()));
                continue;
            }
            if (!itemStack.hasTag() || BannerBlockEntity.getPatternCount(itemStack) <= 0) continue;
            ItemStack itemStack2 = itemStack.copy();
            itemStack2.setCount(1);
            nonNullList.set(i, itemStack2);
        }
        return nonNullList;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.BANNER_DUPLICATE;
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return i * j >= 2;
    }
}

