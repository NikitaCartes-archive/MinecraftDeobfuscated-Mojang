package net.minecraft.world.item.enchantment;

import java.util.Map.Entry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class ThornsEnchantment extends Enchantment {
	private static final float CHANCE_PER_LEVEL = 0.15F;

	public ThornsEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.ARMOR_CHEST, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return 10 + 20 * (i - 1);
	}

	@Override
	public int getMaxCost(int i) {
		return super.getMinCost(i) + 50;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public boolean canEnchant(ItemStack itemStack) {
		return itemStack.getItem() instanceof ArmorItem ? true : super.canEnchant(itemStack);
	}

	@Override
	public void doPostHurt(LivingEntity livingEntity, Entity entity, int i) {
		RandomSource randomSource = livingEntity.getRandom();
		Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(Enchantments.THORNS, livingEntity);
		if (shouldHit(i, randomSource)) {
			if (entity != null) {
				entity.hurt(livingEntity.damageSources().thorns(livingEntity), (float)getDamage(i, randomSource));
			}

			if (entry != null) {
				((ItemStack)entry.getValue()).hurtAndBreak(2, livingEntity, livingEntityx -> livingEntityx.broadcastBreakEvent((EquipmentSlot)entry.getKey()));
			}
		}
	}

	public static boolean shouldHit(int i, RandomSource randomSource) {
		return i <= 0 ? false : randomSource.nextFloat() < 0.15F * (float)i;
	}

	public static int getDamage(int i, RandomSource randomSource) {
		return i > 10 ? i - 10 : 1 + randomSource.nextInt(4);
	}
}
