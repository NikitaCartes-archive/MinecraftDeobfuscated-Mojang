package net.minecraft.world.level.biome;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceKey;

public final class OverworldBiomeBuilder {
	private static final float VALLEY_SIZE = 0.05F;
	private static final float LOW_START = 0.26666668F;
	public static final float HIGH_START = 0.4F;
	private static final float HIGH_END = 0.93333334F;
	private static final float PEAK_SIZE = 0.1F;
	public static final float PEAK_START = 0.56666666F;
	private static final float PEAK_END = 0.7666667F;
	public static final float NEAR_INLAND_START = -0.11F;
	public static final float MID_INLAND_START = 0.03F;
	public static final float FAR_INLAND_START = 0.3F;
	public static final float EROSION_INDEX_1_START = -0.78F;
	public static final float EROSION_INDEX_2_START = -0.375F;
	private final Climate.Parameter FULL_RANGE = Climate.Parameter.span(-1.0F, 1.0F);
	private final Climate.Parameter[] temperatures = new Climate.Parameter[]{
		Climate.Parameter.span(-1.0F, -0.45F),
		Climate.Parameter.span(-0.45F, -0.15F),
		Climate.Parameter.span(-0.15F, 0.2F),
		Climate.Parameter.span(0.2F, 0.55F),
		Climate.Parameter.span(0.55F, 1.0F)
	};
	private final Climate.Parameter[] humidities = new Climate.Parameter[]{
		Climate.Parameter.span(-1.0F, -0.35F),
		Climate.Parameter.span(-0.35F, -0.1F),
		Climate.Parameter.span(-0.1F, 0.1F),
		Climate.Parameter.span(0.1F, 0.3F),
		Climate.Parameter.span(0.3F, 1.0F)
	};
	private final Climate.Parameter[] erosions = new Climate.Parameter[]{
		Climate.Parameter.span(-1.0F, -0.78F),
		Climate.Parameter.span(-0.78F, -0.375F),
		Climate.Parameter.span(-0.375F, -0.2225F),
		Climate.Parameter.span(-0.2225F, 0.05F),
		Climate.Parameter.span(0.05F, 0.45F),
		Climate.Parameter.span(0.45F, 0.55F),
		Climate.Parameter.span(0.55F, 1.0F)
	};
	private final Climate.Parameter FROZEN_RANGE = this.temperatures[0];
	private final Climate.Parameter UNFROZEN_RANGE = Climate.Parameter.span(this.temperatures[1], this.temperatures[4]);
	private final Climate.Parameter mushroomFieldsContinentalness = Climate.Parameter.span(-1.2F, -1.05F);
	private final Climate.Parameter deepOceanContinentalness = Climate.Parameter.span(-1.05F, -0.455F);
	private final Climate.Parameter oceanContinentalness = Climate.Parameter.span(-0.455F, -0.19F);
	private final Climate.Parameter coastContinentalness = Climate.Parameter.span(-0.19F, -0.11F);
	private final Climate.Parameter inlandContinentalness = Climate.Parameter.span(-0.11F, 0.55F);
	private final Climate.Parameter nearInlandContinentalness = Climate.Parameter.span(-0.11F, 0.03F);
	private final Climate.Parameter midInlandContinentalness = Climate.Parameter.span(0.03F, 0.3F);
	private final Climate.Parameter farInlandContinentalness = Climate.Parameter.span(0.3F, 1.0F);
	private final ResourceKey<Biome>[][] OCEANS = new ResourceKey[][]{
		{Biomes.DEEP_FROZEN_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.WARM_OCEAN},
		{Biomes.FROZEN_OCEAN, Biomes.COLD_OCEAN, Biomes.OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.WARM_OCEAN}
	};
	private final ResourceKey<Biome>[][] MIDDLE_BIOMES = new ResourceKey[][]{
		{Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.TAIGA},
		{Biomes.PLAINS, Biomes.PLAINS, Biomes.FOREST, Biomes.TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA},
		{Biomes.FLOWER_FOREST, Biomes.PLAINS, Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.DARK_FOREST},
		{Biomes.SAVANNA, Biomes.SAVANNA, Biomes.FOREST, Biomes.JUNGLE, Biomes.JUNGLE},
		{Biomes.DESERT, Biomes.DESERT, Biomes.DESERT, Biomes.DESERT, Biomes.DESERT}
	};
	private final ResourceKey<Biome>[][] MIDDLE_BIOMES_VARIANT = new ResourceKey[][]{
		{Biomes.ICE_SPIKES, null, Biomes.SNOWY_TAIGA, null, null},
		{null, null, null, null, Biomes.OLD_GROWTH_PINE_TAIGA},
		{Biomes.SUNFLOWER_PLAINS, null, null, Biomes.OLD_GROWTH_BIRCH_FOREST, null},
		{null, null, Biomes.PLAINS, Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE},
		{null, null, null, null, null}
	};
	private final ResourceKey<Biome>[][] PLATEAU_BIOMES = new ResourceKey[][]{
		{Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.SNOWY_TAIGA},
		{Biomes.MEADOW, Biomes.MEADOW, Biomes.FOREST, Biomes.TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA},
		{Biomes.MEADOW, Biomes.MEADOW, Biomes.MEADOW, Biomes.MEADOW, Biomes.DARK_FOREST},
		{Biomes.SAVANNA_PLATEAU, Biomes.SAVANNA_PLATEAU, Biomes.FOREST, Biomes.FOREST, Biomes.JUNGLE},
		{Biomes.BADLANDS, Biomes.BADLANDS, Biomes.BADLANDS, Biomes.WOODED_BADLANDS, Biomes.WOODED_BADLANDS}
	};
	private final ResourceKey<Biome>[][] PLATEAU_BIOMES_VARIANT = new ResourceKey[][]{
		{Biomes.ICE_SPIKES, null, null, null, null},
		{null, null, Biomes.MEADOW, Biomes.MEADOW, Biomes.OLD_GROWTH_PINE_TAIGA},
		{null, null, Biomes.FOREST, Biomes.BIRCH_FOREST, null},
		{null, null, null, null, null},
		{Biomes.ERODED_BADLANDS, Biomes.ERODED_BADLANDS, null, null, null}
	};
	private final ResourceKey<Biome>[][] EXTREME_HILLS = new ResourceKey[][]{
		{Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST},
		{Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST},
		{Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST},
		{null, null, null, null, null},
		{null, null, null, null, null}
	};

	public List<Climate.ParameterPoint> spawnTarget() {
		Climate.Parameter parameter = Climate.Parameter.point(0.0F);
		float f = 0.16F;
		return List.of(
			new Climate.ParameterPoint(
				this.FULL_RANGE,
				this.FULL_RANGE,
				Climate.Parameter.span(this.inlandContinentalness, this.FULL_RANGE),
				this.FULL_RANGE,
				parameter,
				Climate.Parameter.span(-1.0F, -0.16F),
				0L
			),
			new Climate.ParameterPoint(
				this.FULL_RANGE,
				this.FULL_RANGE,
				Climate.Parameter.span(this.inlandContinentalness, this.FULL_RANGE),
				this.FULL_RANGE,
				parameter,
				Climate.Parameter.span(0.16F, 1.0F),
				0L
			)
		);
	}

	protected void addBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer) {
		if (SharedConstants.debugGenerateSquareTerrainWithoutNoise) {
			new TerrainShaper().addDebugBiomesToVisualizeSplinePoints(consumer);
		} else {
			this.addOffCoastBiomes(consumer);
			this.addInlandBiomes(consumer);
			this.addUndergroundBiomes(consumer);
		}
	}

	private void addOffCoastBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer) {
		this.addSurfaceBiome(
			consumer, this.FULL_RANGE, this.FULL_RANGE, this.mushroomFieldsContinentalness, this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.MUSHROOM_FIELDS
		);

		for (int i = 0; i < this.temperatures.length; i++) {
			Climate.Parameter parameter = this.temperatures[i];
			this.addSurfaceBiome(consumer, parameter, this.FULL_RANGE, this.deepOceanContinentalness, this.FULL_RANGE, this.FULL_RANGE, 0.0F, this.OCEANS[0][i]);
			this.addSurfaceBiome(consumer, parameter, this.FULL_RANGE, this.oceanContinentalness, this.FULL_RANGE, this.FULL_RANGE, 0.0F, this.OCEANS[1][i]);
		}
	}

	private void addInlandBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer) {
		this.addMidSlice(consumer, Climate.Parameter.span(-1.0F, -0.93333334F));
		this.addHighSlice(consumer, Climate.Parameter.span(-0.93333334F, -0.7666667F));
		this.addPeaks(consumer, Climate.Parameter.span(-0.7666667F, -0.56666666F));
		this.addHighSlice(consumer, Climate.Parameter.span(-0.56666666F, -0.4F));
		this.addMidSlice(consumer, Climate.Parameter.span(-0.4F, -0.26666668F));
		this.addLowSlice(consumer, Climate.Parameter.span(-0.26666668F, -0.05F));
		this.addValleys(consumer, Climate.Parameter.span(-0.05F, 0.05F));
		this.addLowSlice(consumer, Climate.Parameter.span(0.05F, 0.26666668F));
		this.addMidSlice(consumer, Climate.Parameter.span(0.26666668F, 0.4F));
		this.addHighSlice(consumer, Climate.Parameter.span(0.4F, 0.56666666F));
		this.addPeaks(consumer, Climate.Parameter.span(0.56666666F, 0.7666667F));
		this.addHighSlice(consumer, Climate.Parameter.span(0.7666667F, 0.93333334F));
		this.addMidSlice(consumer, Climate.Parameter.span(0.93333334F, 1.0F));
	}

	private void addPeaks(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter parameter) {
		for (int i = 0; i < this.temperatures.length; i++) {
			Climate.Parameter parameter2 = this.temperatures[i];

			for (int j = 0; j < this.humidities.length; j++) {
				Climate.Parameter parameter3 = this.humidities[j];
				ResourceKey<Biome> resourceKey = this.pickMiddleBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey2 = this.pickMiddleBiomeOrBadlandsIfHot(i, j, parameter);
				ResourceKey<Biome> resourceKey3 = this.pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(i, j, parameter);
				ResourceKey<Biome> resourceKey4 = this.pickPlateauBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey5 = this.pickExtremeHillsBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey6 = this.maybePickShatteredBiome(i, j, parameter, resourceKey5);
				ResourceKey<Biome> resourceKey7 = this.pickPeakBiome(i, j, parameter);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
					this.erosions[0],
					parameter,
					0.0F,
					resourceKey7
				);
				this.addSurfaceBiome(consumer, parameter2, parameter3, this.nearInlandContinentalness, this.erosions[1], parameter, 0.0F, resourceKey3);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[1],
					parameter,
					0.0F,
					resourceKey7
				);
				this.addSurfaceBiome(
					consumer, parameter2, parameter3, this.nearInlandContinentalness, Climate.Parameter.span(this.erosions[2], this.erosions[3]), parameter, 0.0F, resourceKey
				);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[2],
					parameter,
					0.0F,
					resourceKey4
				);
				this.addSurfaceBiome(consumer, parameter2, parameter3, this.midInlandContinentalness, this.erosions[3], parameter, 0.0F, resourceKey2);
				this.addSurfaceBiome(consumer, parameter2, parameter3, this.farInlandContinentalness, this.erosions[3], parameter, 0.0F, resourceKey4);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness),
					this.erosions[4],
					parameter,
					0.0F,
					resourceKey
				);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness),
					this.erosions[5],
					parameter,
					0.0F,
					resourceKey6
				);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[5],
					parameter,
					0.0F,
					resourceKey5
				);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness),
					this.erosions[6],
					parameter,
					0.0F,
					resourceKey
				);
			}
		}
	}

	private void addHighSlice(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter parameter) {
		for (int i = 0; i < this.temperatures.length; i++) {
			Climate.Parameter parameter2 = this.temperatures[i];

			for (int j = 0; j < this.humidities.length; j++) {
				Climate.Parameter parameter3 = this.humidities[j];
				ResourceKey<Biome> resourceKey = this.pickMiddleBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey2 = this.pickMiddleBiomeOrBadlandsIfHot(i, j, parameter);
				ResourceKey<Biome> resourceKey3 = this.pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(i, j, parameter);
				ResourceKey<Biome> resourceKey4 = this.pickPlateauBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey5 = this.pickExtremeHillsBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey6 = this.maybePickShatteredBiome(i, j, parameter, resourceKey);
				ResourceKey<Biome> resourceKey7 = this.pickSlopeBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey8 = this.pickPeakBiome(i, j, parameter);
				this.addSurfaceBiome(
					consumer, parameter2, parameter3, this.coastContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[1]), parameter, 0.0F, resourceKey
				);
				this.addSurfaceBiome(consumer, parameter2, parameter3, this.nearInlandContinentalness, this.erosions[0], parameter, 0.0F, resourceKey7);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[0],
					parameter,
					0.0F,
					resourceKey8
				);
				this.addSurfaceBiome(consumer, parameter2, parameter3, this.nearInlandContinentalness, this.erosions[1], parameter, 0.0F, resourceKey3);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[1],
					parameter,
					0.0F,
					resourceKey7
				);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness),
					Climate.Parameter.span(this.erosions[2], this.erosions[3]),
					parameter,
					0.0F,
					resourceKey
				);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[2],
					parameter,
					0.0F,
					resourceKey4
				);
				this.addSurfaceBiome(consumer, parameter2, parameter3, this.midInlandContinentalness, this.erosions[3], parameter, 0.0F, resourceKey2);
				this.addSurfaceBiome(consumer, parameter2, parameter3, this.farInlandContinentalness, this.erosions[3], parameter, 0.0F, resourceKey4);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness),
					this.erosions[4],
					parameter,
					0.0F,
					resourceKey
				);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness),
					this.erosions[5],
					parameter,
					0.0F,
					resourceKey6
				);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[5],
					parameter,
					0.0F,
					resourceKey5
				);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness),
					this.erosions[6],
					parameter,
					0.0F,
					resourceKey
				);
			}
		}
	}

	private void addMidSlice(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter parameter) {
		this.addSurfaceBiome(
			consumer,
			this.FULL_RANGE,
			this.FULL_RANGE,
			this.coastContinentalness,
			Climate.Parameter.span(this.erosions[0], this.erosions[2]),
			parameter,
			0.0F,
			Biomes.STONY_SHORE
		);
		this.addSurfaceBiome(
			consumer,
			this.UNFROZEN_RANGE,
			this.FULL_RANGE,
			Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
			this.erosions[6],
			parameter,
			0.0F,
			Biomes.SWAMP
		);

		for (int i = 0; i < this.temperatures.length; i++) {
			Climate.Parameter parameter2 = this.temperatures[i];

			for (int j = 0; j < this.humidities.length; j++) {
				Climate.Parameter parameter3 = this.humidities[j];
				ResourceKey<Biome> resourceKey = this.pickMiddleBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey2 = this.pickMiddleBiomeOrBadlandsIfHot(i, j, parameter);
				ResourceKey<Biome> resourceKey3 = this.pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(i, j, parameter);
				ResourceKey<Biome> resourceKey4 = this.pickExtremeHillsBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey5 = this.pickPlateauBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey6 = this.pickBeachBiome(i, j);
				ResourceKey<Biome> resourceKey7 = this.maybePickShatteredBiome(i, j, parameter, resourceKey);
				ResourceKey<Biome> resourceKey8 = this.pickShatteredCoastBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey9 = this.pickSlopeBiome(i, j, parameter);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
					this.erosions[0],
					parameter,
					0.0F,
					resourceKey9
				);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.nearInlandContinentalness, this.midInlandContinentalness),
					this.erosions[1],
					parameter,
					0.0F,
					resourceKey3
				);
				this.addSurfaceBiome(
					consumer, parameter2, parameter3, this.farInlandContinentalness, this.erosions[1], parameter, 0.0F, i == 0 ? resourceKey9 : resourceKey5
				);
				this.addSurfaceBiome(consumer, parameter2, parameter3, this.nearInlandContinentalness, this.erosions[2], parameter, 0.0F, resourceKey);
				this.addSurfaceBiome(consumer, parameter2, parameter3, this.midInlandContinentalness, this.erosions[2], parameter, 0.0F, resourceKey2);
				this.addSurfaceBiome(consumer, parameter2, parameter3, this.farInlandContinentalness, this.erosions[2], parameter, 0.0F, resourceKey5);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness),
					this.erosions[3],
					parameter,
					0.0F,
					resourceKey
				);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[3],
					parameter,
					0.0F,
					resourceKey2
				);
				if (parameter.max() < 0L) {
					this.addSurfaceBiome(consumer, parameter2, parameter3, this.coastContinentalness, this.erosions[4], parameter, 0.0F, resourceKey6);
					this.addSurfaceBiome(
						consumer,
						parameter2,
						parameter3,
						Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
						this.erosions[4],
						parameter,
						0.0F,
						resourceKey
					);
				} else {
					this.addSurfaceBiome(
						consumer,
						parameter2,
						parameter3,
						Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness),
						this.erosions[4],
						parameter,
						0.0F,
						resourceKey
					);
				}

				this.addSurfaceBiome(consumer, parameter2, parameter3, this.coastContinentalness, this.erosions[5], parameter, 0.0F, resourceKey8);
				this.addSurfaceBiome(consumer, parameter2, parameter3, this.nearInlandContinentalness, this.erosions[5], parameter, 0.0F, resourceKey7);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[5],
					parameter,
					0.0F,
					resourceKey4
				);
				if (parameter.max() < 0L) {
					this.addSurfaceBiome(consumer, parameter2, parameter3, this.coastContinentalness, this.erosions[6], parameter, 0.0F, resourceKey6);
				} else {
					this.addSurfaceBiome(consumer, parameter2, parameter3, this.coastContinentalness, this.erosions[6], parameter, 0.0F, resourceKey);
				}

				if (i == 0) {
					this.addSurfaceBiome(
						consumer,
						parameter2,
						parameter3,
						Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
						this.erosions[6],
						parameter,
						0.0F,
						resourceKey
					);
				}
			}
		}
	}

	private void addLowSlice(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter parameter) {
		this.addSurfaceBiome(
			consumer,
			this.FULL_RANGE,
			this.FULL_RANGE,
			this.coastContinentalness,
			Climate.Parameter.span(this.erosions[0], this.erosions[2]),
			parameter,
			0.0F,
			Biomes.STONY_SHORE
		);
		this.addSurfaceBiome(
			consumer,
			this.UNFROZEN_RANGE,
			this.FULL_RANGE,
			Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
			this.erosions[6],
			parameter,
			0.0F,
			Biomes.SWAMP
		);

		for (int i = 0; i < this.temperatures.length; i++) {
			Climate.Parameter parameter2 = this.temperatures[i];

			for (int j = 0; j < this.humidities.length; j++) {
				Climate.Parameter parameter3 = this.humidities[j];
				ResourceKey<Biome> resourceKey = this.pickMiddleBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey2 = this.pickMiddleBiomeOrBadlandsIfHot(i, j, parameter);
				ResourceKey<Biome> resourceKey3 = this.pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(i, j, parameter);
				ResourceKey<Biome> resourceKey4 = this.pickBeachBiome(i, j);
				ResourceKey<Biome> resourceKey5 = this.maybePickShatteredBiome(i, j, parameter, resourceKey);
				ResourceKey<Biome> resourceKey6 = this.pickShatteredCoastBiome(i, j, parameter);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					this.nearInlandContinentalness,
					Climate.Parameter.span(this.erosions[0], this.erosions[1]),
					parameter,
					0.0F,
					resourceKey2
				);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
					Climate.Parameter.span(this.erosions[0], this.erosions[1]),
					parameter,
					0.0F,
					resourceKey3
				);
				this.addSurfaceBiome(
					consumer, parameter2, parameter3, this.nearInlandContinentalness, Climate.Parameter.span(this.erosions[2], this.erosions[3]), parameter, 0.0F, resourceKey
				);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
					Climate.Parameter.span(this.erosions[2], this.erosions[3]),
					parameter,
					0.0F,
					resourceKey2
				);
				this.addSurfaceBiome(
					consumer, parameter2, parameter3, this.coastContinentalness, Climate.Parameter.span(this.erosions[3], this.erosions[4]), parameter, 0.0F, resourceKey4
				);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
					this.erosions[4],
					parameter,
					0.0F,
					resourceKey
				);
				this.addSurfaceBiome(consumer, parameter2, parameter3, this.coastContinentalness, this.erosions[5], parameter, 0.0F, resourceKey6);
				this.addSurfaceBiome(consumer, parameter2, parameter3, this.nearInlandContinentalness, this.erosions[5], parameter, 0.0F, resourceKey5);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[5],
					parameter,
					0.0F,
					resourceKey
				);
				this.addSurfaceBiome(consumer, parameter2, parameter3, this.coastContinentalness, this.erosions[6], parameter, 0.0F, resourceKey4);
				if (i == 0) {
					this.addSurfaceBiome(
						consumer,
						parameter2,
						parameter3,
						Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness),
						this.erosions[6],
						parameter,
						0.0F,
						resourceKey
					);
				}
			}
		}
	}

	private void addValleys(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, Climate.Parameter parameter) {
		this.addSurfaceBiome(
			consumer,
			this.FROZEN_RANGE,
			this.FULL_RANGE,
			this.coastContinentalness,
			Climate.Parameter.span(this.erosions[0], this.erosions[1]),
			parameter,
			0.0F,
			parameter.max() < 0L ? Biomes.STONY_SHORE : Biomes.FROZEN_RIVER
		);
		this.addSurfaceBiome(
			consumer,
			this.UNFROZEN_RANGE,
			this.FULL_RANGE,
			this.coastContinentalness,
			Climate.Parameter.span(this.erosions[0], this.erosions[1]),
			parameter,
			0.0F,
			parameter.max() < 0L ? Biomes.STONY_SHORE : Biomes.RIVER
		);
		this.addSurfaceBiome(
			consumer,
			this.FROZEN_RANGE,
			this.FULL_RANGE,
			this.nearInlandContinentalness,
			Climate.Parameter.span(this.erosions[0], this.erosions[1]),
			parameter,
			0.0F,
			Biomes.FROZEN_RIVER
		);
		this.addSurfaceBiome(
			consumer,
			this.UNFROZEN_RANGE,
			this.FULL_RANGE,
			this.nearInlandContinentalness,
			Climate.Parameter.span(this.erosions[0], this.erosions[1]),
			parameter,
			0.0F,
			Biomes.RIVER
		);
		this.addSurfaceBiome(
			consumer,
			this.FROZEN_RANGE,
			this.FULL_RANGE,
			Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness),
			Climate.Parameter.span(this.erosions[2], this.erosions[5]),
			parameter,
			0.0F,
			Biomes.FROZEN_RIVER
		);
		this.addSurfaceBiome(
			consumer,
			this.UNFROZEN_RANGE,
			this.FULL_RANGE,
			Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness),
			Climate.Parameter.span(this.erosions[2], this.erosions[5]),
			parameter,
			0.0F,
			Biomes.RIVER
		);
		this.addSurfaceBiome(consumer, this.FROZEN_RANGE, this.FULL_RANGE, this.coastContinentalness, this.erosions[6], parameter, 0.0F, Biomes.FROZEN_RIVER);
		this.addSurfaceBiome(consumer, this.UNFROZEN_RANGE, this.FULL_RANGE, this.coastContinentalness, this.erosions[6], parameter, 0.0F, Biomes.RIVER);
		this.addSurfaceBiome(
			consumer,
			this.UNFROZEN_RANGE,
			this.FULL_RANGE,
			Climate.Parameter.span(this.inlandContinentalness, this.farInlandContinentalness),
			this.erosions[6],
			parameter,
			0.0F,
			Biomes.SWAMP
		);
		this.addSurfaceBiome(
			consumer,
			this.FROZEN_RANGE,
			this.FULL_RANGE,
			Climate.Parameter.span(this.inlandContinentalness, this.farInlandContinentalness),
			this.erosions[6],
			parameter,
			0.0F,
			Biomes.FROZEN_RIVER
		);

		for (int i = 0; i < this.temperatures.length; i++) {
			Climate.Parameter parameter2 = this.temperatures[i];

			for (int j = 0; j < this.humidities.length; j++) {
				Climate.Parameter parameter3 = this.humidities[j];
				ResourceKey<Biome> resourceKey = this.pickMiddleBiomeOrBadlandsIfHot(i, j, parameter);
				this.addSurfaceBiome(
					consumer,
					parameter2,
					parameter3,
					Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness),
					Climate.Parameter.span(this.erosions[0], this.erosions[1]),
					parameter,
					0.0F,
					resourceKey
				);
			}
		}
	}

	private void addUndergroundBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer) {
		this.addUndergroundBiome(
			consumer, this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.span(0.8F, 1.0F), this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.DRIPSTONE_CAVES
		);
		this.addUndergroundBiome(
			consumer, this.FULL_RANGE, Climate.Parameter.span(0.7F, 1.0F), this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.LUSH_CAVES
		);
	}

	private ResourceKey<Biome> pickMiddleBiome(int i, int j, Climate.Parameter parameter) {
		if (parameter.max() < 0L) {
			return this.MIDDLE_BIOMES[i][j];
		} else {
			ResourceKey<Biome> resourceKey = this.MIDDLE_BIOMES_VARIANT[i][j];
			return resourceKey == null ? this.MIDDLE_BIOMES[i][j] : resourceKey;
		}
	}

	private ResourceKey<Biome> pickMiddleBiomeOrBadlandsIfHot(int i, int j, Climate.Parameter parameter) {
		return i == 4 ? this.pickBadlandsBiome(j, parameter) : this.pickMiddleBiome(i, j, parameter);
	}

	private ResourceKey<Biome> pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(int i, int j, Climate.Parameter parameter) {
		return i == 0 ? this.pickSlopeBiome(i, j, parameter) : this.pickMiddleBiomeOrBadlandsIfHot(i, j, parameter);
	}

	private ResourceKey<Biome> maybePickShatteredBiome(int i, int j, Climate.Parameter parameter, ResourceKey<Biome> resourceKey) {
		return i > 1 && j < 4 && parameter.max() >= 0L ? Biomes.WINDSWEPT_SAVANNA : resourceKey;
	}

	private ResourceKey<Biome> pickShatteredCoastBiome(int i, int j, Climate.Parameter parameter) {
		ResourceKey<Biome> resourceKey = parameter.max() >= 0L ? this.pickMiddleBiome(i, j, parameter) : this.pickBeachBiome(i, j);
		return this.maybePickShatteredBiome(i, j, parameter, resourceKey);
	}

	private ResourceKey<Biome> pickBeachBiome(int i, int j) {
		if (i == 0) {
			return Biomes.SNOWY_BEACH;
		} else {
			return i == 4 ? Biomes.DESERT : Biomes.BEACH;
		}
	}

	private ResourceKey<Biome> pickBadlandsBiome(int i, Climate.Parameter parameter) {
		if (i < 2) {
			return parameter.max() < 0L ? Biomes.ERODED_BADLANDS : Biomes.BADLANDS;
		} else {
			return i < 3 ? Biomes.BADLANDS : Biomes.WOODED_BADLANDS;
		}
	}

	private ResourceKey<Biome> pickPlateauBiome(int i, int j, Climate.Parameter parameter) {
		if (parameter.max() < 0L) {
			return this.PLATEAU_BIOMES[i][j];
		} else {
			ResourceKey<Biome> resourceKey = this.PLATEAU_BIOMES_VARIANT[i][j];
			return resourceKey == null ? this.PLATEAU_BIOMES[i][j] : resourceKey;
		}
	}

	private ResourceKey<Biome> pickPeakBiome(int i, int j, Climate.Parameter parameter) {
		if (i <= 2) {
			return parameter.max() < 0L ? Biomes.JAGGED_PEAKS : Biomes.FROZEN_PEAKS;
		} else {
			return i == 3 ? Biomes.STONY_PEAKS : this.pickBadlandsBiome(j, parameter);
		}
	}

	private ResourceKey<Biome> pickSlopeBiome(int i, int j, Climate.Parameter parameter) {
		if (i >= 3) {
			return this.pickPlateauBiome(i, j, parameter);
		} else {
			return j <= 1 ? Biomes.SNOWY_SLOPES : Biomes.GROVE;
		}
	}

	private ResourceKey<Biome> pickExtremeHillsBiome(int i, int j, Climate.Parameter parameter) {
		ResourceKey<Biome> resourceKey = this.EXTREME_HILLS[i][j];
		return resourceKey == null ? this.pickMiddleBiome(i, j, parameter) : resourceKey;
	}

	private void addSurfaceBiome(
		Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer,
		Climate.Parameter parameter,
		Climate.Parameter parameter2,
		Climate.Parameter parameter3,
		Climate.Parameter parameter4,
		Climate.Parameter parameter5,
		float f,
		ResourceKey<Biome> resourceKey
	) {
		consumer.accept(Pair.of(Climate.parameters(parameter, parameter2, parameter3, parameter4, Climate.Parameter.point(0.0F), parameter5, f), resourceKey));
		consumer.accept(Pair.of(Climate.parameters(parameter, parameter2, parameter3, parameter4, Climate.Parameter.point(1.0F), parameter5, f), resourceKey));
	}

	private void addUndergroundBiome(
		Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer,
		Climate.Parameter parameter,
		Climate.Parameter parameter2,
		Climate.Parameter parameter3,
		Climate.Parameter parameter4,
		Climate.Parameter parameter5,
		float f,
		ResourceKey<Biome> resourceKey
	) {
		consumer.accept(Pair.of(Climate.parameters(parameter, parameter2, parameter3, parameter4, Climate.Parameter.span(0.2F, 0.9F), parameter5, f), resourceKey));
	}

	public static String getDebugStringForPeaksAndValleys(double d) {
		if (d < (double)TerrainShaper.peaksAndValleys(0.05F)) {
			return "Valley";
		} else if (d < (double)TerrainShaper.peaksAndValleys(0.26666668F)) {
			return "Low";
		} else if (d < (double)TerrainShaper.peaksAndValleys(0.4F)) {
			return "Mid";
		} else {
			return d < (double)TerrainShaper.peaksAndValleys(0.56666666F) ? "High" : "Peak";
		}
	}

	public String getDebugStringForContinentalness(double d) {
		double e = (double)Climate.quantizeCoord((float)d);
		if (e < (double)this.mushroomFieldsContinentalness.max()) {
			return "Mushroom fields";
		} else if (e < (double)this.deepOceanContinentalness.max()) {
			return "Deep ocean";
		} else if (e < (double)this.oceanContinentalness.max()) {
			return "Ocean";
		} else if (e < (double)this.coastContinentalness.max()) {
			return "Coast";
		} else if (e < (double)this.nearInlandContinentalness.max()) {
			return "Near inland";
		} else {
			return e < (double)this.midInlandContinentalness.max() ? "Mid inland" : "Far inland";
		}
	}

	public String getDebugStringForErosion(double d) {
		return getDebugStringForNoiseValue(d, this.erosions);
	}

	public String getDebugStringForTemperature(double d) {
		return getDebugStringForNoiseValue(d, this.temperatures);
	}

	public String getDebugStringForHumidity(double d) {
		return getDebugStringForNoiseValue(d, this.humidities);
	}

	private static String getDebugStringForNoiseValue(double d, Climate.Parameter[] parameters) {
		double e = (double)Climate.quantizeCoord((float)d);

		for (int i = 0; i < parameters.length; i++) {
			if (e < (double)parameters[i].max()) {
				return i + "";
			}
		}

		return "?";
	}
}
