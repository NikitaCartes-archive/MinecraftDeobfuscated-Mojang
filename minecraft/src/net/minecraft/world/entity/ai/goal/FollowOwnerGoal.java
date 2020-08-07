package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class FollowOwnerGoal extends Goal {
	private final TamableAnimal tamable;
	private LivingEntity owner;
	private final LevelReader level;
	private final double speedModifier;
	private final PathNavigation navigation;
	private int timeToRecalcPath;
	private final float stopDistance;
	private final float startDistance;
	private float oldWaterCost;
	private final boolean canFly;

	public FollowOwnerGoal(TamableAnimal tamableAnimal, double d, float f, float g, boolean bl) {
		this.tamable = tamableAnimal;
		this.level = tamableAnimal.level;
		this.speedModifier = d;
		this.navigation = tamableAnimal.getNavigation();
		this.startDistance = f;
		this.stopDistance = g;
		this.canFly = bl;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		if (!(tamableAnimal.getNavigation() instanceof GroundPathNavigation) && !(tamableAnimal.getNavigation() instanceof FlyingPathNavigation)) {
			throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
		}
	}

	@Override
	public boolean canUse() {
		LivingEntity livingEntity = this.tamable.getOwner();
		if (livingEntity == null) {
			return false;
		} else if (livingEntity.isSpectator()) {
			return false;
		} else if (this.tamable.isOrderedToSit()) {
			return false;
		} else if (this.tamable.distanceToSqr(livingEntity) < (double)(this.startDistance * this.startDistance)) {
			return false;
		} else {
			this.owner = livingEntity;
			return true;
		}
	}

	@Override
	public boolean canContinueToUse() {
		if (this.navigation.isDone()) {
			return false;
		} else {
			return this.tamable.isOrderedToSit() ? false : !(this.tamable.distanceToSqr(this.owner) <= (double)(this.stopDistance * this.stopDistance));
		}
	}

	@Override
	public void start() {
		this.timeToRecalcPath = 0;
		this.oldWaterCost = this.tamable.getPathfindingMalus(BlockPathTypes.WATER);
		this.tamable.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
	}

	@Override
	public void stop() {
		this.owner = null;
		this.navigation.stop();
		this.tamable.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
	}

	@Override
	public void tick() {
		this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float)this.tamable.getMaxHeadXRot());
		if (--this.timeToRecalcPath <= 0) {
			this.timeToRecalcPath = 10;
			if (!this.tamable.isLeashed() && !this.tamable.isPassenger()) {
				if (this.tamable.distanceToSqr(this.owner) >= 144.0) {
					this.teleportToOwner();
				} else {
					this.navigation.moveTo(this.owner, this.speedModifier);
				}
			}
		}
	}

	private void teleportToOwner() {
		BlockPos blockPos = this.owner.blockPosition();

		for (int i = 0; i < 10; i++) {
			int j = this.randomIntInclusive(-3, 3);
			int k = this.randomIntInclusive(-1, 1);
			int l = this.randomIntInclusive(-3, 3);
			boolean bl = this.maybeTeleportTo(blockPos.getX() + j, blockPos.getY() + k, blockPos.getZ() + l);
			if (bl) {
				return;
			}
		}
	}

	private boolean maybeTeleportTo(int i, int j, int k) {
		if (Math.abs((double)i - this.owner.getX()) < 2.0 && Math.abs((double)k - this.owner.getZ()) < 2.0) {
			return false;
		} else if (!this.canTeleportTo(new BlockPos(i, j, k))) {
			return false;
		} else {
			this.tamable.moveTo((double)i + 0.5, (double)j, (double)k + 0.5, this.tamable.yRot, this.tamable.xRot);
			this.navigation.stop();
			return true;
		}
	}

	private boolean canTeleportTo(BlockPos blockPos) {
		BlockPathTypes blockPathTypes = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, blockPos.mutable());
		if (blockPathTypes != BlockPathTypes.WALKABLE) {
			return false;
		} else {
			BlockState blockState = this.level.getBlockState(blockPos.below());
			if (!this.canFly && blockState.getBlock() instanceof LeavesBlock) {
				return false;
			} else {
				BlockPos blockPos2 = blockPos.subtract(this.tamable.blockPosition());
				return this.level.noCollision(this.tamable, this.tamable.getBoundingBox().move(blockPos2));
			}
		}
	}

	private int randomIntInclusive(int i, int j) {
		return this.tamable.getRandom().nextInt(j - i + 1) + i;
	}
}
