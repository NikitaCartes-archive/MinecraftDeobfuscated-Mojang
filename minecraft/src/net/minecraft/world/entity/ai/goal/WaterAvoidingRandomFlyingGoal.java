package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.phys.Vec3;

public class WaterAvoidingRandomFlyingGoal extends WaterAvoidingRandomStrollGoal {
	public WaterAvoidingRandomFlyingGoal(PathfinderMob pathfinderMob, double d) {
		super(pathfinderMob, d);
	}

	@Nullable
	@Override
	protected Vec3 getPosition() {
		Vec3 vec3 = null;
		if (this.mob.isInWater()) {
			vec3 = RandomPos.getLandPos(this.mob, 15, 15);
		}

		if (this.mob.getRandom().nextFloat() >= this.probability) {
			vec3 = this.getTreePos();
		}

		return vec3 == null ? super.getPosition() : vec3;
	}

	@Nullable
	private Vec3 getTreePos() {
		BlockPos blockPos = new BlockPos(this.mob);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();

		for (BlockPos blockPos2 : BlockPos.betweenClosed(
			Mth.floor(this.mob.x - 3.0),
			Mth.floor(this.mob.y - 6.0),
			Mth.floor(this.mob.z - 3.0),
			Mth.floor(this.mob.x + 3.0),
			Mth.floor(this.mob.y + 6.0),
			Mth.floor(this.mob.z + 3.0)
		)) {
			if (!blockPos.equals(blockPos2)) {
				Block block = this.mob.level.getBlockState(mutableBlockPos2.set(blockPos2).move(Direction.DOWN)).getBlock();
				boolean bl = block instanceof LeavesBlock || block.is(BlockTags.LOGS);
				if (bl && this.mob.level.isEmptyBlock(blockPos2) && this.mob.level.isEmptyBlock(mutableBlockPos.set(blockPos2).move(Direction.UP))) {
					return new Vec3((double)blockPos2.getX(), (double)blockPos2.getY(), (double)blockPos2.getZ());
				}
			}
		}

		return null;
	}
}
