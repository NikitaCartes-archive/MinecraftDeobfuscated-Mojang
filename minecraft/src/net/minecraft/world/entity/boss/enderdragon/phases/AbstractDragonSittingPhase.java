package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.AbstractArrow;

public abstract class AbstractDragonSittingPhase extends AbstractDragonPhaseInstance {
	public AbstractDragonSittingPhase(EnderDragon enderDragon) {
		super(enderDragon);
	}

	@Override
	public boolean isSitting() {
		return true;
	}

	@Override
	public float onHurt(DamageSource damageSource, float f) {
		if (damageSource.getDirectEntity() instanceof AbstractArrow) {
			damageSource.getDirectEntity().setSecondsOnFire(1);
			return 0.0F;
		} else {
			return super.onHurt(damageSource, f);
		}
	}
}
