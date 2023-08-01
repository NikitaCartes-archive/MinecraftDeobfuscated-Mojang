package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class FlyingPathNavigation extends PathNavigation {
	public FlyingPathNavigation(Mob mob, Level level) {
		super(mob, level);
	}

	@Override
	protected PathFinder createPathFinder(int i) {
		this.nodeEvaluator = new FlyNodeEvaluator();
		this.nodeEvaluator.setCanPassDoors(true);
		return new PathFinder(this.nodeEvaluator, i);
	}

	@Override
	protected boolean canMoveDirectly(Vec3 vec3, Vec3 vec32) {
		return isClearForMovementBetween(this.mob, vec3, vec32, true);
	}

	@Override
	protected boolean canUpdatePath() {
		return this.canFloat() && this.mob.isInLiquid() || !this.mob.isPassenger();
	}

	@Override
	protected Vec3 getTempMobPos() {
		return this.mob.position();
	}

	@Override
	public Path createPath(Entity entity, int i) {
		return this.createPath(entity.blockPosition(), i);
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

	public void setCanOpenDoors(boolean bl) {
		this.nodeEvaluator.setCanOpenDoors(bl);
	}

	public boolean canPassDoors() {
		return this.nodeEvaluator.canPassDoors();
	}

	public void setCanPassDoors(boolean bl) {
		this.nodeEvaluator.setCanPassDoors(bl);
	}

	public boolean canOpenDoors() {
		return this.nodeEvaluator.canPassDoors();
	}

	@Override
	public boolean isStableDestination(BlockPos blockPos) {
		return this.level.getBlockState(blockPos).entityCanStandOn(this.level, blockPos, this.mob);
	}
}
