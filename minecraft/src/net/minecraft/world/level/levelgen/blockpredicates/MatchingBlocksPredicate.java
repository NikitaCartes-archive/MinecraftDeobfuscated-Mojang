package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

class MatchingBlocksPredicate extends StateTestingPredicate {
	private final HolderSet<Block> blocks;
	public static final Codec<MatchingBlocksPredicate> CODEC = RecordCodecBuilder.create(
		instance -> stateTestingCodec(instance)
				.and(RegistryCodecs.homogeneousList(Registry.BLOCK_REGISTRY).fieldOf("blocks").forGetter(matchingBlocksPredicate -> matchingBlocksPredicate.blocks))
				.apply(instance, MatchingBlocksPredicate::new)
	);

	public MatchingBlocksPredicate(Vec3i vec3i, HolderSet<Block> holderSet) {
		super(vec3i);
		this.blocks = holderSet;
	}

	@Override
	protected boolean test(BlockState blockState) {
		return blockState.is(this.blocks);
	}

	@Override
	public BlockPredicateType<?> type() {
		return BlockPredicateType.MATCHING_BLOCKS;
	}
}
