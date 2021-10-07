package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.BlockFilterConfiguration;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public interface Decoratable<R> {
	R decorated(ConfiguredDecorator<?> configuredDecorator);

	default R rarity(int i) {
		return this.decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(i)));
	}

	default R count(IntProvider intProvider) {
		return this.decorated(FeatureDecorator.COUNT.configured(new CountConfiguration(intProvider)));
	}

	default R count(int i) {
		return this.count(ConstantInt.of(i));
	}

	default R countRandom(int i) {
		return this.count(UniformInt.of(0, i));
	}

	default R rangeUniform(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
		return this.range(new RangeDecoratorConfiguration(UniformHeight.of(verticalAnchor, verticalAnchor2)));
	}

	default R rangeTriangle(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
		return this.range(new RangeDecoratorConfiguration(TrapezoidHeight.of(verticalAnchor, verticalAnchor2)));
	}

	default R range(RangeDecoratorConfiguration rangeDecoratorConfiguration) {
		return this.decorated(FeatureDecorator.RANGE.configured(rangeDecoratorConfiguration));
	}

	default R squared() {
		return this.decorated(FeatureDecorator.SQUARE.configured(NoneDecoratorConfiguration.INSTANCE));
	}

	default R filteredByBlockSurvival(Block block) {
		return this.filtered(BlockPredicate.wouldSurvive(block.defaultBlockState(), BlockPos.ZERO));
	}

	default R onlyWhenEmpty() {
		return this.filtered(BlockPredicate.matchesBlock(Blocks.AIR, BlockPos.ZERO));
	}

	default R filtered(BlockPredicate blockPredicate) {
		return this.decorated(FeatureDecorator.BLOCK_FILTER.configured(new BlockFilterConfiguration(blockPredicate)));
	}
}
