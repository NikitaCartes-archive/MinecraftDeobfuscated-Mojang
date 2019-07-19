package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.block.state.BlockState;

public class FollowOwnerFlyingGoal extends FollowOwnerGoal {
	public FollowOwnerFlyingGoal(TamableAnimal tamableAnimal, double d, float f, float g) {
		super(tamableAnimal, d, f, g);
	}

	@Override
	protected boolean isTeleportFriendlyBlock(BlockPos blockPos) {
		BlockState blockState = this.level.getBlockState(blockPos);
		return (blockState.entityCanStandOn(this.level, blockPos, this.tamable) || blockState.is(BlockTags.LEAVES))
			&& this.level.isEmptyBlock(blockPos.above())
			&& this.level.isEmptyBlock(blockPos.above(2));
	}
}
