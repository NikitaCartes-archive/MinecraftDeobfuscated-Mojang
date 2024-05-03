package net.minecraft.world.entity.projectile.windcharge;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.level.Level;

public class BreezeWindCharge extends AbstractWindCharge {
	private static final float RADIUS = 3.0F;

	public BreezeWindCharge(EntityType<? extends AbstractWindCharge> entityType, Level level) {
		super(entityType, level);
	}

	public BreezeWindCharge(Breeze breeze, Level level) {
		super(EntityType.BREEZE_WIND_CHARGE, level, breeze, breeze.getX(), breeze.getSnoutYPosition(), breeze.getZ());
	}

	@Override
	protected void explode() {
		this.level()
			.explode(
				this,
				null,
				EXPLOSION_DAMAGE_CALCULATOR,
				this.getX(),
				this.getY(),
				this.getZ(),
				3.0F,
				false,
				Level.ExplosionInteraction.TRIGGER,
				ParticleTypes.GUST_EMITTER_SMALL,
				ParticleTypes.GUST_EMITTER_LARGE,
				SoundEvents.BREEZE_WIND_CHARGE_BURST
			);
	}
}
