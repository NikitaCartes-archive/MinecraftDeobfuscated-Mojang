package net.minecraft.world.entity.monster.creaking;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CreakingTransient extends Creaking {
	public static final int INVULNERABILITY_ANIMATION_DURATION = 8;
	private int invulnerabilityAnimationRemainingTicks;
	@Nullable
	BlockPos homePos;

	public CreakingTransient(EntityType<? extends Creaking> entityType, Level level) {
		super(entityType, level);
	}

	public void bindToCreakingHeart(BlockPos blockPos) {
		this.homePos = blockPos;
	}

	@Override
	public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
		if (this.level().isClientSide) {
			return super.hurtServer(serverLevel, damageSource, f);
		} else if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return super.hurtServer(serverLevel, damageSource, f);
		} else if (!this.isInvulnerableTo(serverLevel, damageSource) && this.invulnerabilityAnimationRemainingTicks <= 0) {
			this.invulnerabilityAnimationRemainingTicks = 8;
			this.level().broadcastEntityEvent(this, (byte)66);
			if (this.level().getBlockEntity(this.homePos) instanceof CreakingHeartBlockEntity creakingHeartBlockEntity && creakingHeartBlockEntity.isProtector(this)) {
				if (damageSource.getEntity() instanceof Player) {
					creakingHeartBlockEntity.creakingHurt();
				}

				this.playHurtSound(damageSource);
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void aiStep() {
		if (this.invulnerabilityAnimationRemainingTicks > 0) {
			this.invulnerabilityAnimationRemainingTicks--;
		}

		super.aiStep();
	}

	@Override
	public void tick() {
		if (this.level().isClientSide
			|| this.homePos != null
				&& this.level().getBlockEntity(this.homePos) instanceof CreakingHeartBlockEntity creakingHeartBlockEntity
				&& creakingHeartBlockEntity.isProtector(this)) {
			super.tick();
			if (this.level().isClientSide) {
				this.setupAnimationStates();
			}
		} else {
			this.setRemoved(Entity.RemovalReason.DISCARDED);
		}
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 66) {
			this.invulnerabilityAnimationRemainingTicks = 8;
			this.playHurtSound(this.damageSources().generic());
		} else {
			super.handleEntityEvent(b);
		}
	}

	private void setupAnimationStates() {
		this.invulnerabilityAnimationState.animateWhen(this.invulnerabilityAnimationRemainingTicks > 0, this.tickCount);
	}

	public void tearDown(@Nullable DamageSource damageSource) {
		if (this.level() instanceof ServerLevel serverLevel) {
			AABB aABB = this.getBoundingBox();
			Vec3 vec3 = aABB.getCenter();
			double d = aABB.getXsize() * 0.3;
			double e = aABB.getYsize() * 0.3;
			double f = aABB.getZsize() * 0.3;
			serverLevel.sendParticles(
				new BlockParticleOption(ParticleTypes.BLOCK_CRUMBLE, Blocks.PALE_OAK_WOOD.defaultBlockState()), vec3.x, vec3.y, vec3.z, 100, d, e, f, 0.0
			);
			serverLevel.sendParticles(
				new BlockParticleOption(
					ParticleTypes.BLOCK_CRUMBLE, Blocks.CREAKING_HEART.defaultBlockState().setValue(CreakingHeartBlock.CREAKING, CreakingHeartBlock.CreakingHeartState.ACTIVE)
				),
				vec3.x,
				vec3.y,
				vec3.z,
				10,
				d,
				e,
				f,
				0.0
			);
		}

		this.makeSound(this.getDeathSound());
		if (this.deathScore >= 0 && damageSource != null && damageSource.getEntity() instanceof LivingEntity livingEntity) {
			livingEntity.awardKillScore(this, this.deathScore, damageSource);
		}

		this.remove(Entity.RemovalReason.DISCARDED);
	}

	@Override
	protected boolean canAddPassenger(Entity entity) {
		return false;
	}

	@Override
	protected boolean couldAcceptPassenger() {
		return false;
	}

	@Override
	protected void addPassenger(Entity entity) {
		throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
	}

	@Override
	public boolean canUsePortal(boolean bl) {
		return false;
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new CreakingTransient.CreakingPathNavigation(this, level);
	}

	class CreakingPathNavigation extends GroundPathNavigation {
		CreakingPathNavigation(final Creaking creaking, final Level level) {
			super(creaking, level);
		}

		@Override
		public void tick() {
			if (CreakingTransient.this.canMove()) {
				super.tick();
			}
		}

		@Override
		protected PathFinder createPathFinder(int i) {
			this.nodeEvaluator = CreakingTransient.this.new HomeNodeEvaluator();
			return new PathFinder(this.nodeEvaluator, i);
		}
	}

	class HomeNodeEvaluator extends WalkNodeEvaluator {
		private static final int MAX_DISTANCE_TO_HOME_SQ = 1024;

		@Override
		public PathType getPathType(PathfindingContext pathfindingContext, int i, int j, int k) {
			BlockPos blockPos = CreakingTransient.this.homePos;
			if (blockPos == null) {
				return super.getPathType(pathfindingContext, i, j, k);
			} else {
				double d = blockPos.distSqr(new Vec3i(i, j, k));
				return d > 1024.0 && d >= blockPos.distSqr(pathfindingContext.mobPosition()) ? PathType.BLOCKED : super.getPathType(pathfindingContext, i, j, k);
			}
		}
	}
}
