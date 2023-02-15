/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;

public class DecoratedPotRecipe
extends CustomRecipe {
    public DecoratedPotRecipe(ResourceLocation resourceLocation, CraftingBookCategory craftingBookCategory) {
        super(resourceLocation, craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        if (!this.canCraftInDimensions(craftingContainer.getWidth(), craftingContainer.getHeight())) {
            return false;
        }
        block3: for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemStack = craftingContainer.getItem(i);
            switch (i) {
                case 1: 
                case 3: 
                case 5: 
                case 7: {
                    if (itemStack.is(ItemTags.DECORATED_POT_SHARDS)) continue block3;
                    return false;
                }
                default: {
                    if (itemStack.is(Items.AIR)) continue block3;
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
        ItemStack itemStack = Items.DECORATED_POT.getDefaultInstance();
        CompoundTag compoundTag = new CompoundTag();
        DecoratedPotBlockEntity.saveShards(List.of(craftingContainer.getItem(1).getItem(), craftingContainer.getItem(3).getItem(), craftingContainer.getItem(5).getItem(), craftingContainer.getItem(7).getItem()), compoundTag);
        BlockItem.setBlockEntityData(itemStack, BlockEntityType.DECORATED_POT, compoundTag);
        return itemStack;
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return i == 3 && j == 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.DECORATED_POT_RECIPE;
    }
}

