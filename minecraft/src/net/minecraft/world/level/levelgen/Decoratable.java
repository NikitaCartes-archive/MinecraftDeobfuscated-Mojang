package net.minecraft.world.level.levelgen;

import net.minecraft.util.UniformInt;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public interface Decoratable<R> {
	R decorated(ConfiguredDecorator<?> configuredDecorator);

	default R rarity(int i) {
		return this.decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(i)));
	}

	default R count(UniformInt uniformInt) {
		return this.decorated(FeatureDecorator.COUNT.configured(new CountConfiguration(uniformInt)));
	}

	default R count(int i) {
		return this.count(UniformInt.fixed(i));
	}

	default R countRandom(int i) {
		return this.count(UniformInt.of(0, i));
	}

	default R range(int i) {
		return this.decorated(FeatureDecorator.RANGE.configured(new RangeDecoratorConfiguration(0, 0, i)));
	}

	default R squared() {
		return this.decorated(FeatureDecorator.SQUARE.configured(NoneDecoratorConfiguration.INSTANCE));
	}
}
