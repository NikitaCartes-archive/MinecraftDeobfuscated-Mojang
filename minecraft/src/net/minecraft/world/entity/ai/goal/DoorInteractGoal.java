package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
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
		if (!(mob.getNavigation() instanceof GroundPathNavigation)) {
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
				((DoorBlock)blockState.getBlock()).setOpen(this.mob.level, this.doorPos, bl);
			}
		}
	}

	@Override
	public boolean canUse() {
		if (!this.mob.horizontalCollision) {
			return false;
		} else {
			GroundPathNavigation groundPathNavigation = (GroundPathNavigation)this.mob.getNavigation();
			Path path = groundPathNavigation.getPath();
			if (path != null && !path.isDone() && groundPathNavigation.canOpenDoors()) {
				for (int i = 0; i < Math.min(path.getIndex() + 2, path.getSize()); i++) {
					Node node = path.get(i);
					this.doorPos = new BlockPos(node.x, node.y + 1, node.z);
					if (!(this.mob.distanceToSqr((double)this.doorPos.getX(), this.mob.y, (double)this.doorPos.getZ()) > 2.25)) {
						this.hasDoor = isDoor(this.mob.level, this.doorPos);
						if (this.hasDoor) {
							return true;
						}
					}
				}

				this.doorPos = new BlockPos(this.mob).above();
				this.hasDoor = isDoor(this.mob.level, this.doorPos);
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
		this.doorOpenDirX = (float)((double)((float)this.doorPos.getX() + 0.5F) - this.mob.x);
		this.doorOpenDirZ = (float)((double)((float)this.doorPos.getZ() + 0.5F) - this.mob.z);
	}

	@Override
	public void tick() {
		float f = (float)((double)((float)this.doorPos.getX() + 0.5F) - this.mob.x);
		float g = (float)((double)((float)this.doorPos.getZ() + 0.5F) - this.mob.z);
		float h = this.doorOpenDirX * f + this.doorOpenDirZ * g;
		if (h < 0.0F) {
			this.passed = true;
		}
	}

	public static boolean isDoor(Level level, BlockPos blockPos) {
		BlockState blockState = level.getBlockState(blockPos);
		return blockState.getBlock() instanceof DoorBlock && blockState.getMaterial() == Material.WOOD;
	}
}
