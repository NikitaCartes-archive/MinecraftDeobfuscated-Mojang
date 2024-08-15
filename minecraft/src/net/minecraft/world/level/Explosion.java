package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public interface Explosion {
	static DamageSource getDefaultDamageSource(Level level, @Nullable Entity entity) {
		return level.damageSources().explosion(entity, getIndirectSourceEntity(entity));
	}

	@Nullable
	static LivingEntity getIndirectSourceEntity(@Nullable Entity entity) {
		return switch (entity) {
			case null, default -> null;
			case PrimedTnt primedTnt -> primedTnt.getOwner();
			case LivingEntity livingEntity -> livingEntity;
			case Projectile projectile when projectile.getOwner() instanceof LivingEntity livingEntity2 -> livingEntity2;
		};
	}

	Explosion.BlockInteraction getBlockInteraction();

	@Nullable
	LivingEntity getIndirectSourceEntity();

	@Nullable
	Entity getDirectSourceEntity();

	float radius();

	Vec3 center();

	boolean canTriggerBlocks();

	boolean shouldAffectBlocklikeEntities();

	public static enum BlockInteraction {
		KEEP(false),
		DESTROY(true),
		DESTROY_WITH_DECAY(true),
		TRIGGER_BLOCK(false);

		private final boolean shouldAffectBlocklikeEntities;

		private BlockInteraction(final boolean bl) {
			this.shouldAffectBlocklikeEntities = bl;
		}

		public boolean shouldAffectBlocklikeEntities() {
			return this.shouldAffectBlocklikeEntities;
		}
	}
}
