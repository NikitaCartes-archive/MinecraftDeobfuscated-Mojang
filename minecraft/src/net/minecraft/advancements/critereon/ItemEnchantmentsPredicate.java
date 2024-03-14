package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public abstract class ItemEnchantmentsPredicate implements SingleComponentItemPredicate<ItemEnchantments> {
	private final List<EnchantmentPredicate> enchantments;

	protected ItemEnchantmentsPredicate(List<EnchantmentPredicate> list) {
		this.enchantments = list;
	}

	public static <T extends ItemEnchantmentsPredicate> Codec<T> codec(Function<List<EnchantmentPredicate>, T> function) {
		return EnchantmentPredicate.CODEC.listOf().xmap(function, ItemEnchantmentsPredicate::enchantments);
	}

	protected List<EnchantmentPredicate> enchantments() {
		return this.enchantments;
	}

	public boolean matches(ItemStack itemStack, ItemEnchantments itemEnchantments) {
		for (EnchantmentPredicate enchantmentPredicate : this.enchantments) {
			if (!enchantmentPredicate.containedIn(itemEnchantments)) {
				return false;
			}
		}

		return true;
	}

	public static ItemEnchantmentsPredicate.Enchantments enchantments(List<EnchantmentPredicate> list) {
		return new ItemEnchantmentsPredicate.Enchantments(list);
	}

	public static ItemEnchantmentsPredicate.StoredEnchantments storedEnchantments(List<EnchantmentPredicate> list) {
		return new ItemEnchantmentsPredicate.StoredEnchantments(list);
	}

	public static class Enchantments extends ItemEnchantmentsPredicate {
		public static final Codec<ItemEnchantmentsPredicate.Enchantments> CODEC = codec(ItemEnchantmentsPredicate.Enchantments::new);

		protected Enchantments(List<EnchantmentPredicate> list) {
			super(list);
		}

		@Override
		public DataComponentType<ItemEnchantments> componentType() {
			return DataComponents.ENCHANTMENTS;
		}
	}

	public static class StoredEnchantments extends ItemEnchantmentsPredicate {
		public static final Codec<ItemEnchantmentsPredicate.StoredEnchantments> CODEC = codec(ItemEnchantmentsPredicate.StoredEnchantments::new);

		protected StoredEnchantments(List<EnchantmentPredicate> list) {
			super(list);
		}

		@Override
		public DataComponentType<ItemEnchantments> componentType() {
			return DataComponents.STORED_ENCHANTMENTS;
		}
	}
}
