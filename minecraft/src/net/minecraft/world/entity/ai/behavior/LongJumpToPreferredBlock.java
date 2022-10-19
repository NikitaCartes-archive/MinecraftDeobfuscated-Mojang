package net.minecraft.world.entity.ai.behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;

public class LongJumpToPreferredBlock<E extends Mob> extends LongJumpToRandomPos<E> {
	private final TagKey<Block> preferredBlockTag;
	private final float preferredBlocksChance;
	private final List<LongJumpToRandomPos.PossibleJump> notPrefferedJumpCandidates = new ArrayList();
	private boolean currentlyWantingPreferredOnes;

	public LongJumpToPreferredBlock(
		UniformInt uniformInt, int i, int j, float f, Function<E, SoundEvent> function, TagKey<Block> tagKey, float g, BiPredicate<E, BlockPos> biPredicate
	) {
		super(uniformInt, i, j, f, function, biPredicate);
		this.preferredBlockTag = tagKey;
		this.preferredBlocksChance = g;
	}

	@Override
	protected void start(ServerLevel serverLevel, E mob, long l) {
		super.start(serverLevel, mob, l);
		this.notPrefferedJumpCandidates.clear();
		this.currentlyWantingPreferredOnes = mob.getRandom().nextFloat() < this.preferredBlocksChance;
	}

	@Override
	protected Optional<LongJumpToRandomPos.PossibleJump> getJumpCandidate(ServerLevel serverLevel) {
		if (!this.currentlyWantingPreferredOnes) {
			return super.getJumpCandidate(serverLevel);
		} else {
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			while (!this.jumpCandidates.isEmpty()) {
				Optional<LongJumpToRandomPos.PossibleJump> optional = super.getJumpCandidate(serverLevel);
				if (optional.isPresent()) {
					LongJumpToRandomPos.PossibleJump possibleJump = (LongJumpToRandomPos.PossibleJump)optional.get();
					if (serverLevel.getBlockState(mutableBlockPos.setWithOffset(possibleJump.getJumpTarget(), Direction.DOWN)).is(this.preferredBlockTag)) {
						return optional;
					}

					this.notPrefferedJumpCandidates.add(possibleJump);
				}
			}

			return !this.notPrefferedJumpCandidates.isEmpty()
				? Optional.of((LongJumpToRandomPos.PossibleJump)this.notPrefferedJumpCandidates.remove(0))
				: Optional.empty();
		}
	}
}
