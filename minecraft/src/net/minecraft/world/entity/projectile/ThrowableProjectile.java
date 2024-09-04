package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class ThrowableProjectile extends Projectile {
	private static final float MIN_CAMERA_DISTANCE_SQUARED = 12.25F;

	protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, Level level) {
		super(entityType, level);
	}

	protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, double d, double e, double f, Level level) {
		this(entityType, level);
		this.setPos(d, e, f);
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		if (this.tickCount < 2 && d < 12.25) {
			return false;
		} else {
			double e = this.getBoundingBox().getSize() * 4.0;
			if (Double.isNaN(e)) {
				e = 4.0;
			}

			e *= 64.0;
			return d < e * e;
		}
	}

	@Override
	public boolean canUsePortal(boolean bl) {
		return true;
	}

	@Override
	public void tick() {
		HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
		Vec3 vec3;
		if (hitResult.getType() != HitResult.Type.MISS) {
			vec3 = hitResult.getLocation();
		} else {
			vec3 = this.position().add(this.getDeltaMovement());
		}

		this.setPos(vec3);
		this.updateRotation();
		this.applyEffectsFromBlocks();
		super.tick();
		if (hitResult.getType() != HitResult.Type.MISS && this.isAlive()) {
			this.hitTargetOrDeflectSelf(hitResult);
		}

		this.applyInertia();
		this.applyGravity();
	}

	private void applyInertia() {
		Vec3 vec3 = this.getDeltaMovement();
		Vec3 vec32 = this.position();
		float g;
		if (this.isInWater()) {
			for (int i = 0; i < 4; i++) {
				float f = 0.25F;
				this.level().addParticle(ParticleTypes.BUBBLE, vec32.x - vec3.x * 0.25, vec32.y - vec3.y * 0.25, vec32.z - vec3.z * 0.25, vec3.x, vec3.y, vec3.z);
			}

			g = 0.8F;
		} else {
			g = 0.99F;
		}

		this.setDeltaMovement(vec3.scale((double)g));
	}

	@Override
	protected double getDefaultGravity() {
		return 0.03;
	}
}
