package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class GroundPathNavigation extends PathNavigation {
	private boolean avoidSun;

	public GroundPathNavigation(Mob mob, Level level) {
		super(mob, level);
	}

	@Override
	protected PathFinder createPathFinder(int i) {
		this.nodeEvaluator = new WalkNodeEvaluator();
		this.nodeEvaluator.setCanPassDoors(true);
		return new PathFinder(this.nodeEvaluator, i);
	}

	@Override
	protected boolean canUpdatePath() {
		return this.mob.isOnGround() || this.isInLiquid() || this.mob.isPassenger();
	}

	@Override
	protected Vec3 getTempMobPos() {
		return new Vec3(this.mob.getX(), (double)this.getSurfaceY(), this.mob.getZ());
	}

	@Override
	public Path createPath(BlockPos blockPos, int i) {
		if (this.level.getBlockState(blockPos).isAir()) {
			BlockPos blockPos2 = blockPos.below();

			while (blockPos2.getY() > this.level.getMinBuildHeight() && this.level.getBlockState(blockPos2).isAir()) {
				blockPos2 = blockPos2.below();
			}

			if (blockPos2.getY() > this.level.getMinBuildHeight()) {
				return super.createPath(blockPos2.above(), i);
			}

			while (blockPos2.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockPos2).isAir()) {
				blockPos2 = blockPos2.above();
			}

			blockPos = blockPos2;
		}

		if (!this.level.getBlockState(blockPos).getMaterial().isSolid()) {
			return super.createPath(blockPos, i);
		} else {
			BlockPos blockPos2 = blockPos.above();

			while (blockPos2.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockPos2).getMaterial().isSolid()) {
				blockPos2 = blockPos2.above();
			}

			return super.createPath(blockPos2, i);
		}
	}

	@Override
	public Path createPath(Entity entity, int i) {
		return this.createPath(entity.blockPosition(), i);
	}

	private int getSurfaceY() {
		if (this.mob.isInWater() && this.canFloat()) {
			int i = this.mob.getBlockY();
			BlockState blockState = this.level.getBlockState(new BlockPos(this.mob.getX(), (double)i, this.mob.getZ()));
			int j = 0;

			while (blockState.is(Blocks.WATER)) {
				blockState = this.level.getBlockState(new BlockPos(this.mob.getX(), (double)(++i), this.mob.getZ()));
				if (++j > 16) {
					return this.mob.getBlockY();
				}
			}

			return i;
		} else {
			return Mth.floor(this.mob.getY() + 0.5);
		}
	}

	@Override
	protected void trimPath() {
		super.trimPath();
		if (this.avoidSun) {
			if (this.level.canSeeSky(new BlockPos(this.mob.getX(), this.mob.getY() + 0.5, this.mob.getZ()))) {
				return;
			}

			for (int i = 0; i < this.path.getNodeCount(); i++) {
				Node node = this.path.getNode(i);
				if (this.level.canSeeSky(new BlockPos(node.x, node.y, node.z))) {
					this.path.truncateNodes(i);
					return;
				}
			}
		}
	}

	protected boolean hasValidPathType(BlockPathTypes blockPathTypes) {
		if (blockPathTypes == BlockPathTypes.WATER) {
			return false;
		} else {
			return blockPathTypes == BlockPathTypes.LAVA ? false : blockPathTypes != BlockPathTypes.OPEN;
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

	public void setAvoidSun(boolean bl) {
		this.avoidSun = bl;
	}
}
