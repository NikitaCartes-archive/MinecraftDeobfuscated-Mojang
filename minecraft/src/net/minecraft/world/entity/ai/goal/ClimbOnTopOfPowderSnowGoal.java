package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;

public class ClimbOnTopOfPowderSnowGoal extends Goal {
	private final Mob mob;
	private final Level level;

	public ClimbOnTopOfPowderSnowGoal(Mob mob, Level level) {
		this.mob = mob;
		this.level = level;
		this.setFlags(EnumSet.of(Goal.Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		boolean bl = this.mob.wasInPowderSnow || this.mob.isInPowderSnow;
		if (bl && this.mob.getType().is(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)) {
			BlockPos blockPos = this.mob.blockPosition().above();
			BlockState blockState = this.level.getBlockState(blockPos);
			return blockState.is(Blocks.POWDER_SNOW) || blockState.getCollisionShape(this.level, blockPos) == Shapes.empty();
		} else {
			return false;
		}
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		this.mob.getJumpControl().jump();
	}
}
