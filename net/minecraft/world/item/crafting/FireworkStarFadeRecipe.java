/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class FireworkStarFadeRecipe
extends CustomRecipe {
    private static final Ingredient STAR_INGREDIENT = Ingredient.of(Items.FIREWORK_STAR);

    public FireworkStarFadeRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        boolean bl = false;
        boolean bl2 = false;
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemStack = craftingContainer.getItem(i);
            if (itemStack.isEmpty()) continue;
            if (itemStack.getItem() instanceof DyeItem) {
                bl = true;
                continue;
            }
            if (STAR_INGREDIENT.test(itemStack)) {
                if (bl2) {
                    return false;
                }
                bl2 = true;
                continue;
            }
            return false;
        }
        return bl2 && bl;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer) {
        ArrayList<Integer> list = Lists.newArrayList();
        ItemStack itemStack = null;
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemStack2 = craftingContainer.getItem(i);
            Item item = itemStack2.getItem();
            if (item instanceof DyeItem) {
                list.add(((DyeItem)item).getDyeColor().getFireworkColor());
                continue;
            }
            if (!STAR_INGREDIENT.test(itemStack2)) continue;
            itemStack = itemStack2.copy();
            itemStack.setCount(1);
        }
        if (itemStack == null || list.isEmpty()) {
            return ItemStack.EMPTY;
        }
        itemStack.getOrCreateTagElement("Explosion").putIntArray("FadeColors", list);
        return itemStack;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean canCraftInDimensions(int i, int j) {
        return i * j >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_STAR_FADE;
    }
}

