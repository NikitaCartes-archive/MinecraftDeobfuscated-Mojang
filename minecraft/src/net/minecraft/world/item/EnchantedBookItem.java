package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;

public class EnchantedBookItem extends Item {
	public EnchantedBookItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean isFoil(ItemStack itemStack) {
		return true;
	}

	@Override
	public boolean isEnchantable(ItemStack itemStack) {
		return false;
	}

	public static ListTag getEnchantments(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		return compoundTag != null ? compoundTag.getList("StoredEnchantments", 10) : new ListTag();
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, level, list, tooltipFlag);
		ItemStack.appendEnchantmentNames(list, getEnchantments(itemStack));
	}

	public static void addEnchantment(ItemStack itemStack, EnchantmentInstance enchantmentInstance) {
		ListTag listTag = getEnchantments(itemStack);
		boolean bl = true;
		ResourceLocation resourceLocation = Registry.ENCHANTMENT.getKey(enchantmentInstance.enchantment);

		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag = listTag.getCompound(i);
			ResourceLocation resourceLocation2 = ResourceLocation.tryParse(compoundTag.getString("id"));
			if (resourceLocation2 != null && resourceLocation2.equals(resourceLocation)) {
				if (compoundTag.getInt("lvl") < enchantmentInstance.level) {
					compoundTag.putShort("lvl", (short)enchantmentInstance.level);
				}

				bl = false;
				break;
			}
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
		addEnchantment(itemStack, enchantmentInstance);
		return itemStack;
	}

	@Override
	public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
		if (creativeModeTab == CreativeModeTab.TAB_SEARCH) {
			for (Enchantment enchantment : Registry.ENCHANTMENT) {
				if (enchantment.category != null) {
					for (int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); i++) {
						nonNullList.add(createForEnchantment(new EnchantmentInstance(enchantment, i)));
					}
				}
			}
		} else if (creativeModeTab.getEnchantmentCategories().length != 0) {
			for (Enchantment enchantmentx : Registry.ENCHANTMENT) {
				if (creativeModeTab.hasEnchantmentCategory(enchantmentx.category)) {
					nonNullList.add(createForEnchantment(new EnchantmentInstance(enchantmentx, enchantmentx.getMaxLevel())));
				}
			}
		}
	}
}
