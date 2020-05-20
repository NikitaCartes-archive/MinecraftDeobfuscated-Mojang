package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;

public class DecoratedFlowerFeature extends DecoratedFeature {
	public DecoratedFlowerFeature(Codec<DecoratedFeatureConfiguration> codec) {
		super(codec);
	}
}
