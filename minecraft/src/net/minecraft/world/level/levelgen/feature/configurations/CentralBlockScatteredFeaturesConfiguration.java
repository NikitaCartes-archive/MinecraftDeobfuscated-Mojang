package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class CentralBlockScatteredFeaturesConfiguration implements FeatureConfiguration {
	public static final Codec<CentralBlockScatteredFeaturesConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ResourceLocation.CODEC
						.fieldOf("can_place_central_block_on")
						.forGetter(centralBlockScatteredFeaturesConfiguration -> centralBlockScatteredFeaturesConfiguration.canPlaceCentralBlockOn),
					BlockStateProvider.CODEC
						.fieldOf("central_state")
						.forGetter(centralBlockScatteredFeaturesConfiguration -> centralBlockScatteredFeaturesConfiguration.centralState),
					ConfiguredFeature.CODEC
						.fieldOf("scattered_feature")
						.forGetter(centralBlockScatteredFeaturesConfiguration -> centralBlockScatteredFeaturesConfiguration.scatteredFeature),
					ConfiguredFeature.CODEC
						.fieldOf("central_feature")
						.forGetter(centralBlockScatteredFeaturesConfiguration -> centralBlockScatteredFeaturesConfiguration.centralFeature),
					CaveSurface.CODEC.fieldOf("surface").forGetter(centralBlockScatteredFeaturesConfiguration -> centralBlockScatteredFeaturesConfiguration.surface),
					Codec.intRange(1, 256)
						.fieldOf("vertical_range")
						.forGetter(centralBlockScatteredFeaturesConfiguration -> centralBlockScatteredFeaturesConfiguration.verticalRange),
					Codec.intRange(1, 256)
						.fieldOf("feature_count_min")
						.forGetter(centralBlockScatteredFeaturesConfiguration -> centralBlockScatteredFeaturesConfiguration.featureCountMin),
					Codec.intRange(1, 256)
						.fieldOf("feature_count_max")
						.forGetter(centralBlockScatteredFeaturesConfiguration -> centralBlockScatteredFeaturesConfiguration.featureCountMax),
					Codec.intRange(1, 256)
						.fieldOf("max_feature_distance")
						.forGetter(centralBlockScatteredFeaturesConfiguration -> centralBlockScatteredFeaturesConfiguration.maxFeatureDistance)
				)
				.apply(instance, CentralBlockScatteredFeaturesConfiguration::new)
	);
	public final ResourceLocation canPlaceCentralBlockOn;
	public final BlockStateProvider centralState;
	public final Supplier<ConfiguredFeature<?, ?>> scatteredFeature;
	public final Supplier<ConfiguredFeature<?, ?>> centralFeature;
	public final CaveSurface surface;
	public final int verticalRange;
	public final int featureCountMin;
	public final int featureCountMax;
	public final int maxFeatureDistance;

	public CentralBlockScatteredFeaturesConfiguration(
		ResourceLocation resourceLocation,
		BlockStateProvider blockStateProvider,
		Supplier<ConfiguredFeature<?, ?>> supplier,
		Supplier<ConfiguredFeature<?, ?>> supplier2,
		CaveSurface caveSurface,
		int i,
		int j,
		int k,
		int l
	) {
		this.canPlaceCentralBlockOn = resourceLocation;
		this.centralState = blockStateProvider;
		this.scatteredFeature = supplier;
		this.centralFeature = supplier2;
		this.surface = caveSurface;
		this.verticalRange = i;
		this.featureCountMin = j;
		this.featureCountMax = k;
		this.maxFeatureDistance = l;
	}
}
