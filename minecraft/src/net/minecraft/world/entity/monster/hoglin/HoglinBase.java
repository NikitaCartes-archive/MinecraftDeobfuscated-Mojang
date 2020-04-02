package net.minecraft.world.entity.monster.hoglin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public interface HoglinBase {
	@Environment(EnvType.CLIENT)
	int getAttackAnimationRemainingTicks();

	static boolean hurtAndThrowTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
		float f = (float)livingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE);
		float g;
		if (!livingEntity.isBaby() && (int)f > 0) {
			g = f / 2.0F + (float)livingEntity.level.random.nextInt((int)f);
		} else {
			g = f;
		}

		boolean bl = livingEntity2.hurt(DamageSource.mobAttack(livingEntity), g);
		if (bl) {
			livingEntity.doEnchantDamageEffects(livingEntity, livingEntity2);
			if (!livingEntity.isBaby()) {
				throwTarget(livingEntity, livingEntity2);
			}
		}

		return bl;
	}

	static void throwTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
		double d = livingEntity.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
		double e = livingEntity2.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		double f = d - e;
		if (!(f <= 0.0)) {
			double g = livingEntity2.getX() - livingEntity.getX();
			double h = livingEntity2.getZ() - livingEntity.getZ();
			float i = (float)(livingEntity.level.random.nextInt(21) - 10);
			double j = f * (double)(livingEntity.level.random.nextFloat() * 0.5F + 0.2F);
			Vec3 vec3 = new Vec3(g, 0.0, h).normalize().scale(j).yRot(i);
			double k = f * (double)livingEntity.level.random.nextFloat() * 0.5;
			livingEntity2.push(vec3.x, k, vec3.z);
			livingEntity2.hurtMarked = true;
		}
	}
}
