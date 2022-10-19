/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class FireworkRocketRecipe
extends CustomRecipe {
    private static final Ingredient PAPER_INGREDIENT = Ingredient.of(Items.PAPER);
    private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.of(Items.GUNPOWDER);
    private static final Ingredient STAR_INGREDIENT = Ingredient.of(Items.FIREWORK_STAR);

    public FireworkRocketRecipe(ResourceLocation resourceLocation, CraftingBookCategory craftingBookCategory) {
        super(resourceLocation, craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        boolean bl = false;
        int i = 0;
        for (int j = 0; j < craftingContainer.getContainerSize(); ++j) {
            ItemStack itemStack = craftingContainer.getItem(j);
            if (itemStack.isEmpty()) continue;
            if (PAPER_INGREDIENT.test(itemStack)) {
                if (bl) {
                    return false;
                }
                bl = true;
                continue;
            }
            if (!(GUNPOWDER_INGREDIENT.test(itemStack) ? ++i > 3 : !STAR_INGREDIENT.test(itemStack))) continue;
            return false;
        }
        return bl && i >= 1;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer) {
        ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET, 3);
        CompoundTag compoundTag = itemStack.getOrCreateTagElement("Fireworks");
        ListTag listTag = new ListTag();
        int i = 0;
        for (int j = 0; j < craftingContainer.getContainerSize(); ++j) {
            CompoundTag compoundTag2;
            ItemStack itemStack2 = craftingContainer.getItem(j);
            if (itemStack2.isEmpty()) continue;
            if (GUNPOWDER_INGREDIENT.test(itemStack2)) {
                ++i;
                continue;
            }
            if (!STAR_INGREDIENT.test(itemStack2) || (compoundTag2 = itemStack2.getTagElement("Explosion")) == null) continue;
            listTag.add(compoundTag2);
        }
        compoundTag.putByte("Flight", (byte)i);
        if (!listTag.isEmpty()) {
            compoundTag.put("Explosions", listTag);
        }
        return itemStack;
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return i * j >= 2;
    }

    @Override
    public ItemStack getResultItem() {
        return new ItemStack(Items.FIREWORK_ROCKET);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_ROCKET;
    }
}

