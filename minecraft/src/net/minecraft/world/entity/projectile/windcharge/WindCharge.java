package net.minecraft.world.entity.projectile.windcharge;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class WindCharge extends AbstractWindCharge {
	private static final WindCharge.WindChargePlayerDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new WindCharge.WindChargePlayerDamageCalculator();
	private static final float BASE_STRENGTH = 1.0F;

	public WindCharge(EntityType<? extends AbstractWindCharge> entityType, Level level) {
		super(entityType, level);
	}

	public WindCharge(Player player, Level level, double d, double e, double f) {
		super(EntityType.WIND_CHARGE, level, player, d, e, f);
	}

	public WindCharge(Level level, double d, double e, double f, double g, double h, double i) {
		super(EntityType.WIND_CHARGE, d, e, f, g, h, i, level);
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
				1.0F + 0.3F * this.random.nextFloat(),
				false,
				Level.ExplosionInteraction.BLOW,
				ParticleTypes.GUST_EMITTER_SMALL,
				ParticleTypes.GUST_EMITTER_LARGE,
				SoundEvents.WIND_CHARGE_BURST
			);
	}

	public static final class WindChargePlayerDamageCalculator extends AbstractWindCharge.WindChargeDamageCalculator {
		@Override
		public float getKnockbackMultiplier() {
			return 1.1F;
		}
	}
}
