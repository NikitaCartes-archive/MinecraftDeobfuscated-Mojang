package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;

public class TryFindWaterGoal extends Goal {
	private final PathfinderMob mob;

	public TryFindWaterGoal(PathfinderMob pathfinderMob) {
		this.mob = pathfinderMob;
	}

	@Override
	public boolean canUse() {
		return this.mob.isOnGround() && !this.mob.level.getFluidState(this.mob.blockPosition()).is(FluidTags.WATER);
	}

	@Override
	public void start() {
		BlockPos blockPos = null;

		for (BlockPos blockPos2 : BlockPos.betweenClosed(
			Mth.floor(this.mob.getX() - 2.0),
			Mth.floor(this.mob.getY() - 2.0),
			Mth.floor(this.mob.getZ() - 2.0),
			Mth.floor(this.mob.getX() + 2.0),
			this.mob.getBlockY(),
			Mth.floor(this.mob.getZ() + 2.0)
		)) {
			if (this.mob.level.getFluidState(blockPos2).is(FluidTags.WATER)) {
				blockPos = blockPos2;
				break;
			}
		}

		if (blockPos != null) {
			this.mob.getMoveControl().setWantedPosition((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), 1.0);
		}
	}
}
