package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

class TrueBlockPredicate implements BlockPredicate {
	public static TrueBlockPredicate INSTANCE = new TrueBlockPredicate();
	public static final MapCodec<TrueBlockPredicate> CODEC = MapCodec.unit((Supplier<TrueBlockPredicate>)(() -> INSTANCE));

	private TrueBlockPredicate() {
	}

	public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
		return true;
	}

	@Override
	public BlockPredicateType<?> type() {
		return BlockPredicateType.TRUE;
	}
}
