package net.minecraft.world.level.levelgen;

import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import net.minecraft.world.level.levelgen.placement.DepthAverageConfiguration;
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

	default R range(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
		return this.range(new RangeDecoratorConfiguration(verticalAnchor, verticalAnchor2));
	}

	default R range(RangeDecoratorConfiguration rangeDecoratorConfiguration) {
		return this.decorated(FeatureDecorator.RANGE.configured(rangeDecoratorConfiguration));
	}

	default R squared() {
		return this.decorated(FeatureDecorator.SQUARE.configured(NoneDecoratorConfiguration.INSTANCE));
	}

	default R depthAverage(VerticalAnchor verticalAnchor, int i) {
		return this.decorated(FeatureDecorator.DEPTH_AVERAGE.configured(new DepthAverageConfiguration(verticalAnchor, i)));
	}
}
