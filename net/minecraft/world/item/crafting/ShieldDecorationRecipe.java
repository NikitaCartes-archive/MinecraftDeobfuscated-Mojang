/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ShieldDecorationRecipe
extends CustomRecipe {
    public ShieldDecorationRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        ItemStack itemStack = ItemStack.EMPTY;
        ItemStack itemStack2 = ItemStack.EMPTY;
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemStack3 = craftingContainer.getItem(i);
            if (itemStack3.isEmpty()) continue;
            if (itemStack3.getItem() instanceof BannerItem) {
                if (!itemStack2.isEmpty()) {
                    return false;
                }
                itemStack2 = itemStack3;
                continue;
            }
            if (itemStack3.is(Items.SHIELD)) {
                if (!itemStack.isEmpty()) {
                    return false;
                }
                if (itemStack3.getTagElement("BlockEntityTag") != null) {
                    return false;
                }
                itemStack = itemStack3;
                continue;
            }
            return false;
        }
        return !itemStack.isEmpty() && !itemStack2.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer) {
        ItemStack itemStack = ItemStack.EMPTY;
        ItemStack itemStack2 = ItemStack.EMPTY;
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemStack3 = craftingContainer.getItem(i);
            if (itemStack3.isEmpty()) continue;
            if (itemStack3.getItem() instanceof BannerItem) {
                itemStack = itemStack3;
                continue;
            }
            if (!itemStack3.is(Items.SHIELD)) continue;
            itemStack2 = itemStack3.copy();
        }
        if (itemStack2.isEmpty()) {
            return itemStack2;
        }
        CompoundTag compoundTag = itemStack.getTagElement("BlockEntityTag");
        CompoundTag compoundTag2 = compoundTag == null ? new CompoundTag() : compoundTag.copy();
        compoundTag2.putInt("Base", ((BannerItem)itemStack.getItem()).getColor().getId());
        itemStack2.addTagElement("BlockEntityTag", compoundTag2);
        return itemStack2;
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return i * j >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHIELD_DECORATION;
    }
}

