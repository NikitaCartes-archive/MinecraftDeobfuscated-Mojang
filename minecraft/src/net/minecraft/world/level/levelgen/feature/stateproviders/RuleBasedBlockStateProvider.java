package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

public record RuleBasedBlockStateProvider(BlockStateProvider fallback, List<RuleBasedBlockStateProvider.Rule> rules) {
	public static final Codec<RuleBasedBlockStateProvider> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockStateProvider.CODEC.fieldOf("fallback").forGetter(RuleBasedBlockStateProvider::fallback),
					RuleBasedBlockStateProvider.Rule.CODEC.listOf().fieldOf("rules").forGetter(RuleBasedBlockStateProvider::rules)
				)
				.apply(instance, RuleBasedBlockStateProvider::new)
	);

	public static RuleBasedBlockStateProvider simple(BlockStateProvider blockStateProvider) {
		return new RuleBasedBlockStateProvider(blockStateProvider, List.of());
	}

	public static RuleBasedBlockStateProvider simple(Block block) {
		return simple(BlockStateProvider.simple(block));
	}

	public BlockState getState(WorldGenLevel worldGenLevel, RandomSource randomSource, BlockPos blockPos) {
		for (RuleBasedBlockStateProvider.Rule rule : this.rules) {
			if (rule.ifTrue().test(worldGenLevel, blockPos)) {
				return rule.then().getState(randomSource, blockPos);
			}
		}

		return this.fallback.getState(randomSource, blockPos);
	}

	public static record Rule(BlockPredicate ifTrue, BlockStateProvider then) {
		public static final Codec<RuleBasedBlockStateProvider.Rule> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						BlockPredicate.CODEC.fieldOf("if_true").forGetter(RuleBasedBlockStateProvider.Rule::ifTrue),
						BlockStateProvider.CODEC.fieldOf("then").forGetter(RuleBasedBlockStateProvider.Rule::then)
					)
					.apply(instance, RuleBasedBlockStateProvider.Rule::new)
		);
	}
}
