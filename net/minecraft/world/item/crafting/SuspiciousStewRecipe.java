/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SuspiciousEffectHolder;

public class SuspiciousStewRecipe
extends CustomRecipe {
    public SuspiciousStewRecipe(ResourceLocation resourceLocation, CraftingBookCategory craftingBookCategory) {
        super(resourceLocation, craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        boolean bl = false;
        boolean bl2 = false;
        boolean bl3 = false;
        boolean bl4 = false;
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemStack = craftingContainer.getItem(i);
            if (itemStack.isEmpty()) continue;
            if (itemStack.is(Blocks.BROWN_MUSHROOM.asItem()) && !bl3) {
                bl3 = true;
                continue;
            }
            if (itemStack.is(Blocks.RED_MUSHROOM.asItem()) && !bl2) {
                bl2 = true;
                continue;
            }
            if (itemStack.is(ItemTags.SMALL_FLOWERS) && !bl) {
                bl = true;
                continue;
            }
            if (itemStack.is(Items.BOWL) && !bl4) {
                bl4 = true;
                continue;
            }
            return false;
        }
        return bl && bl3 && bl2 && bl4;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
        ItemStack itemStack = new ItemStack(Items.SUSPICIOUS_STEW, 1);
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            SuspiciousEffectHolder suspiciousEffectHolder;
            ItemStack itemStack2 = craftingContainer.getItem(i);
            if (itemStack2.isEmpty() || (suspiciousEffectHolder = SuspiciousEffectHolder.tryGet(itemStack2.getItem())) == null) continue;
            SuspiciousStewItem.saveMobEffect(itemStack, suspiciousEffectHolder.getSuspiciousEffect(), suspiciousEffectHolder.getEffectDuration());
            break;
        }
        return itemStack;
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return i >= 2 && j >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SUSPICIOUS_STEW;
    }
}

