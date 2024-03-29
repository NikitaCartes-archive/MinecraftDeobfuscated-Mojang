package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

public class PanicGoal extends Goal {
	public static final int WATER_CHECK_DISTANCE_VERTICAL = 1;
	protected final PathfinderMob mob;
	protected final double speedModifier;
	protected double posX;
	protected double posY;
	protected double posZ;
	protected boolean isRunning;

	public PanicGoal(PathfinderMob pathfinderMob, double d) {
		this.mob = pathfinderMob;
		this.speedModifier = d;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (!this.shouldPanic()) {
			return false;
		} else {
			if (this.mob.isOnFire()) {
				BlockPos blockPos = this.lookForWater(this.mob.level(), this.mob, 5);
				if (blockPos != null) {
					this.posX = (double)blockPos.getX();
					this.posY = (double)blockPos.getY();
					this.posZ = (double)blockPos.getZ();
					return true;
				}
			}

			return this.findRandomPosition();
		}
	}

	protected boolean shouldPanic() {
		return this.mob.getLastHurtByMob() != null || this.mob.isFreezing() || this.mob.isOnFire();
	}

	protected boolean findRandomPosition() {
		Vec3 vec3 = DefaultRandomPos.getPos(this.mob, 5, 4);
		if (vec3 == null) {
			return false;
		} else {
			this.posX = vec3.x;
			this.posY = vec3.y;
			this.posZ = vec3.z;
			return true;
		}
	}

	public boolean isRunning() {
		return this.isRunning;
	}

	@Override
	public void start() {
		this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
		this.isRunning = true;
	}

	@Override
	public void stop() {
		this.isRunning = false;
	}

	@Override
	public boolean canContinueToUse() {
		return !this.mob.getNavigation().isDone();
	}

	@Nullable
	protected BlockPos lookForWater(BlockGetter blockGetter, Entity entity, int i) {
		BlockPos blockPos = entity.blockPosition();
		return !blockGetter.getBlockState(blockPos).getCollisionShape(blockGetter, blockPos).isEmpty()
			? null
			: (BlockPos)BlockPos.findClosestMatch(entity.blockPosition(), i, 1, blockPosx -> blockGetter.getFluidState(blockPosx).is(FluidTags.WATER)).orElse(null);
	}
}
