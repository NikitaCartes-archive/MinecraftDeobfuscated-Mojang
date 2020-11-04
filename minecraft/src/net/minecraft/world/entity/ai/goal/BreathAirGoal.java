package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

public class BreathAirGoal extends Goal {
	private final PathfinderMob mob;

	public BreathAirGoal(PathfinderMob pathfinderMob) {
		this.mob = pathfinderMob;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		return this.mob.getAirSupply() < 140;
	}

	@Override
	public boolean canContinueToUse() {
		return this.canUse();
	}

	@Override
	public boolean isInterruptable() {
		return false;
	}

	@Override
	public void start() {
		this.findAirPosition();
	}

	private void findAirPosition() {
		Iterable<BlockPos> iterable = BlockPos.betweenClosed(
			Mth.floor(this.mob.getX() - 1.0),
			this.mob.getBlockY(),
			Mth.floor(this.mob.getZ() - 1.0),
			Mth.floor(this.mob.getX() + 1.0),
			Mth.floor(this.mob.getY() + 8.0),
			Mth.floor(this.mob.getZ() + 1.0)
		);
		BlockPos blockPos = null;

		for (BlockPos blockPos2 : iterable) {
			if (this.givesAir(this.mob.level, blockPos2)) {
				blockPos = blockPos2;
				break;
			}
		}

		if (blockPos == null) {
			blockPos = new BlockPos(this.mob.getX(), this.mob.getY() + 8.0, this.mob.getZ());
		}

		this.mob.getNavigation().moveTo((double)blockPos.getX(), (double)(blockPos.getY() + 1), (double)blockPos.getZ(), 1.0);
	}

	@Override
	public void tick() {
		this.findAirPosition();
		this.mob.moveRelative(0.02F, new Vec3((double)this.mob.xxa, (double)this.mob.yya, (double)this.mob.zza));
		this.mob.move(MoverType.SELF, this.mob.getDeltaMovement());
	}

	private boolean givesAir(LevelReader levelReader, BlockPos blockPos) {
		BlockState blockState = levelReader.getBlockState(blockPos);
		return (levelReader.getFluidState(blockPos).isEmpty() || blockState.is(Blocks.BUBBLE_COLUMN))
			&& blockState.isPathfindable(levelReader, blockPos, PathComputationType.LAND);
	}
}
