package net.minecraft.world.entity.projectile.windcharge;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BreezeWindCharge extends AbstractWindCharge {
	private static final float RADIUS = 3.0F;

	public BreezeWindCharge(EntityType<? extends AbstractWindCharge> entityType, Level level) {
		super(entityType, level);
	}

	public BreezeWindCharge(Breeze breeze, Level level) {
		super(EntityType.BREEZE_WIND_CHARGE, level, breeze, breeze.getX(), breeze.getFiringYPosition(), breeze.getZ());
	}

	@Override
	protected void explode(Vec3 vec3) {
		this.level()
			.explode(
				this,
				null,
				EXPLOSION_DAMAGE_CALCULATOR,
				vec3.x(),
				vec3.y(),
				vec3.z(),
				3.0F,
				false,
				Level.ExplosionInteraction.TRIGGER,
				ParticleTypes.GUST_EMITTER_SMALL,
				ParticleTypes.GUST_EMITTER_LARGE,
				SoundEvents.BREEZE_WIND_CHARGE_BURST
			);
	}
}
