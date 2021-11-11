package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

class MatchingBlocksPredicate extends StateTestingPredicate {
	private final List<Block> blocks;
	public static final Codec<MatchingBlocksPredicate> CODEC = RecordCodecBuilder.create(
		instance -> stateTestingCodec(instance)
				.and(Registry.BLOCK.byNameCodec().listOf().fieldOf("blocks").forGetter(matchingBlocksPredicate -> matchingBlocksPredicate.blocks))
				.apply(instance, MatchingBlocksPredicate::new)
	);

	public MatchingBlocksPredicate(Vec3i vec3i, List<Block> list) {
		super(vec3i);
		this.blocks = list;
	}

	@Override
	protected boolean test(BlockState blockState) {
		return this.blocks.contains(blockState.getBlock());
	}

	@Override
	public BlockPredicateType<?> type() {
		return BlockPredicateType.MATCHING_BLOCKS;
	}
}
