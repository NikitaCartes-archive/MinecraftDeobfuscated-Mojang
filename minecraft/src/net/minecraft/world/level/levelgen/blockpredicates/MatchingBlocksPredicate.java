package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;

class MatchingBlocksPredicate implements BlockPredicate {
	private final List<Block> blocks;
	private final BlockPos offset;
	public static final Codec<MatchingBlocksPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Registry.BLOCK.listOf().fieldOf("blocks").forGetter(matchingBlocksPredicate -> matchingBlocksPredicate.blocks),
					BlockPos.CODEC.fieldOf("offset").forGetter(matchingBlocksPredicate -> matchingBlocksPredicate.offset)
				)
				.apply(instance, MatchingBlocksPredicate::new)
	);

	public MatchingBlocksPredicate(List<Block> list, BlockPos blockPos) {
		this.blocks = list;
		this.offset = blockPos;
	}

	public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
		Block block = worldGenLevel.getBlockState(blockPos.offset(this.offset)).getBlock();
		return this.blocks.contains(block);
	}

	@Override
	public BlockPredicateType<?> type() {
		return BlockPredicateType.MATCHING_BLOCKS;
	}
}
