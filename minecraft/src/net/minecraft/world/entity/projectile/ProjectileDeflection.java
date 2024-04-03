package net.minecraft.world.entity.projectile;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

@FunctionalInterface
public interface ProjectileDeflection {
	ProjectileDeflection NONE = (projectile, entity, randomSource) -> {
	};
	ProjectileDeflection REVERSE = (projectile, entity, randomSource) -> {
		float f = 180.0F + randomSource.nextFloat() * 20.0F;
		projectile.setDeltaMovement(projectile.getDeltaMovement().scale(-0.5));
		projectile.setYRot(projectile.getYRot() + f);
		projectile.yRotO += f;
		projectile.hurtMarked = true;
		projectile.onDeflection();
	};

	void deflect(Projectile projectile, Entity entity, RandomSource randomSource);
}
