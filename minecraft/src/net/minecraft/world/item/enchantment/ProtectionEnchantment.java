package net.minecraft.world.item.enchantment;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class ProtectionEnchantment extends Enchantment {
	public final ProtectionEnchantment.Type type;

	public ProtectionEnchantment(Enchantment.EnchantmentDefinition enchantmentDefinition, ProtectionEnchantment.Type type) {
		super(enchantmentDefinition);
		this.type = type;
	}

	@Override
	public int getDamageProtection(int i, DamageSource damageSource) {
		if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return 0;
		} else if (this.type == ProtectionEnchantment.Type.ALL) {
			return i;
		} else if (this.type == ProtectionEnchantment.Type.FIRE && damageSource.is(DamageTypeTags.IS_FIRE)) {
			return i * 2;
		} else if (this.type == ProtectionEnchantment.Type.FALL && damageSource.is(DamageTypeTags.IS_FALL)) {
			return i * 3;
		} else if (this.type == ProtectionEnchantment.Type.EXPLOSION && damageSource.is(DamageTypeTags.IS_EXPLOSION)) {
			return i * 2;
		} else {
			return this.type == ProtectionEnchantment.Type.PROJECTILE && damageSource.is(DamageTypeTags.IS_PROJECTILE) ? i * 2 : 0;
		}
	}

	@Override
	public boolean checkCompatibility(Enchantment enchantment) {
		if (enchantment instanceof ProtectionEnchantment protectionEnchantment) {
			return this.type == protectionEnchantment.type
				? false
				: this.type == ProtectionEnchantment.Type.FALL || protectionEnchantment.type == ProtectionEnchantment.Type.FALL;
		} else {
			return super.checkCompatibility(enchantment);
		}
	}

	public static int getFireAfterDampener(LivingEntity livingEntity, int i) {
		int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_PROTECTION, livingEntity);
		if (j > 0) {
			i -= Mth.floor((float)i * (float)j * 0.15F);
		}

		return i;
	}

	public static double getExplosionKnockbackAfterDampener(LivingEntity livingEntity, double d) {
		int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, livingEntity);
		if (i > 0) {
			d *= Mth.clamp(1.0 - (double)i * 0.15, 0.0, 1.0);
		}

		return d;
	}

	public static enum Type {
		ALL,
		FIRE,
		FALL,
		EXPLOSION,
		PROJECTILE;
	}
}
