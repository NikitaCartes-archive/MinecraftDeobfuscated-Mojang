package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;

public class EatBlockGoal extends Goal {
	private static final Predicate<BlockState> IS_TALL_GRASS = BlockStatePredicate.forBlock(Blocks.GRASS);
	private final Mob mob;
	private final Level level;
	private int eatAnimationTick;

	public EatBlockGoal(Mob mob) {
		this.mob = mob;
		this.level = mob.level;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (this.mob.getRandom().nextInt(this.mob.isBaby() ? 50 : 1000) != 0) {
			return false;
		} else {
			BlockPos blockPos = this.mob.blockPosition();
			return IS_TALL_GRASS.test(this.level.getBlockState(blockPos)) ? true : this.level.getBlockState(blockPos.below()).is(Blocks.GRASS_BLOCK);
		}
	}

	@Override
	public void start() {
		this.eatAnimationTick = 40;
		this.level.broadcastEntityEvent(this.mob, (byte)10);
		this.mob.getNavigation().stop();
	}

	@Override
	public void stop() {
		this.eatAnimationTick = 0;
	}

	@Override
	public boolean canContinueToUse() {
		return this.eatAnimationTick > 0;
	}

	public int getEatAnimationTick() {
		return this.eatAnimationTick;
	}

	@Override
	public void tick() {
		this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
		if (this.eatAnimationTick == 4) {
			BlockPos blockPos = this.mob.blockPosition();
			if (IS_TALL_GRASS.test(this.level.getBlockState(blockPos))) {
				if (this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
					this.level.destroyBlock(blockPos, false);
				}

				this.mob.ate();
			} else {
				BlockPos blockPos2 = blockPos.below();
				if (this.level.getBlockState(blockPos2).is(Blocks.GRASS_BLOCK)) {
					if (this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
						this.level.levelEvent(2001, blockPos2, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
						this.level.setBlock(blockPos2, Blocks.DIRT.defaultBlockState(), 2);
					}

					this.mob.ate();
				}
			}
		}
	}
}
