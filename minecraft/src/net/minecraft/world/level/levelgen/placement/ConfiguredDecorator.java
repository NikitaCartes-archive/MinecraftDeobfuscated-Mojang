package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.Decoratable;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class ConfiguredDecorator<DC extends DecoratorConfiguration> implements Decoratable<ConfiguredDecorator<?>> {
	public static final Codec<ConfiguredDecorator<?>> CODEC = Registry.DECORATOR
		.dispatch("type", configuredDecorator -> configuredDecorator.decorator, FeatureDecorator::configuredCodec);
	private final FeatureDecorator<DC> decorator;
	private final DC config;

	public ConfiguredDecorator(FeatureDecorator<DC> featureDecorator, DC decoratorConfiguration) {
		this.decorator = featureDecorator;
		this.config = decoratorConfiguration;
	}

	public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, BlockPos blockPos) {
		return this.decorator.getPositions(decorationContext, random, this.config, blockPos);
	}

	public String toString() {
		return String.format("[%s %s]", Registry.DECORATOR.getKey(this.decorator), this.config);
	}

	public ConfiguredDecorator<?> decorated(ConfiguredDecorator<?> configuredDecorator) {
		return new ConfiguredDecorator<>(FeatureDecorator.DECORATED, new DecoratedDecoratorConfiguration(configuredDecorator, this));
	}

	public DC config() {
		return this.config;
	}
}
