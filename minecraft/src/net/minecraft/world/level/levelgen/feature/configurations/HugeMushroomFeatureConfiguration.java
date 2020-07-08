package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class HugeMushroomFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<HugeMushroomFeatureConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockStateProvider.CODEC.fieldOf("cap_provider").forGetter(hugeMushroomFeatureConfiguration -> hugeMushroomFeatureConfiguration.capProvider),
					BlockStateProvider.CODEC.fieldOf("stem_provider").forGetter(hugeMushroomFeatureConfiguration -> hugeMushroomFeatureConfiguration.stemProvider),
					Codec.INT.fieldOf("foliage_radius").orElse(2).forGetter(hugeMushroomFeatureConfiguration -> hugeMushroomFeatureConfiguration.foliageRadius)
				)
				.apply(instance, HugeMushroomFeatureConfiguration::new)
	);
	public final BlockStateProvider capProvider;
	public final BlockStateProvider stemProvider;
	public final int foliageRadius;

	public HugeMushroomFeatureConfiguration(BlockStateProvider blockStateProvider, BlockStateProvider blockStateProvider2, int i) {
		this.capProvider = blockStateProvider;
		this.stemProvider = blockStateProvider2;
		this.foliageRadius = i;
	}
}
