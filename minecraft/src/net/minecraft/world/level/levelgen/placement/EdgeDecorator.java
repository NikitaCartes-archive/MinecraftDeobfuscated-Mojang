package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public abstract class EdgeDecorator<DC extends DecoratorConfiguration> extends FeatureDecorator<DC> {
	public EdgeDecorator(Codec<DC> codec) {
		super(codec);
	}

	protected abstract Heightmap.Types type(DC decoratorConfiguration);
}
