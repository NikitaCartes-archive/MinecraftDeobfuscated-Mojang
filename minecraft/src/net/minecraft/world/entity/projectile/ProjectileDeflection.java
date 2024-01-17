package net.minecraft.world.entity.projectile;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

@FunctionalInterface
public interface ProjectileDeflection {
	ProjectileDeflection NONE = (projectile, entity, randomSource) -> {
	};
	ProjectileDeflection REVERSE = (projectile, entity, randomSource) -> {
		float f = 180.0F + randomSource.nextFloat() * 20.0F;
		projectile.setDeltaMovement(entity.getDeltaMovement().scale(-0.25));
		projectile.setYRot(entity.getYRot() + f);
		projectile.yRotO += f;
	};

	void deflect(Projectile projectile, Entity entity, RandomSource randomSource);
}
