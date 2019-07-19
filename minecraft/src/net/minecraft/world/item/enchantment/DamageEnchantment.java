package net.minecraft.world.item.enchantment;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;

public class DamageEnchantment extends Enchantment {
	private static final String[] NAMES = new String[]{"all", "undead", "arthropods"};
	private static final int[] MIN_COST = new int[]{1, 5, 5};
	private static final int[] LEVEL_COST = new int[]{11, 8, 8};
	private static final int[] LEVEL_COST_SPAN = new int[]{20, 20, 20};
	public final int type;

	public DamageEnchantment(Enchantment.Rarity rarity, int i, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.WEAPON, equipmentSlots);
		this.type = i;
	}

	@Override
	public int getMinCost(int i) {
		return MIN_COST[this.type] + (i - 1) * LEVEL_COST[this.type];
	}

	@Override
	public int getMaxCost(int i) {
		return this.getMinCost(i) + LEVEL_COST_SPAN[this.type];
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public float getDamageBonus(int i, MobType mobType) {
		if (this.type == 0) {
			return 1.0F + (float)Math.max(0, i - 1) * 0.5F;
		} else if (this.type == 1 && mobType == MobType.UNDEAD) {
			return (float)i * 2.5F;
		} else {
			return this.type == 2 && mobType == MobType.ARTHROPOD ? (float)i * 2.5F : 0.0F;
		}
	}

	@Override
	public boolean checkCompatibility(Enchantment enchantment) {
		return !(enchantment instanceof DamageEnchantment);
	}

	@Override
	public boolean canEnchant(ItemStack itemStack) {
		return itemStack.getItem() instanceof AxeItem ? true : super.canEnchant(itemStack);
	}

	@Override
	public void doPostAttack(LivingEntity livingEntity, Entity entity, int i) {
		if (entity instanceof LivingEntity) {
			LivingEntity livingEntity2 = (LivingEntity)entity;
			if (this.type == 2 && livingEntity2.getMobType() == MobType.ARTHROPOD) {
				int j = 20 + livingEntity.getRandom().nextInt(10 * i);
				livingEntity2.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, j, 3));
			}
		}
	}
}
