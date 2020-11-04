package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;

public class GeodeConfiguration implements FeatureConfiguration {
	public static final Codec<Double> CHANCE_RANGE = Codec.doubleRange(0.0, 1.0);
	public static final Codec<GeodeConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					GeodeBlockSettings.CODEC.fieldOf("blocks").forGetter(geodeConfiguration -> geodeConfiguration.geodeBlockSettings),
					GeodeLayerSettings.CODEC.fieldOf("layers").forGetter(geodeConfiguration -> geodeConfiguration.geodeLayerSettings),
					GeodeCrackSettings.CODEC.fieldOf("crack").forGetter(geodeConfiguration -> geodeConfiguration.geodeCrackSettings),
					CHANCE_RANGE.fieldOf("use_potential_placements_chance").orElse(0.35).forGetter(geodeConfiguration -> geodeConfiguration.usePotentialPlacementsChance),
					CHANCE_RANGE.fieldOf("use_alternate_layer0_chance").orElse(0.0).forGetter(geodeConfiguration -> geodeConfiguration.useAlternateLayer0Chance),
					Codec.BOOL
						.fieldOf("placements_require_layer0_alternate")
						.orElse(true)
						.forGetter(geodeConfiguration -> geodeConfiguration.placementsRequireLayer0Alternate),
					Codec.intRange(1, 10).fieldOf("min_outer_wall_distance").orElse(4).forGetter(geodeConfiguration -> geodeConfiguration.minOuterWallDistance),
					Codec.intRange(1, 20).fieldOf("max_outer_wall_distance").orElse(6).forGetter(geodeConfiguration -> geodeConfiguration.maxOuterWallDistance),
					Codec.intRange(1, 10).fieldOf("min_distribution_points").orElse(3).forGetter(geodeConfiguration -> geodeConfiguration.minDistributionPoints),
					Codec.intRange(1, 20).fieldOf("max_distribution_points").orElse(5).forGetter(geodeConfiguration -> geodeConfiguration.maxDistributionPoints),
					Codec.intRange(0, 10).fieldOf("min_point_offset").orElse(1).forGetter(geodeConfiguration -> geodeConfiguration.minPointOffset),
					Codec.intRange(0, 10).fieldOf("max_point_offset").orElse(3).forGetter(geodeConfiguration -> geodeConfiguration.maxPointOffset),
					Codec.INT.fieldOf("min_gen_offset").orElse(-16).forGetter(geodeConfiguration -> geodeConfiguration.minGenOffset),
					Codec.INT.fieldOf("max_gen_offset").orElse(16).forGetter(geodeConfiguration -> geodeConfiguration.maxGenOffset),
					CHANCE_RANGE.fieldOf("noise_multiplier").orElse(0.05).forGetter(geodeConfiguration -> geodeConfiguration.noiseMultiplier)
				)
				.apply(instance, GeodeConfiguration::new)
	);
	public final GeodeBlockSettings geodeBlockSettings;
	public final GeodeLayerSettings geodeLayerSettings;
	public final GeodeCrackSettings geodeCrackSettings;
	public final double usePotentialPlacementsChance;
	public final double useAlternateLayer0Chance;
	public final boolean placementsRequireLayer0Alternate;
	public final int minOuterWallDistance;
	public final int maxOuterWallDistance;
	public final int minDistributionPoints;
	public final int maxDistributionPoints;
	public final int minPointOffset;
	public final int maxPointOffset;
	public final int minGenOffset;
	public final int maxGenOffset;
	public final double noiseMultiplier;

	public GeodeConfiguration(
		GeodeBlockSettings geodeBlockSettings,
		GeodeLayerSettings geodeLayerSettings,
		GeodeCrackSettings geodeCrackSettings,
		double d,
		double e,
		boolean bl,
		int i,
		int j,
		int k,
		int l,
		int m,
		int n,
		int o,
		int p,
		double f
	) {
		this.geodeBlockSettings = geodeBlockSettings;
		this.geodeLayerSettings = geodeLayerSettings;
		this.geodeCrackSettings = geodeCrackSettings;
		this.usePotentialPlacementsChance = d;
		this.useAlternateLayer0Chance = e;
		this.placementsRequireLayer0Alternate = bl;
		this.minOuterWallDistance = i;
		this.maxOuterWallDistance = j;
		this.minDistributionPoints = k;
		this.maxDistributionPoints = l;
		this.minPointOffset = m;
		this.maxPointOffset = n;
		this.minGenOffset = o;
		this.maxGenOffset = p;
		this.noiseMultiplier = f;
	}
}
