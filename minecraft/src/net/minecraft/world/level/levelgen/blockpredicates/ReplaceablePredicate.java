package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

class ReplaceablePredicate implements BlockPredicate {
	public static final ReplaceablePredicate INSTANCE = new ReplaceablePredicate();
	public static final Codec<ReplaceablePredicate> CODEC = Codec.unit((Supplier<ReplaceablePredicate>)(() -> INSTANCE));

	private ReplaceablePredicate() {
	}

	public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
		return worldGenLevel.getBlockState(blockPos).getMaterial().isReplaceable();
	}

	@Override
	public BlockPredicateType<?> type() {
		return BlockPredicateType.REPLACEABLE;
	}
}
