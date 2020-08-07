package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;

public class NoneFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<NoneFeatureConfiguration> CODEC = Codec.unit((Supplier<NoneFeatureConfiguration>)(() -> NoneFeatureConfiguration.INSTANCE));
	public static final NoneFeatureConfiguration INSTANCE = new NoneFeatureConfiguration();
}
