package net.minecraft.world.entity.ai.navigation;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WaterBoundPathNavigation extends PathNavigation {
	private boolean allowBreaching;

	public WaterBoundPathNavigation(Mob mob, Level level) {
		super(mob, level);
	}

	@Override
	protected PathFinder createPathFinder(int i) {
		this.allowBreaching = this.mob.getType() == EntityType.DOLPHIN;
		this.nodeEvaluator = new SwimNodeEvaluator(this.allowBreaching);
		return new PathFinder(this.nodeEvaluator, i);
	}

	@Override
	protected boolean canUpdatePath() {
		return this.allowBreaching || this.isInLiquid();
	}

	@Override
	protected Vec3 getTempMobPos() {
		return new Vec3(this.mob.getX(), this.mob.getY(0.5), this.mob.getZ());
	}

	@Override
	public void tick() {
		this.tick++;
		if (this.hasDelayedRecomputation) {
			this.recomputePath();
		}

		if (!this.isDone()) {
			if (this.canUpdatePath()) {
				this.followThePath();
			} else if (this.path != null && !this.path.isDone()) {
				Vec3 vec3 = this.path.getNextEntityPos(this.mob);
				if (this.mob.getBlockX() == Mth.floor(vec3.x) && this.mob.getBlockY() == Mth.floor(vec3.y) && this.mob.getBlockZ() == Mth.floor(vec3.z)) {
					this.path.advance();
				}
			}

			DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
			if (!this.isDone()) {
				Vec3 vec3 = this.path.getNextEntityPos(this.mob);
				this.mob.getMoveControl().setWantedPosition(vec3.x, vec3.y, vec3.z, this.speedModifier);
			}
		}
	}

	@Override
	protected void followThePath() {
		if (this.path != null) {
			Vec3 vec3 = this.getTempMobPos();
			float f = this.mob.getBbWidth();
			float g = f > 0.75F ? f / 2.0F : 0.75F - f / 2.0F;
			Vec3 vec32 = this.mob.getDeltaMovement();
			if (Math.abs(vec32.x) > 0.2 || Math.abs(vec32.z) > 0.2) {
				g = (float)((double)g * vec32.length() * 6.0);
			}

			int i = 6;
			Vec3 vec33 = Vec3.atBottomCenterOf(this.path.getNextNodePos());
			if (Math.abs(this.mob.getX() - vec33.x) < (double)g
				&& Math.abs(this.mob.getZ() - vec33.z) < (double)g
				&& Math.abs(this.mob.getY() - vec33.y) < (double)(g * 2.0F)) {
				this.path.advance();
			}

			for (int j = Math.min(this.path.getNextNodeIndex() + 6, this.path.getNodeCount() - 1); j > this.path.getNextNodeIndex(); j--) {
				vec33 = this.path.getEntityPosAtNode(this.mob, j);
				if (!(vec33.distanceToSqr(vec3) > 36.0) && this.canMoveDirectly(vec3, vec33, 0, 0, 0)) {
					this.path.setNextNodeIndex(j);
					break;
				}
			}

			this.doStuckDetection(vec3);
		}
	}

	@Override
	protected void doStuckDetection(Vec3 vec3) {
		if (this.tick - this.lastStuckCheck > 100) {
			if (vec3.distanceToSqr(this.lastStuckCheckPos) < 2.25) {
				this.stop();
			}

			this.lastStuckCheck = this.tick;
			this.lastStuckCheckPos = vec3;
		}

		if (this.path != null && !this.path.isDone()) {
			Vec3i vec3i = this.path.getNextNodePos();
			if (vec3i.equals(this.timeoutCachedNode)) {
				this.timeoutTimer = this.timeoutTimer + (Util.getMillis() - this.lastTimeoutCheck);
			} else {
				this.timeoutCachedNode = vec3i;
				double d = vec3.distanceTo(Vec3.atCenterOf(this.timeoutCachedNode));
				this.timeoutLimit = this.mob.getSpeed() > 0.0F ? d / (double)this.mob.getSpeed() * 100.0 : 0.0;
			}

			if (this.timeoutLimit > 0.0 && (double)this.timeoutTimer > this.timeoutLimit * 2.0) {
				this.timeoutCachedNode = Vec3i.ZERO;
				this.timeoutTimer = 0L;
				this.timeoutLimit = 0.0;
				this.stop();
			}

			this.lastTimeoutCheck = Util.getMillis();
		}
	}

	@Override
	protected boolean canMoveDirectly(Vec3 vec3, Vec3 vec32, int i, int j, int k) {
		Vec3 vec33 = new Vec3(vec32.x, vec32.y + (double)this.mob.getBbHeight() * 0.5, vec32.z);
		return this.level.clip(new ClipContext(vec3, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.mob)).getType() == HitResult.Type.MISS;
	}

	@Override
	public boolean isStableDestination(BlockPos blockPos) {
		return !this.level.getBlockState(blockPos).isSolidRender(this.level, blockPos);
	}

	@Override
	public void setCanFloat(boolean bl) {
	}
}
