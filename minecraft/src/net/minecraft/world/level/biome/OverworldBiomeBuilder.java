package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceKey;

public final class OverworldBiomeBuilder {
	private final Climate.Parameter FULL_RANGE = Climate.range(-1.0F, 1.0F);
	private final Climate.Parameter[] temperatures = new Climate.Parameter[]{
		Climate.range(-1.0F, -0.45F), Climate.range(-0.45F, -0.15F), Climate.range(-0.15F, 0.2F), Climate.range(0.2F, 0.55F), Climate.range(0.55F, 1.0F)
	};
	private final Climate.Parameter[] humidities = new Climate.Parameter[]{
		Climate.range(-1.0F, -0.3F), Climate.range(-0.3F, -0.1F), Climate.range(-0.1F, 0.1F), Climate.range(0.1F, 0.3F), Climate.range(0.3F, 1.0F)
	};
	private final Climate.Parameter[] erosions = new Climate.Parameter[]{
		Climate.range(-1.0F, -0.375F),
		Climate.range(-0.375F, -0.2225F),
		Climate.range(-0.2225F, 0.05F),
		Climate.range(0.05F, 0.45F),
		Climate.range(0.45F, 0.55F),
		Climate.range(0.55F, 1.0F)
	};
	private final Climate.Parameter FROZEN_RANGE = this.temperatures[0];
	private final Climate.Parameter UNFROZEN_RANGE = Climate.range(this.temperatures[1], this.temperatures[4]);
	private final Climate.Parameter mushroomFieldsContinentalness = Climate.range(-1.2F, -1.05F);
	private final Climate.Parameter deepOceanContinentalness = Climate.range(-1.05F, -0.455F);
	private final Climate.Parameter oceanContinentalness = Climate.range(-0.455F, -0.19F);
	private final Climate.Parameter coastContinentalness = Climate.range(-0.19F, -0.11F);
	private final Climate.Parameter inlandContinentalness = Climate.range(-0.11F, 0.55F);
	private final Climate.Parameter nearInlandContinentalness = Climate.range(-0.11F, 0.03F);
	private final Climate.Parameter midInlandContinentalness = Climate.range(0.03F, 0.55F);
	private final Climate.Parameter farInlandContinentalness = Climate.range(0.55F, 1.0F);
	private final ResourceKey<Biome>[][] OCEANS = new ResourceKey[][]{
		{Biomes.DEEP_FROZEN_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_WARM_OCEAN},
		{Biomes.FROZEN_OCEAN, Biomes.COLD_OCEAN, Biomes.OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.WARM_OCEAN}
	};
	private final ResourceKey<Biome>[][] MIDDLE_BIOMES = new ResourceKey[][]{
		{Biomes.SNOWY_TUNDRA, Biomes.SNOWY_TUNDRA, Biomes.SNOWY_TUNDRA, Biomes.SNOWY_TAIGA, Biomes.GIANT_TREE_TAIGA},
		{Biomes.PLAINS, Biomes.PLAINS, Biomes.FOREST, Biomes.TAIGA, Biomes.TAIGA},
		{Biomes.PLAINS, Biomes.PLAINS, Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.DARK_FOREST},
		{Biomes.SAVANNA, Biomes.SAVANNA, Biomes.FOREST, Biomes.FOREST, Biomes.JUNGLE},
		{Biomes.DESERT, Biomes.DESERT, Biomes.DESERT, Biomes.JUNGLE, Biomes.JUNGLE}
	};
	private final ResourceKey<Biome>[][] MIDDLE_BIOMES_VARIANT = new ResourceKey[][]{
		{Biomes.ICE_SPIKES, null, null, Biomes.GIANT_SPRUCE_TAIGA, null},
		{null, null, null, null, null},
		{null, Biomes.SUNFLOWER_PLAINS, Biomes.FLOWER_FOREST, Biomes.TALL_BIRCH_FOREST, null},
		{null, null, Biomes.PLAINS, Biomes.PLAINS, null},
		{null, null, null, Biomes.JUNGLE_EDGE, Biomes.BAMBOO_JUNGLE}
	};
	private final ResourceKey<Biome>[][] PLATEAU_BIOMES = new ResourceKey[][]{
		{Biomes.SNOWY_TUNDRA, Biomes.SNOWY_TUNDRA, Biomes.SNOWY_TUNDRA, Biomes.SNOWY_TAIGA, Biomes.GIANT_TREE_TAIGA},
		{Biomes.SNOWY_TUNDRA, Biomes.SNOWY_TUNDRA, Biomes.SNOWY_TUNDRA, Biomes.SNOWY_TAIGA, Biomes.SNOWY_TAIGA},
		{Biomes.MEADOW, Biomes.MEADOW, Biomes.MEADOW, Biomes.MEADOW, Biomes.MEADOW},
		{Biomes.SAVANNA_PLATEAU, Biomes.SAVANNA_PLATEAU, Biomes.FOREST, Biomes.FOREST, Biomes.JUNGLE},
		{Biomes.BADLANDS, Biomes.BADLANDS, Biomes.BADLANDS, Biomes.WOODED_BADLANDS_PLATEAU, Biomes.WOODED_BADLANDS_PLATEAU}
	};
	private final ResourceKey<Biome>[][] PLATEAU_BIOMES_VARIANT = new ResourceKey[][]{
		{Biomes.ICE_SPIKES, null, null, Biomes.GIANT_SPRUCE_TAIGA, null},
		{null, null, null, null, null},
		{null, null, null, null, null},
		{null, null, null, null, null},
		{Biomes.ERODED_BADLANDS, Biomes.ERODED_BADLANDS, null, null, null}
	};
	private final ResourceKey<Biome>[][] EXTREME_HILLS = new ResourceKey[][]{
		{Biomes.GRAVELLY_MOUNTAINS, Biomes.GRAVELLY_MOUNTAINS, Biomes.MOUNTAINS, Biomes.WOODED_MOUNTAINS, Biomes.WOODED_MOUNTAINS},
		{Biomes.GRAVELLY_MOUNTAINS, Biomes.GRAVELLY_MOUNTAINS, Biomes.MOUNTAINS, Biomes.WOODED_MOUNTAINS, Biomes.WOODED_MOUNTAINS},
		{Biomes.MOUNTAINS, Biomes.MOUNTAINS, Biomes.MOUNTAINS, Biomes.WOODED_MOUNTAINS, Biomes.WOODED_MOUNTAINS},
		{null, null, null, null, null},
		{null, null, null, null, null}
	};

	protected void addBiomes(Builder<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> builder) {
		if (SharedConstants.DEBUG_GENERATE_SQUARE_TERRAIN_WITHOUT_NOISE) {
			this.addDebugBiomesToVisualizeSplinePoints(builder);
		} else {
			this.addOffCoastBiomes(builder);
			this.addInlandBiomes(builder);
			this.addUndergroundBiomes(builder);
		}
	}

	private void addOffCoastBiomes(Builder<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> builder) {
		this.addSurfaceBiome(
			builder, this.FULL_RANGE, this.FULL_RANGE, this.mushroomFieldsContinentalness, this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.MUSHROOM_FIELDS
		);

		for (int i = 0; i < this.temperatures.length; i++) {
			Climate.Parameter parameter = this.temperatures[i];
			this.addSurfaceBiome(builder, parameter, this.FULL_RANGE, this.deepOceanContinentalness, this.FULL_RANGE, this.FULL_RANGE, 0.0F, this.OCEANS[0][i]);
			this.addSurfaceBiome(builder, parameter, this.FULL_RANGE, this.oceanContinentalness, this.FULL_RANGE, this.FULL_RANGE, 0.0F, this.OCEANS[1][i]);
		}
	}

	private void addInlandBiomes(Builder<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> builder) {
		float f = 0.05F;
		float g = 0.1F;
		float h = 0.56666666F;
		float i = 0.7666667F;
		this.addMidSlice(builder, Climate.range(-1.0F, -0.93333334F));
		this.addHighSlice(builder, Climate.range(-0.93333334F, -0.7666667F));
		this.addPeaks(builder, Climate.range(-0.7666667F, -0.56666666F));
		this.addHighSlice(builder, Climate.range(-0.56666666F, -0.4F));
		this.addMidSlice(builder, Climate.range(-0.4F, -0.26666668F));
		this.addLowSlice(builder, Climate.range(-0.26666668F, -0.05F));
		this.addValleys(builder, Climate.range(-0.05F, 0.05F));
		this.addLowSlice(builder, Climate.range(0.05F, 0.26666668F));
		this.addMidSlice(builder, Climate.range(0.26666668F, 0.4F));
		this.addHighSlice(builder, Climate.range(0.4F, 0.56666666F));
		this.addPeaks(builder, Climate.range(0.56666666F, 0.7666667F));
		this.addHighSlice(builder, Climate.range(0.7666667F, 0.93333334F));
		this.addMidSlice(builder, Climate.range(0.93333334F, 1.0F));
	}

	private void addPeaks(Builder<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> builder, Climate.Parameter parameter) {
		for (int i = 0; i < this.temperatures.length; i++) {
			Climate.Parameter parameter2 = this.temperatures[i];

			for (int j = 0; j < this.humidities.length; j++) {
				Climate.Parameter parameter3 = this.humidities[j];
				ResourceKey<Biome> resourceKey = this.pickMiddleBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey2 = this.pickMiddleBiomeOrBadlandsIfHot(i, j, parameter);
				ResourceKey<Biome> resourceKey3 = this.pickPlateauBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey4 = this.pickExtremeHillsBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey5 = this.pickShatteredBiome(i, j, parameter, resourceKey4);
				ResourceKey<Biome> resourceKey6 = this.pickPeakBiome(i, j, parameter);
				this.addSurfaceBiome(builder, parameter2, parameter3, this.nearInlandContinentalness, this.erosions[0], parameter, 0.0F, resourceKey3);
				this.addSurfaceBiome(
					builder,
					parameter2,
					parameter3,
					Climate.range(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[0],
					parameter,
					0.0F,
					resourceKey6
				);
				this.addSurfaceBiome(
					builder, parameter2, parameter3, this.nearInlandContinentalness, Climate.range(this.erosions[1], this.erosions[2]), parameter, 0.0F, resourceKey
				);
				this.addSurfaceBiome(
					builder,
					parameter2,
					parameter3,
					Climate.range(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[1],
					parameter,
					0.0F,
					resourceKey3
				);
				this.addSurfaceBiome(builder, parameter2, parameter3, this.midInlandContinentalness, this.erosions[2], parameter, 0.0F, resourceKey2);
				this.addSurfaceBiome(builder, parameter2, parameter3, this.farInlandContinentalness, this.erosions[2], parameter, 0.0F, resourceKey3);
				this.addSurfaceBiome(
					builder, parameter2, parameter3, Climate.range(this.coastContinentalness, this.farInlandContinentalness), this.erosions[3], parameter, 0.0F, resourceKey
				);
				this.addSurfaceBiome(
					builder, parameter2, parameter3, Climate.range(this.coastContinentalness, this.nearInlandContinentalness), this.erosions[4], parameter, 0.0F, resourceKey5
				);
				this.addSurfaceBiome(
					builder,
					parameter2,
					parameter3,
					Climate.range(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[4],
					parameter,
					0.0F,
					resourceKey4
				);
				this.addSurfaceBiome(
					builder, parameter2, parameter3, Climate.range(this.coastContinentalness, this.farInlandContinentalness), this.erosions[5], parameter, 0.0F, resourceKey
				);
			}
		}
	}

	private void addHighSlice(Builder<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> builder, Climate.Parameter parameter) {
		for (int i = 0; i < this.temperatures.length; i++) {
			Climate.Parameter parameter2 = this.temperatures[i];

			for (int j = 0; j < this.humidities.length; j++) {
				Climate.Parameter parameter3 = this.humidities[j];
				ResourceKey<Biome> resourceKey = this.pickMiddleBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey2 = this.pickMiddleBiomeOrBadlandsIfHot(i, j, parameter);
				ResourceKey<Biome> resourceKey3 = this.pickPlateauBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey4 = this.pickExtremeHillsBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey5 = this.pickShatteredBiome(i, j, parameter, resourceKey);
				ResourceKey<Biome> resourceKey6 = this.pickSlopeBiome(i, j, parameter);
				this.addSurfaceBiome(builder, parameter2, parameter3, this.coastContinentalness, this.erosions[0], parameter, 0.0F, resourceKey);
				this.addSurfaceBiome(builder, parameter2, parameter3, this.nearInlandContinentalness, this.erosions[0], parameter, 0.0F, resourceKey2);
				this.addSurfaceBiome(
					builder,
					parameter2,
					parameter3,
					Climate.range(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[0],
					parameter,
					0.0F,
					resourceKey6
				);
				this.addSurfaceBiome(
					builder,
					parameter2,
					parameter3,
					Climate.range(this.coastContinentalness, this.nearInlandContinentalness),
					Climate.range(this.erosions[1], this.erosions[2]),
					parameter,
					0.0F,
					resourceKey
				);
				this.addSurfaceBiome(
					builder,
					parameter2,
					parameter3,
					Climate.range(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[1],
					parameter,
					0.0F,
					resourceKey3
				);
				this.addSurfaceBiome(builder, parameter2, parameter3, this.midInlandContinentalness, this.erosions[2], parameter, 0.0F, resourceKey2);
				this.addSurfaceBiome(builder, parameter2, parameter3, this.farInlandContinentalness, this.erosions[2], parameter, 0.0F, resourceKey3);
				this.addSurfaceBiome(
					builder, parameter2, parameter3, Climate.range(this.coastContinentalness, this.farInlandContinentalness), this.erosions[3], parameter, 0.0F, resourceKey
				);
				this.addSurfaceBiome(
					builder, parameter2, parameter3, Climate.range(this.coastContinentalness, this.nearInlandContinentalness), this.erosions[4], parameter, 0.0F, resourceKey5
				);
				this.addSurfaceBiome(
					builder,
					parameter2,
					parameter3,
					Climate.range(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[4],
					parameter,
					0.0F,
					resourceKey4
				);
				this.addSurfaceBiome(
					builder, parameter2, parameter3, Climate.range(this.coastContinentalness, this.farInlandContinentalness), this.erosions[5], parameter, 0.0F, resourceKey
				);
			}
		}
	}

	private void addMidSlice(Builder<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> builder, Climate.Parameter parameter) {
		this.addSurfaceBiome(
			builder, this.FULL_RANGE, this.FULL_RANGE, this.coastContinentalness, Climate.range(this.erosions[0], this.erosions[1]), parameter, 0.0F, Biomes.STONE_SHORE
		);
		this.addSurfaceBiome(
			builder,
			this.UNFROZEN_RANGE,
			Climate.range(this.humidities[1], this.humidities[4]),
			Climate.range(this.nearInlandContinentalness, this.farInlandContinentalness),
			this.erosions[5],
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
				ResourceKey<Biome> resourceKey3 = this.pickExtremeHillsBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey4 = this.pickPlateauBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey5 = this.pickBeachBiome(i, j);
				ResourceKey<Biome> resourceKey6 = this.pickShatteredBiome(i, j, parameter, resourceKey);
				ResourceKey<Biome> resourceKey7 = this.pickShatteredCoastBiome(i, j, parameter, resourceKey5);
				ResourceKey<Biome> resourceKey8 = this.pickSlopeBiome(i, j, parameter);
				this.addSurfaceBiome(
					builder,
					parameter2,
					parameter3,
					Climate.range(this.nearInlandContinentalness, this.midInlandContinentalness),
					this.erosions[0],
					parameter,
					0.0F,
					resourceKey2
				);
				this.addSurfaceBiome(builder, parameter2, parameter3, this.farInlandContinentalness, this.erosions[0], parameter, 0.0F, resourceKey8);
				this.addSurfaceBiome(builder, parameter2, parameter3, this.nearInlandContinentalness, this.erosions[1], parameter, 0.0F, resourceKey);
				this.addSurfaceBiome(builder, parameter2, parameter3, this.midInlandContinentalness, this.erosions[1], parameter, 0.0F, resourceKey2);
				this.addSurfaceBiome(builder, parameter2, parameter3, this.farInlandContinentalness, this.erosions[1], parameter, 0.0F, resourceKey4);
				this.addSurfaceBiome(
					builder, parameter2, parameter3, Climate.range(this.coastContinentalness, this.nearInlandContinentalness), this.erosions[2], parameter, 0.0F, resourceKey
				);
				this.addSurfaceBiome(
					builder,
					parameter2,
					parameter3,
					Climate.range(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[2],
					parameter,
					0.0F,
					resourceKey2
				);
				if (parameter.max() < 0.0F) {
					this.addSurfaceBiome(builder, parameter2, parameter3, this.coastContinentalness, this.erosions[3], parameter, 0.0F, resourceKey5);
					this.addSurfaceBiome(
						builder,
						parameter2,
						parameter3,
						Climate.range(this.nearInlandContinentalness, this.farInlandContinentalness),
						this.erosions[3],
						parameter,
						0.0F,
						resourceKey
					);
				} else {
					this.addSurfaceBiome(
						builder, parameter2, parameter3, Climate.range(this.coastContinentalness, this.farInlandContinentalness), this.erosions[3], parameter, 0.0F, resourceKey
					);
				}

				this.addSurfaceBiome(builder, parameter2, parameter3, this.coastContinentalness, this.erosions[4], parameter, 0.0F, resourceKey7);
				this.addSurfaceBiome(builder, parameter2, parameter3, this.nearInlandContinentalness, this.erosions[4], parameter, 0.0F, resourceKey6);
				this.addSurfaceBiome(
					builder,
					parameter2,
					parameter3,
					Climate.range(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[4],
					parameter,
					0.0F,
					resourceKey3
				);
				if (parameter.max() < 0.0F) {
					this.addSurfaceBiome(builder, parameter2, parameter3, this.coastContinentalness, this.erosions[5], parameter, 0.0F, resourceKey5);
				} else {
					this.addSurfaceBiome(builder, parameter2, parameter3, this.coastContinentalness, this.erosions[5], parameter, 0.0F, resourceKey);
				}

				if (i == 0 || j == 0) {
					this.addSurfaceBiome(
						builder,
						parameter2,
						parameter3,
						Climate.range(this.nearInlandContinentalness, this.farInlandContinentalness),
						this.erosions[5],
						parameter,
						0.0F,
						resourceKey
					);
				}
			}
		}
	}

	private void addLowSlice(Builder<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> builder, Climate.Parameter parameter) {
		this.addSurfaceBiome(
			builder, this.FULL_RANGE, this.FULL_RANGE, this.coastContinentalness, Climate.range(this.erosions[0], this.erosions[1]), parameter, 0.0F, Biomes.STONE_SHORE
		);
		this.addSurfaceBiome(
			builder,
			this.UNFROZEN_RANGE,
			Climate.range(this.humidities[1], this.humidities[4]),
			Climate.range(this.nearInlandContinentalness, this.farInlandContinentalness),
			this.erosions[5],
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
				ResourceKey<Biome> resourceKey3 = this.pickSlopeBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey4 = this.pickBeachBiome(i, j);
				ResourceKey<Biome> resourceKey5 = this.pickShatteredBiome(i, j, parameter, resourceKey);
				ResourceKey<Biome> resourceKey6 = this.pickShatteredCoastBiome(i, j, parameter, resourceKey4);
				this.addSurfaceBiome(
					builder,
					parameter2,
					parameter3,
					Climate.range(this.nearInlandContinentalness, this.midInlandContinentalness),
					this.erosions[0],
					parameter,
					0.0F,
					resourceKey2
				);
				this.addSurfaceBiome(builder, parameter2, parameter3, this.farInlandContinentalness, this.erosions[0], parameter, 0.0F, resourceKey3);
				this.addSurfaceBiome(
					builder, parameter2, parameter3, this.nearInlandContinentalness, Climate.range(this.erosions[1], this.erosions[2]), parameter, 0.0F, resourceKey
				);
				this.addSurfaceBiome(
					builder,
					parameter2,
					parameter3,
					Climate.range(this.midInlandContinentalness, this.farInlandContinentalness),
					Climate.range(this.erosions[1], this.erosions[2]),
					parameter,
					0.0F,
					resourceKey2
				);
				this.addSurfaceBiome(
					builder, parameter2, parameter3, this.coastContinentalness, Climate.range(this.erosions[2], this.erosions[3]), parameter, 0.0F, resourceKey4
				);
				this.addSurfaceBiome(
					builder,
					parameter2,
					parameter3,
					Climate.range(this.nearInlandContinentalness, this.farInlandContinentalness),
					this.erosions[3],
					parameter,
					0.0F,
					resourceKey
				);
				this.addSurfaceBiome(builder, parameter2, parameter3, this.coastContinentalness, this.erosions[4], parameter, 0.0F, resourceKey6);
				this.addSurfaceBiome(builder, parameter2, parameter3, this.nearInlandContinentalness, this.erosions[4], parameter, 0.0F, resourceKey5);
				this.addSurfaceBiome(
					builder,
					parameter2,
					parameter3,
					Climate.range(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[4],
					parameter,
					0.0F,
					resourceKey
				);
				this.addSurfaceBiome(builder, parameter2, parameter3, this.coastContinentalness, this.erosions[5], parameter, 0.0F, resourceKey4);
				if (i == 0 || j == 0) {
					this.addSurfaceBiome(
						builder,
						parameter2,
						parameter3,
						Climate.range(this.nearInlandContinentalness, this.farInlandContinentalness),
						this.erosions[5],
						parameter,
						0.0F,
						resourceKey
					);
				}
			}
		}
	}

	private void addValleys(Builder<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> builder, Climate.Parameter parameter) {
		this.addSurfaceBiome(
			builder,
			this.FROZEN_RANGE,
			this.FULL_RANGE,
			this.coastContinentalness,
			this.erosions[0],
			parameter,
			0.0F,
			parameter.max() < 0.0F ? Biomes.STONE_SHORE : Biomes.FROZEN_RIVER
		);
		this.addSurfaceBiome(
			builder,
			this.UNFROZEN_RANGE,
			this.FULL_RANGE,
			this.coastContinentalness,
			this.erosions[0],
			parameter,
			0.0F,
			parameter.max() < 0.0F ? Biomes.STONE_SHORE : Biomes.RIVER
		);
		this.addSurfaceBiome(builder, this.FROZEN_RANGE, this.FULL_RANGE, this.nearInlandContinentalness, this.erosions[0], parameter, 0.0F, Biomes.FROZEN_RIVER);
		this.addSurfaceBiome(builder, this.UNFROZEN_RANGE, this.FULL_RANGE, this.nearInlandContinentalness, this.erosions[0], parameter, 0.0F, Biomes.RIVER);
		this.addSurfaceBiome(
			builder,
			this.FROZEN_RANGE,
			this.FULL_RANGE,
			Climate.range(this.coastContinentalness, this.farInlandContinentalness),
			Climate.range(this.erosions[1], this.erosions[4]),
			parameter,
			0.0F,
			Biomes.FROZEN_RIVER
		);
		this.addSurfaceBiome(
			builder,
			this.UNFROZEN_RANGE,
			this.FULL_RANGE,
			Climate.range(this.coastContinentalness, this.farInlandContinentalness),
			Climate.range(this.erosions[1], this.erosions[4]),
			parameter,
			0.0F,
			Biomes.RIVER
		);
		this.addSurfaceBiome(builder, this.FROZEN_RANGE, this.FULL_RANGE, this.coastContinentalness, this.erosions[5], parameter, 0.0F, Biomes.FROZEN_RIVER);
		this.addSurfaceBiome(builder, this.UNFROZEN_RANGE, this.FULL_RANGE, this.coastContinentalness, this.erosions[5], parameter, 0.0F, Biomes.RIVER);
		this.addSurfaceBiome(
			builder,
			this.UNFROZEN_RANGE,
			Climate.range(this.humidities[1], this.humidities[4]),
			Climate.range(this.inlandContinentalness, this.farInlandContinentalness),
			this.erosions[5],
			parameter,
			0.0F,
			Biomes.SWAMP
		);
		this.addSurfaceBiome(
			builder,
			this.UNFROZEN_RANGE,
			this.humidities[0],
			Climate.range(this.inlandContinentalness, this.farInlandContinentalness),
			this.erosions[5],
			parameter,
			0.0F,
			Biomes.RIVER
		);
		this.addSurfaceBiome(
			builder,
			this.FROZEN_RANGE,
			this.FULL_RANGE,
			Climate.range(this.inlandContinentalness, this.farInlandContinentalness),
			this.erosions[5],
			parameter,
			0.0F,
			Biomes.FROZEN_RIVER
		);

		for (int i = 0; i < this.temperatures.length; i++) {
			Climate.Parameter parameter2 = this.temperatures[i];

			for (int j = 0; j < this.humidities.length; j++) {
				Climate.Parameter parameter3 = this.humidities[j];
				ResourceKey<Biome> resourceKey = this.pickMiddleBiome(i, j, parameter);
				ResourceKey<Biome> resourceKey2 = this.pickBadlandsBiome(j, parameter);
				ResourceKey<Biome> resourceKey3 = i == 4 ? resourceKey2 : resourceKey;
				this.addSurfaceBiome(
					builder,
					parameter2,
					parameter3,
					Climate.range(this.midInlandContinentalness, this.farInlandContinentalness),
					this.erosions[0],
					parameter,
					0.0F,
					resourceKey3
				);
			}
		}
	}

	private void addUndergroundBiomes(Builder<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> builder) {
		this.addUndergroundBiome(builder, this.FULL_RANGE, this.FULL_RANGE, Climate.range(0.8F, 1.0F), this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.DRIPSTONE_CAVES);
		this.addUndergroundBiome(builder, this.FULL_RANGE, Climate.range(0.7F, 1.0F), this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.LUSH_CAVES);
	}

	private ResourceKey<Biome> pickMiddleBiome(int i, int j, Climate.Parameter parameter) {
		if (parameter.max() < 0.0F) {
			return this.MIDDLE_BIOMES[i][j];
		} else {
			ResourceKey<Biome> resourceKey = this.MIDDLE_BIOMES_VARIANT[i][j];
			return resourceKey == null ? this.MIDDLE_BIOMES[i][j] : resourceKey;
		}
	}

	private ResourceKey<Biome> pickMiddleBiomeOrBadlandsIfHot(int i, int j, Climate.Parameter parameter) {
		return i == 4 ? this.pickBadlandsBiome(j, parameter) : this.pickMiddleBiome(i, j, parameter);
	}

	private ResourceKey<Biome> pickShatteredBiome(int i, int j, Climate.Parameter parameter, ResourceKey<Biome> resourceKey) {
		return i > 1 && j < 4 && parameter.max() >= 0.0F ? Biomes.SHATTERED_SAVANNA : resourceKey;
	}

	private ResourceKey<Biome> pickShatteredCoastBiome(int i, int j, Climate.Parameter parameter, ResourceKey<Biome> resourceKey) {
		if (i > 1 && j < 4 && parameter.max() >= 0.0F) {
			return Biomes.SHATTERED_SAVANNA;
		} else {
			return parameter.max() >= 0.0F ? this.pickMiddleBiome(i, j, parameter) : this.pickBeachBiome(i, j);
		}
	}

	private ResourceKey<Biome> pickBeachBiome(int i, int j) {
		if (i == 0) {
			return Biomes.SNOWY_BEACH;
		} else {
			return i == 4 && j < 3 ? Biomes.DESERT : Biomes.BEACH;
		}
	}

	private ResourceKey<Biome> pickBadlandsBiome(int i, Climate.Parameter parameter) {
		if (i < 2) {
			return parameter.max() < 0.0F ? Biomes.ERODED_BADLANDS : Biomes.BADLANDS;
		} else {
			return i < 3 ? Biomes.BADLANDS : Biomes.WOODED_BADLANDS_PLATEAU;
		}
	}

	private ResourceKey<Biome> pickPlateauBiome(int i, int j, Climate.Parameter parameter) {
		if (parameter.max() < 0.0F) {
			return this.PLATEAU_BIOMES[i][j];
		} else {
			ResourceKey<Biome> resourceKey = this.PLATEAU_BIOMES_VARIANT[i][j];
			return resourceKey == null ? this.PLATEAU_BIOMES[i][j] : resourceKey;
		}
	}

	private ResourceKey<Biome> pickPeakBiome(int i, int j, Climate.Parameter parameter) {
		if (i <= 2) {
			return parameter.max() < 0.0F ? Biomes.LOFTY_PEAKS : Biomes.SNOWCAPPED_PEAKS;
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
		Builder<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> builder,
		Climate.Parameter parameter,
		Climate.Parameter parameter2,
		Climate.Parameter parameter3,
		Climate.Parameter parameter4,
		Climate.Parameter parameter5,
		float f,
		ResourceKey<Biome> resourceKey
	) {
		builder.add(Pair.of(Climate.parameters(parameter, parameter2, parameter3, parameter4, Climate.point(0.1F), parameter5, f), resourceKey));
		builder.add(Pair.of(Climate.parameters(parameter, parameter2, parameter3, parameter4, Climate.point(1.0F), parameter5, f), resourceKey));
	}

	private void addUndergroundBiome(
		Builder<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> builder,
		Climate.Parameter parameter,
		Climate.Parameter parameter2,
		Climate.Parameter parameter3,
		Climate.Parameter parameter4,
		Climate.Parameter parameter5,
		float f,
		ResourceKey<Biome> resourceKey
	) {
		builder.add(Pair.of(Climate.parameters(parameter, parameter2, parameter3, parameter4, Climate.range(0.2F, 0.9F), parameter5, f), resourceKey));
	}

	private void addDebugBiomesToVisualizeSplinePoints(Builder<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> builder) {
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, 0.01F, Biomes.PLAINS);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, Climate.point(-0.9F), this.FULL_RANGE, 0.0F, Biomes.DESERT);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, Climate.point(-0.4F), this.FULL_RANGE, 0.0F, Biomes.BADLANDS);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, Climate.point(-0.35F), this.FULL_RANGE, 0.0F, Biomes.DESERT);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, Climate.point(-0.1F), this.FULL_RANGE, 0.0F, Biomes.BADLANDS);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, Climate.point(0.2F), this.FULL_RANGE, 0.0F, Biomes.DESERT);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, Climate.point(1.0F), this.FULL_RANGE, 0.0F, Biomes.BADLANDS);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, Climate.point(-1.1F), this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.SNOWY_TAIGA);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, Climate.point(-1.005F), this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.SNOWY_TAIGA);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, Climate.point(-0.51F), this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.SNOWY_TAIGA);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, Climate.point(-0.44F), this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.SNOWY_TAIGA);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, Climate.point(-0.18F), this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.SNOWY_TAIGA);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, Climate.point(-0.16F), this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.SNOWY_TAIGA);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, Climate.point(-0.15F), this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.SNOWY_TAIGA);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, Climate.point(-0.1F), this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.SNOWY_TAIGA);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, Climate.point(0.25F), this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.SNOWY_TAIGA);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, Climate.point(1.0F), this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.SNOWY_TAIGA);
	}

	private void addDebugBiomesToShowCoastalAreas(Builder<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> builder) {
		float f = -0.2F;
		float g = -0.05F;
		float h = -0.15F;
		float i = 0.15F;
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, Climate.range(-0.2F, -0.05F), this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.BADLANDS);
		this.addSurfaceBiome(
			builder, this.FULL_RANGE, this.FULL_RANGE, Climate.range(-0.05F, 1.0F), this.FULL_RANGE, Climate.range(-0.15F, 0.15F), 0.0F, Biomes.DESERT
		);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, Climate.range(-1.0F, -0.2F), this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.OCEAN);
		this.addSurfaceBiome(
			builder, this.FULL_RANGE, this.FULL_RANGE, Climate.range(-0.05F, 1.0F), this.FULL_RANGE, Climate.range(-1.0F, -0.15F), 0.0F, Biomes.PLAINS
		);
		this.addSurfaceBiome(builder, this.FULL_RANGE, this.FULL_RANGE, Climate.range(-0.05F, 1.0F), this.FULL_RANGE, Climate.range(0.15F, 1.0F), 0.0F, Biomes.PLAINS);
	}

	public static String getDebugStringForPeaksAndValleys(double d) {
		if (d < -0.85) {
			return "Valley";
		} else if (d < -0.19) {
			return "Low";
		} else if (d < 0.21) {
			return "Mid";
		} else {
			return d < 0.81 ? "High" : "Peak";
		}
	}

	public String getDebugStringForContinentalness(double d) {
		if (d < (double)this.mushroomFieldsContinentalness.max()) {
			return "Mushroom fields";
		} else if (d < (double)this.deepOceanContinentalness.max()) {
			return "Deep ocean";
		} else if (d < (double)this.oceanContinentalness.max()) {
			return "Ocean";
		} else if (d < (double)this.coastContinentalness.max()) {
			return "Coast";
		} else if (d < (double)this.nearInlandContinentalness.max()) {
			return "Near inland";
		} else {
			return d < (double)this.midInlandContinentalness.max() ? "Mid inland" : "Far inland";
		}
	}

	public String getDebugStringForErosion(double d) {
		return this.getDebugStringForNoiseValue(d, this.erosions);
	}

	public String getDebugStringForTemperature(double d) {
		return this.getDebugStringForNoiseValue(d, this.temperatures);
	}

	public String getDebugStringForHumidity(double d) {
		return this.getDebugStringForNoiseValue(d, this.humidities);
	}

	private String getDebugStringForNoiseValue(double d, Climate.Parameter[] parameters) {
		for (int i = 0; i < parameters.length; i++) {
			if (d < (double)parameters[i].max()) {
				return i + "";
			}
		}

		return "?";
	}
}
