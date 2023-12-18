package net.minecraft.world.effect;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

class HealOrHarmMobEffect extends InstantenousMobEffect {
	private final boolean isHarm;

	public HealOrHarmMobEffect(MobEffectCategory mobEffectCategory, int i, boolean bl) {
		super(mobEffectCategory, i);
		this.isHarm = bl;
	}

	@Override
	public boolean applyEffectTick(LivingEntity livingEntity, int i) {
		if (this.isHarm == livingEntity.isInvertedHealAndHarm()) {
			livingEntity.heal((float)Math.max(4 << i, 0));
		} else {
			livingEntity.hurt(livingEntity.damageSources().magic(), (float)(6 << i));
		}

		return true;
	}

	@Override
	public void applyInstantenousEffect(@Nullable Entity entity, @Nullable Entity entity2, LivingEntity livingEntity, int i, double d) {
		if (this.isHarm == livingEntity.isInvertedHealAndHarm()) {
			int j = (int)(d * (double)(4 << i) + 0.5);
			livingEntity.heal((float)j);
		} else {
			int j = (int)(d * (double)(6 << i) + 0.5);
			if (entity == null) {
				livingEntity.hurt(livingEntity.damageSources().magic(), (float)j);
			} else {
				livingEntity.hurt(livingEntity.damageSources().indirectMagic(entity, entity2), (float)j);
			}
		}
	}
}
