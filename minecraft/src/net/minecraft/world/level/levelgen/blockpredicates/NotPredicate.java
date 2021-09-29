package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

class NotPredicate implements BlockPredicate {
	public static final Codec<NotPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(BlockPredicate.CODEC.fieldOf("predicate").forGetter(notPredicate -> notPredicate.predicate)).apply(instance, NotPredicate::new)
	);
	private final BlockPredicate predicate;

	public NotPredicate(BlockPredicate blockPredicate) {
		this.predicate = blockPredicate;
	}

	public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
		return !this.predicate.test(worldGenLevel, blockPos);
	}

	@Override
	public BlockPredicateType<?> type() {
		return BlockPredicateType.NOT;
	}
}
