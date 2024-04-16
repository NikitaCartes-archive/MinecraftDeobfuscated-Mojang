package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface ProjectileDeflection {
	ProjectileDeflection NONE = (projectile, entity, randomSource) -> {
	};
	ProjectileDeflection REVERSE = (projectile, entity, randomSource) -> {
		float f = 170.0F + randomSource.nextFloat() * 20.0F;
		projectile.setDeltaMovement(projectile.getDeltaMovement().scale(-0.5));
		projectile.setYRot(projectile.getYRot() + f);
		projectile.yRotO += f;
		projectile.hurtMarked = true;
	};
	ProjectileDeflection AIM_DEFLECT = (projectile, entity, randomSource) -> {
		if (entity != null) {
			Vec3 vec3 = entity.getLookAngle();
			projectile.setDeltaMovement(vec3.scale(projectile.getDeltaMovement().length()));
			projectile.hurtMarked = true;
		}
	};
	ProjectileDeflection MOMENTUM_DEFLECT = (projectile, entity, randomSource) -> {
		if (entity != null) {
			Vec3 vec3 = entity.getDeltaMovement().normalize();
			projectile.setDeltaMovement(vec3.scale(projectile.getDeltaMovement().length()));
			projectile.hurtMarked = true;
		}
	};

	void deflect(Projectile projectile, @Nullable Entity entity, RandomSource randomSource);
}
