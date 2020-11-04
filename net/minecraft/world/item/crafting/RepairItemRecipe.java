/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class RepairItemRecipe
extends CustomRecipe {
    public RepairItemRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        ArrayList<ItemStack> list = Lists.newArrayList();
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemStack2;
            ItemStack itemStack = craftingContainer.getItem(i);
            if (itemStack.isEmpty()) continue;
            list.add(itemStack);
            if (list.size() <= 1 || itemStack.is((itemStack2 = (ItemStack)list.get(0)).getItem()) && itemStack2.getCount() == 1 && itemStack.getCount() == 1 && itemStack2.getItem().canBeDepleted()) continue;
            return false;
        }
        return list.size() == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer) {
        ItemStack itemStack3;
        ItemStack itemStack;
        ArrayList<ItemStack> list = Lists.newArrayList();
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemStack2;
            itemStack = craftingContainer.getItem(i);
            if (itemStack.isEmpty()) continue;
            list.add(itemStack);
            if (list.size() <= 1 || itemStack.is((itemStack2 = (ItemStack)list.get(0)).getItem()) && itemStack2.getCount() == 1 && itemStack.getCount() == 1 && itemStack2.getItem().canBeDepleted()) continue;
            return ItemStack.EMPTY;
        }
        if (list.size() == 2 && (itemStack3 = (ItemStack)list.get(0)).is((itemStack = (ItemStack)list.get(1)).getItem()) && itemStack3.getCount() == 1 && itemStack.getCount() == 1 && itemStack3.getItem().canBeDepleted()) {
            Item item = itemStack3.getItem();
            int j = item.getMaxDamage() - itemStack3.getDamageValue();
            int k = item.getMaxDamage() - itemStack.getDamageValue();
            int l = j + k + item.getMaxDamage() * 5 / 100;
            int m = item.getMaxDamage() - l;
            if (m < 0) {
                m = 0;
            }
            ItemStack itemStack4 = new ItemStack(itemStack3.getItem());
            itemStack4.setDamageValue(m);
            HashMap<Enchantment, Integer> map = Maps.newHashMap();
            Map<Enchantment, Integer> map2 = EnchantmentHelper.getEnchantments(itemStack3);
            Map<Enchantment, Integer> map3 = EnchantmentHelper.getEnchantments(itemStack);
            Registry.ENCHANTMENT.stream().filter(Enchantment::isCurse).forEach(enchantment -> {
                int i = Math.max(map2.getOrDefault(enchantment, 0), map3.getOrDefault(enchantment, 0));
                if (i > 0) {
                    map.put((Enchantment)enchantment, i);
                }
            });
            if (!map.isEmpty()) {
                EnchantmentHelper.setEnchantments(map, itemStack4);
            }
            return itemStack4;
        }
        return ItemStack.EMPTY;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean canCraftInDimensions(int i, int j) {
        return i * j >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.REPAIR_ITEM;
    }
}

