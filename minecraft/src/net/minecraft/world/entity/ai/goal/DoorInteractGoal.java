package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public abstract class DoorInteractGoal extends Goal {
	protected Mob mob;
	protected BlockPos doorPos = BlockPos.ZERO;
	protected boolean hasDoor;
	private boolean passed;
	private float doorOpenDirX;
	private float doorOpenDirZ;

	public DoorInteractGoal(Mob mob) {
		this.mob = mob;
		if (!GoalUtils.hasGroundPathNavigation(mob)) {
			throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
		}
	}

	protected boolean isOpen() {
		if (!this.hasDoor) {
			return false;
		} else {
			BlockState blockState = this.mob.level.getBlockState(this.doorPos);
			if (!(blockState.getBlock() instanceof DoorBlock)) {
				this.hasDoor = false;
				return false;
			} else {
				return (Boolean)blockState.getValue(DoorBlock.OPEN);
			}
		}
	}

	protected void setOpen(boolean bl) {
		if (this.hasDoor) {
			BlockState blockState = this.mob.level.getBlockState(this.doorPos);
			if (blockState.getBlock() instanceof DoorBlock) {
				((DoorBlock)blockState.getBlock()).setOpen(this.mob, this.mob.level, blockState, this.doorPos, bl);
			}
		}
	}

	@Override
	public boolean canUse() {
		if (!GoalUtils.hasGroundPathNavigation(this.mob)) {
			return false;
		} else if (!this.mob.horizontalCollision) {
			return false;
		} else {
			GroundPathNavigation groundPathNavigation = (GroundPathNavigation)this.mob.getNavigation();
			Path path = groundPathNavigation.getPath();
			if (path != null && !path.isDone() && groundPathNavigation.canOpenDoors()) {
				for (int i = 0; i < Math.min(path.getNextNodeIndex() + 2, path.getNodeCount()); i++) {
					Node node = path.getNode(i);
					this.doorPos = new BlockPos(node.x, node.y + 1, node.z);
					if (!(this.mob.distanceToSqr((double)this.doorPos.getX(), this.mob.getY(), (double)this.doorPos.getZ()) > 2.25)) {
						this.hasDoor = DoorBlock.isWoodenDoor(this.mob.level, this.doorPos);
						if (this.hasDoor) {
							return true;
						}
					}
				}

				this.doorPos = this.mob.blockPosition().above();
				this.hasDoor = DoorBlock.isWoodenDoor(this.mob.level, this.doorPos);
				return this.hasDoor;
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean canContinueToUse() {
		return !this.passed;
	}

	@Override
	public void start() {
		this.passed = false;
		this.doorOpenDirX = (float)((double)this.doorPos.getX() + 0.5 - this.mob.getX());
		this.doorOpenDirZ = (float)((double)this.doorPos.getZ() + 0.5 - this.mob.getZ());
	}

	@Override
	public void tick() {
		float f = (float)((double)this.doorPos.getX() + 0.5 - this.mob.getX());
		float g = (float)((double)this.doorPos.getZ() + 0.5 - this.mob.getZ());
		float h = this.doorOpenDirX * f + this.doorOpenDirZ * g;
		if (h < 0.0F) {
			this.passed = true;
		}
	}
}
