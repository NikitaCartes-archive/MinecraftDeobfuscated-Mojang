/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class EnchantedBookItem
extends Item {
    public EnchantedBookItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean isFoil(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return false;
    }

    public static ListTag getEnchantments(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag != null) {
            return compoundTag.getList("StoredEnchantments", 10);
        }
        return new ListTag();
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, list, tooltipFlag);
        ItemStack.appendEnchantmentNames(list, EnchantedBookItem.getEnchantments(itemStack));
    }

    public static void addEnchantment(ItemStack itemStack, EnchantmentInstance enchantmentInstance) {
        ListTag listTag = EnchantedBookItem.getEnchantments(itemStack);
        boolean bl = true;
        ResourceLocation resourceLocation = Registry.ENCHANTMENT.getKey(enchantmentInstance.enchantment);
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            ResourceLocation resourceLocation2 = ResourceLocation.tryParse(compoundTag.getString("id"));
            if (resourceLocation2 == null || !resourceLocation2.equals(resourceLocation)) continue;
            if (compoundTag.getInt("lvl") < enchantmentInstance.level) {
                compoundTag.putShort("lvl", (short)enchantmentInstance.level);
            }
            bl = false;
            break;
        }
        if (bl) {
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.putString("id", String.valueOf(resourceLocation));
            compoundTag2.putShort("lvl", (short)enchantmentInstance.level);
            listTag.add(compoundTag2);
        }
        itemStack.getOrCreateTag().put("StoredEnchantments", listTag);
    }

    public static ItemStack createForEnchantment(EnchantmentInstance enchantmentInstance) {
        ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(itemStack, enchantmentInstance);
        return itemStack;
    }

    @Override
    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        block4: {
            block3: {
                if (creativeModeTab != CreativeModeTab.TAB_SEARCH) break block3;
                for (Enchantment enchantment : Registry.ENCHANTMENT) {
                    if (enchantment.category == null) continue;
                    for (int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); ++i) {
                        nonNullList.add(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, i)));
                    }
                }
                break block4;
            }
            if (creativeModeTab.getEnchantmentCategories().length == 0) break block4;
            for (Enchantment enchantment : Registry.ENCHANTMENT) {
                if (!creativeModeTab.hasEnchantmentCategory(enchantment.category)) continue;
                nonNullList.add(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, enchantment.getMaxLevel())));
            }
        }
    }
}

