package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

class AnyOfPredicate extends CombiningPredicate {
	public static final MapCodec<AnyOfPredicate> CODEC = codec(AnyOfPredicate::new);

	public AnyOfPredicate(List<BlockPredicate> list) {
		super(list);
	}

	public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
		for (BlockPredicate blockPredicate : this.predicates) {
			if (blockPredicate.test(worldGenLevel, blockPos)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public BlockPredicateType<?> type() {
		return BlockPredicateType.ANY_OF;
	}
}
