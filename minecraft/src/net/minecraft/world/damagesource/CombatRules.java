package net.minecraft.world.damagesource;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class CombatRules {
	public static final float MAX_ARMOR = 20.0F;
	public static final float ARMOR_PROTECTION_DIVIDER = 25.0F;
	public static final float BASE_ARMOR_TOUGHNESS = 2.0F;
	public static final float MIN_ARMOR_RATIO = 0.2F;
	private static final int NUM_ARMOR_ITEMS = 4;

	public static float getDamageAfterAbsorb(LivingEntity livingEntity, float f, DamageSource damageSource, float g, float h) {
		float i = 2.0F + h / 4.0F;
		float j = Mth.clamp(g - f / i, g * 0.2F, 20.0F);
		float k = j / 25.0F;
		ItemStack itemStack = damageSource.getWeaponItem();
		float l;
		if (itemStack != null && livingEntity.level() instanceof ServerLevel serverLevel) {
			l = Mth.clamp(EnchantmentHelper.modifyArmorEffectiveness(serverLevel, itemStack, livingEntity, damageSource, k), 0.0F, 1.0F);
		} else {
			l = k;
		}

		float m = 1.0F - l;
		return f * m;
	}

	public static float getDamageAfterMagicAbsorb(float f, float g) {
		float h = Mth.clamp(g, 0.0F, 20.0F);
		return f * (1.0F - h / 25.0F);
	}
}
