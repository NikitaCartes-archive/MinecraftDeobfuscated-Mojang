package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Biome {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final Codec<Biome> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Biome.ClimateSettings.CODEC.forGetter(biome -> biome.climateSettings),
					Biome.BiomeCategory.CODEC.fieldOf("category").forGetter(biome -> biome.biomeCategory),
					Codec.FLOAT.fieldOf("depth").forGetter(biome -> biome.depth),
					Codec.FLOAT.fieldOf("scale").forGetter(biome -> biome.scale),
					BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter(biome -> biome.specialEffects),
					BiomeGenerationSettings.CODEC.forGetter(biome -> biome.generationSettings),
					MobSpawnSettings.CODEC.forGetter(biome -> biome.mobSettings)
				)
				.apply(instance, Biome::new)
	);
	public static final Codec<Biome> NETWORK_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Biome.ClimateSettings.CODEC.forGetter(biome -> biome.climateSettings),
					Biome.BiomeCategory.CODEC.fieldOf("category").forGetter(biome -> biome.biomeCategory),
					Codec.FLOAT.fieldOf("depth").forGetter(biome -> biome.depth),
					Codec.FLOAT.fieldOf("scale").forGetter(biome -> biome.scale),
					BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter(biome -> biome.specialEffects)
				)
				.apply(
					instance,
					(climateSettings, biomeCategory, float_, float2, biomeSpecialEffects) -> new Biome(
							climateSettings, biomeCategory, float_, float2, biomeSpecialEffects, BiomeGenerationSettings.EMPTY, MobSpawnSettings.EMPTY
						)
				)
	);
	public static final Codec<Supplier<Biome>> CODEC = RegistryFileCodec.create(Registry.BIOME_REGISTRY, DIRECT_CODEC);
	public static final Codec<List<Supplier<Biome>>> LIST_CODEC = RegistryFileCodec.homogeneousList(Registry.BIOME_REGISTRY, DIRECT_CODEC);
	private final Map<Integer, List<StructureFeature<?>>> structuresByStep = (Map<Integer, List<StructureFeature<?>>>)Registry.STRUCTURE_FEATURE
		.stream()
		.collect(Collectors.groupingBy(structureFeature -> structureFeature.step().ordinal()));
	private static final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(1234L), ImmutableList.of(0));
	private static final PerlinSimplexNoise FROZEN_TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(3456L), ImmutableList.of(-2, -1, 0));
	public static final PerlinSimplexNoise BIOME_INFO_NOISE = new PerlinSimplexNoise(new WorldgenRandom(2345L), ImmutableList.of(0));
	private final Biome.ClimateSettings climateSettings;
	private final BiomeGenerationSettings generationSettings;
	private final MobSpawnSettings mobSettings;
	private final float depth;
	private final float scale;
	private final Biome.BiomeCategory biomeCategory;
	private final BiomeSpecialEffects specialEffects;
	private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache = ThreadLocal.withInitial(() -> Util.make(() -> {
			Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(1024, 0.25F) {
				@Override
				protected void rehash(int i) {
				}
			};
			long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
			return long2FloatLinkedOpenHashMap;
		}));

	private Biome(
		Biome.ClimateSettings climateSettings,
		Biome.BiomeCategory biomeCategory,
		float f,
		float g,
		BiomeSpecialEffects biomeSpecialEffects,
		BiomeGenerationSettings biomeGenerationSettings,
		MobSpawnSettings mobSpawnSettings
	) {
		this.climateSettings = climateSettings;
		this.generationSettings = biomeGenerationSettings;
		this.mobSettings = mobSpawnSettings;
		this.biomeCategory = biomeCategory;
		this.depth = f;
		this.scale = g;
		this.specialEffects = biomeSpecialEffects;
	}

	@Environment(EnvType.CLIENT)
	public int getSkyColor() {
		return this.specialEffects.getSkyColor();
	}

	public MobSpawnSettings getMobSettings() {
		return this.mobSettings;
	}

	public Biome.Precipitation getPrecipitation() {
		return this.climateSettings.precipitation;
	}

	public boolean isHumid() {
		return this.getDownfall() > 0.85F;
	}

	private float getHeightAdjustedTemperature(BlockPos blockPos) {
		float f = this.climateSettings.temperatureModifier.modifyTemperature(blockPos, this.getBaseTemperature());
		if (blockPos.getY() > 64) {
			float g = (float)(TEMPERATURE_NOISE.getValue((double)((float)blockPos.getX() / 8.0F), (double)((float)blockPos.getZ() / 8.0F), false) * 4.0);
			return f - (g + (float)blockPos.getY() - 64.0F) * 0.05F / 30.0F;
		} else {
			return f;
		}
	}

	public final float getTemperature(BlockPos blockPos) {
		long l = blockPos.asLong();
		Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = (Long2FloatLinkedOpenHashMap)this.temperatureCache.get();
		float f = long2FloatLinkedOpenHashMap.get(l);
		if (!Float.isNaN(f)) {
			return f;
		} else {
			float g = this.getHeightAdjustedTemperature(blockPos);
			if (long2FloatLinkedOpenHashMap.size() == 1024) {
				long2FloatLinkedOpenHashMap.removeFirstFloat();
			}

			long2FloatLinkedOpenHashMap.put(l, g);
			return g;
		}
	}

	public boolean shouldFreeze(LevelReader levelReader, BlockPos blockPos) {
		return this.shouldFreeze(levelReader, blockPos, true);
	}

	public boolean shouldFreeze(LevelReader levelReader, BlockPos blockPos, boolean bl) {
		if (this.getTemperature(blockPos) >= 0.15F) {
			return false;
		} else {
			if (blockPos.getY() >= levelReader.getMinBuildHeight()
				&& blockPos.getY() < levelReader.getMaxBuildHeight()
				&& levelReader.getBrightness(LightLayer.BLOCK, blockPos) < 10) {
				BlockState blockState = levelReader.getBlockState(blockPos);
				FluidState fluidState = levelReader.getFluidState(blockPos);
				if (fluidState.getType() == Fluids.WATER && blockState.getBlock() instanceof LiquidBlock) {
					if (!bl) {
						return true;
					}

					boolean bl2 = levelReader.isWaterAt(blockPos.west())
						&& levelReader.isWaterAt(blockPos.east())
						&& levelReader.isWaterAt(blockPos.north())
						&& levelReader.isWaterAt(blockPos.south());
					if (!bl2) {
						return true;
					}
				}
			}

			return false;
		}
	}

	public boolean shouldSnow(LevelReader levelReader, BlockPos blockPos) {
		if (this.getTemperature(blockPos) >= 0.15F) {
			return false;
		} else {
			if (blockPos.getY() >= levelReader.getMinBuildHeight()
				&& blockPos.getY() < levelReader.getMaxBuildHeight()
				&& levelReader.getBrightness(LightLayer.BLOCK, blockPos) < 10) {
				BlockState blockState = levelReader.getBlockState(blockPos);
				if (blockState.isAir() && Blocks.SNOW.defaultBlockState().canSurvive(levelReader, blockPos)) {
					return true;
				}
			}

			return false;
		}
	}

	public BiomeGenerationSettings getGenerationSettings() {
		return this.generationSettings;
	}

	public void generate(
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		WorldGenRegion worldGenRegion,
		long l,
		WorldgenRandom worldgenRandom,
		BlockPos blockPos
	) {
		List<List<Supplier<ConfiguredFeature<?, ?>>>> list = this.generationSettings.features();
		int i = GenerationStep.Decoration.values().length;

		for (int j = 0; j < i; j++) {
			int k = 0;
			if (structureFeatureManager.shouldGenerateFeatures()) {
				for (StructureFeature<?> structureFeature : (List)this.structuresByStep.getOrDefault(j, Collections.emptyList())) {
					worldgenRandom.setFeatureSeed(l, k, j);
					int m = SectionPos.blockToSectionCoord(blockPos.getX());
					int n = SectionPos.blockToSectionCoord(blockPos.getZ());
					int o = SectionPos.sectionToBlockCoord(m);
					int p = SectionPos.sectionToBlockCoord(n);

					try {
						structureFeatureManager.startsForFeature(SectionPos.of(blockPos), structureFeature)
							.forEach(
								structureStart -> structureStart.placeInChunk(
										worldGenRegion,
										structureFeatureManager,
										chunkGenerator,
										worldgenRandom,
										new BoundingBox(o, worldGenRegion.getMinBuildHeight() + 1, p, o + 15, worldGenRegion.getMaxBuildHeight(), p + 15),
										new ChunkPos(m, n)
									)
							);
					} catch (Exception var21) {
						CrashReport crashReport = CrashReport.forThrowable(var21, "Feature placement");
						crashReport.addCategory("Feature")
							.setDetail("Id", Registry.STRUCTURE_FEATURE.getKey(structureFeature))
							.setDetail("Description", (CrashReportDetail<String>)(() -> structureFeature.toString()));
						throw new ReportedException(crashReport);
					}

					k++;
				}
			}

			if (list.size() > j) {
				for (Supplier<ConfiguredFeature<?, ?>> supplier : (List)list.get(j)) {
					ConfiguredFeature<?, ?> configuredFeature = (ConfiguredFeature<?, ?>)supplier.get();
					worldgenRandom.setFeatureSeed(l, k, j);

					try {
						configuredFeature.place(worldGenRegion, chunkGenerator, worldgenRandom, blockPos);
					} catch (Exception var22) {
						CrashReport crashReport2 = CrashReport.forThrowable(var22, "Feature placement");
						crashReport2.addCategory("Feature")
							.setDetail("Id", Registry.FEATURE.getKey(configuredFeature.feature))
							.setDetail("Config", configuredFeature.config)
							.setDetail("Description", (CrashReportDetail<String>)(() -> configuredFeature.feature.toString()));
						throw new ReportedException(crashReport2);
					}

					k++;
				}
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public int getFogColor() {
		return this.specialEffects.getFogColor();
	}

	@Environment(EnvType.CLIENT)
	public int getGrassColor(double d, double e) {
		int i = (Integer)this.specialEffects.getGrassColorOverride().orElseGet(this::getGrassColorFromTexture);
		return this.specialEffects.getGrassColorModifier().modifyColor(d, e, i);
	}

	@Environment(EnvType.CLIENT)
	private int getGrassColorFromTexture() {
		double d = (double)Mth.clamp(this.climateSettings.temperature, 0.0F, 1.0F);
		double e = (double)Mth.clamp(this.climateSettings.downfall, 0.0F, 1.0F);
		return GrassColor.get(d, e);
	}

	@Environment(EnvType.CLIENT)
	public int getFoliageColor() {
		return (Integer)this.specialEffects.getFoliageColorOverride().orElseGet(this::getFoliageColorFromTexture);
	}

	@Environment(EnvType.CLIENT)
	private int getFoliageColorFromTexture() {
		double d = (double)Mth.clamp(this.climateSettings.temperature, 0.0F, 1.0F);
		double e = (double)Mth.clamp(this.climateSettings.downfall, 0.0F, 1.0F);
		return FoliageColor.get(d, e);
	}

	public void buildSurfaceAt(Random random, ChunkAccess chunkAccess, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, long m) {
		ConfiguredSurfaceBuilder<?> configuredSurfaceBuilder = (ConfiguredSurfaceBuilder<?>)this.generationSettings.getSurfaceBuilder().get();
		configuredSurfaceBuilder.initNoise(m);
		configuredSurfaceBuilder.apply(random, chunkAccess, this, i, j, k, d, blockState, blockState2, l, m);
	}

	public final float getDepth() {
		return this.depth;
	}

	public final float getDownfall() {
		return this.climateSettings.downfall;
	}

	public final float getScale() {
		return this.scale;
	}

	public final float getBaseTemperature() {
		return this.climateSettings.temperature;
	}

	public BiomeSpecialEffects getSpecialEffects() {
		return this.specialEffects;
	}

	@Environment(EnvType.CLIENT)
	public final int getWaterColor() {
		return this.specialEffects.getWaterColor();
	}

	@Environment(EnvType.CLIENT)
	public final int getWaterFogColor() {
		return this.specialEffects.getWaterFogColor();
	}

	@Environment(EnvType.CLIENT)
	public Optional<AmbientParticleSettings> getAmbientParticle() {
		return this.specialEffects.getAmbientParticleSettings();
	}

	@Environment(EnvType.CLIENT)
	public Optional<SoundEvent> getAmbientLoop() {
		return this.specialEffects.getAmbientLoopSoundEvent();
	}

	@Environment(EnvType.CLIENT)
	public Optional<AmbientMoodSettings> getAmbientMood() {
		return this.specialEffects.getAmbientMoodSettings();
	}

	@Environment(EnvType.CLIENT)
	public Optional<AmbientAdditionsSettings> getAmbientAdditions() {
		return this.specialEffects.getAmbientAdditionsSettings();
	}

	@Environment(EnvType.CLIENT)
	public Optional<Music> getBackgroundMusic() {
		return this.specialEffects.getBackgroundMusic();
	}

	public final Biome.BiomeCategory getBiomeCategory() {
		return this.biomeCategory;
	}

	public String toString() {
		ResourceLocation resourceLocation = BuiltinRegistries.BIOME.getKey(this);
		return resourceLocation == null ? super.toString() : resourceLocation.toString();
	}

	public static class BiomeBuilder {
		@Nullable
		private Biome.Precipitation precipitation;
		@Nullable
		private Biome.BiomeCategory biomeCategory;
		@Nullable
		private Float depth;
		@Nullable
		private Float scale;
		@Nullable
		private Float temperature;
		private Biome.TemperatureModifier temperatureModifier = Biome.TemperatureModifier.NONE;
		@Nullable
		private Float downfall;
		@Nullable
		private BiomeSpecialEffects specialEffects;
		@Nullable
		private MobSpawnSettings mobSpawnSettings;
		@Nullable
		private BiomeGenerationSettings generationSettings;

		public Biome.BiomeBuilder precipitation(Biome.Precipitation precipitation) {
			this.precipitation = precipitation;
			return this;
		}

		public Biome.BiomeBuilder biomeCategory(Biome.BiomeCategory biomeCategory) {
			this.biomeCategory = biomeCategory;
			return this;
		}

		public Biome.BiomeBuilder depth(float f) {
			this.depth = f;
			return this;
		}

		public Biome.BiomeBuilder scale(float f) {
			this.scale = f;
			return this;
		}

		public Biome.BiomeBuilder temperature(float f) {
			this.temperature = f;
			return this;
		}

		public Biome.BiomeBuilder downfall(float f) {
			this.downfall = f;
			return this;
		}

		public Biome.BiomeBuilder specialEffects(BiomeSpecialEffects biomeSpecialEffects) {
			this.specialEffects = biomeSpecialEffects;
			return this;
		}

		public Biome.BiomeBuilder mobSpawnSettings(MobSpawnSettings mobSpawnSettings) {
			this.mobSpawnSettings = mobSpawnSettings;
			return this;
		}

		public Biome.BiomeBuilder generationSettings(BiomeGenerationSettings biomeGenerationSettings) {
			this.generationSettings = biomeGenerationSettings;
			return this;
		}

		public Biome.BiomeBuilder temperatureAdjustment(Biome.TemperatureModifier temperatureModifier) {
			this.temperatureModifier = temperatureModifier;
			return this;
		}

		public Biome build() {
			if (this.precipitation != null
				&& this.biomeCategory != null
				&& this.depth != null
				&& this.scale != null
				&& this.temperature != null
				&& this.downfall != null
				&& this.specialEffects != null
				&& this.mobSpawnSettings != null
				&& this.generationSettings != null) {
				return new Biome(
					new Biome.ClimateSettings(this.precipitation, this.temperature, this.temperatureModifier, this.downfall),
					this.biomeCategory,
					this.depth,
					this.scale,
					this.specialEffects,
					this.generationSettings,
					this.mobSpawnSettings
				);
			} else {
				throw new IllegalStateException("You are missing parameters to build a proper biome\n" + this);
			}
		}

		public String toString() {
			return "BiomeBuilder{\nprecipitation="
				+ this.precipitation
				+ ",\nbiomeCategory="
				+ this.biomeCategory
				+ ",\ndepth="
				+ this.depth
				+ ",\nscale="
				+ this.scale
				+ ",\ntemperature="
				+ this.temperature
				+ ",\ntemperatureModifier="
				+ this.temperatureModifier
				+ ",\ndownfall="
				+ this.downfall
				+ ",\nspecialEffects="
				+ this.specialEffects
				+ ",\nmobSpawnSettings="
				+ this.mobSpawnSettings
				+ ",\ngenerationSettings="
				+ this.generationSettings
				+ ",\n"
				+ '}';
		}
	}

	public static enum BiomeCategory implements StringRepresentable {
		NONE("none"),
		TAIGA("taiga"),
		EXTREME_HILLS("extreme_hills"),
		JUNGLE("jungle"),
		MESA("mesa"),
		PLAINS("plains"),
		SAVANNA("savanna"),
		ICY("icy"),
		THEEND("the_end"),
		BEACH("beach"),
		FOREST("forest"),
		OCEAN("ocean"),
		DESERT("desert"),
		RIVER("river"),
		SWAMP("swamp"),
		MUSHROOM("mushroom"),
		NETHER("nether");

		public static final Codec<Biome.BiomeCategory> CODEC = StringRepresentable.fromEnum(Biome.BiomeCategory::values, Biome.BiomeCategory::byName);
		private static final Map<String, Biome.BiomeCategory> BY_NAME = (Map<String, Biome.BiomeCategory>)Arrays.stream(values())
			.collect(Collectors.toMap(Biome.BiomeCategory::getName, biomeCategory -> biomeCategory));
		private final String name;

		private BiomeCategory(String string2) {
			this.name = string2;
		}

		public String getName() {
			return this.name;
		}

		public static Biome.BiomeCategory byName(String string) {
			return (Biome.BiomeCategory)BY_NAME.get(string);
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}

	public static class ClimateParameters {
		public static final Codec<Biome.ClimateParameters> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.floatRange(-2.0F, 2.0F).fieldOf("temperature").forGetter(climateParameters -> climateParameters.temperature),
						Codec.floatRange(-2.0F, 2.0F).fieldOf("humidity").forGetter(climateParameters -> climateParameters.humidity),
						Codec.floatRange(-2.0F, 2.0F).fieldOf("altitude").forGetter(climateParameters -> climateParameters.altitude),
						Codec.floatRange(-2.0F, 2.0F).fieldOf("weirdness").forGetter(climateParameters -> climateParameters.weirdness),
						Codec.floatRange(0.0F, 1.0F).fieldOf("offset").forGetter(climateParameters -> climateParameters.offset)
					)
					.apply(instance, Biome.ClimateParameters::new)
		);
		private final float temperature;
		private final float humidity;
		private final float altitude;
		private final float weirdness;
		private final float offset;

		public ClimateParameters(float f, float g, float h, float i, float j) {
			this.temperature = f;
			this.humidity = g;
			this.altitude = h;
			this.weirdness = i;
			this.offset = j;
		}

		public String toString() {
			return "temp: " + this.temperature + ", hum: " + this.humidity + ", alt: " + this.altitude + ", weird: " + this.weirdness + ", offset: " + this.offset;
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				Biome.ClimateParameters climateParameters = (Biome.ClimateParameters)object;
				if (Float.compare(climateParameters.temperature, this.temperature) != 0) {
					return false;
				} else if (Float.compare(climateParameters.humidity, this.humidity) != 0) {
					return false;
				} else {
					return Float.compare(climateParameters.altitude, this.altitude) != 0 ? false : Float.compare(climateParameters.weirdness, this.weirdness) == 0;
				}
			} else {
				return false;
			}
		}

		public int hashCode() {
			int i = this.temperature != 0.0F ? Float.floatToIntBits(this.temperature) : 0;
			i = 31 * i + (this.humidity != 0.0F ? Float.floatToIntBits(this.humidity) : 0);
			i = 31 * i + (this.altitude != 0.0F ? Float.floatToIntBits(this.altitude) : 0);
			return 31 * i + (this.weirdness != 0.0F ? Float.floatToIntBits(this.weirdness) : 0);
		}

		public float fitness(Biome.ClimateParameters climateParameters) {
			return (this.temperature - climateParameters.temperature) * (this.temperature - climateParameters.temperature)
				+ (this.humidity - climateParameters.humidity) * (this.humidity - climateParameters.humidity)
				+ (this.altitude - climateParameters.altitude) * (this.altitude - climateParameters.altitude)
				+ (this.weirdness - climateParameters.weirdness) * (this.weirdness - climateParameters.weirdness)
				+ (this.offset - climateParameters.offset) * (this.offset - climateParameters.offset);
		}
	}

	static class ClimateSettings {
		public static final MapCodec<Biome.ClimateSettings> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						Biome.Precipitation.CODEC.fieldOf("precipitation").forGetter(climateSettings -> climateSettings.precipitation),
						Codec.FLOAT.fieldOf("temperature").forGetter(climateSettings -> climateSettings.temperature),
						Biome.TemperatureModifier.CODEC
							.optionalFieldOf("temperature_modifier", Biome.TemperatureModifier.NONE)
							.forGetter(climateSettings -> climateSettings.temperatureModifier),
						Codec.FLOAT.fieldOf("downfall").forGetter(climateSettings -> climateSettings.downfall)
					)
					.apply(instance, Biome.ClimateSettings::new)
		);
		private final Biome.Precipitation precipitation;
		private final float temperature;
		private final Biome.TemperatureModifier temperatureModifier;
		private final float downfall;

		private ClimateSettings(Biome.Precipitation precipitation, float f, Biome.TemperatureModifier temperatureModifier, float g) {
			this.precipitation = precipitation;
			this.temperature = f;
			this.temperatureModifier = temperatureModifier;
			this.downfall = g;
		}
	}

	public static enum Precipitation implements StringRepresentable {
		NONE("none"),
		RAIN("rain"),
		SNOW("snow");

		public static final Codec<Biome.Precipitation> CODEC = StringRepresentable.fromEnum(Biome.Precipitation::values, Biome.Precipitation::byName);
		private static final Map<String, Biome.Precipitation> BY_NAME = (Map<String, Biome.Precipitation>)Arrays.stream(values())
			.collect(Collectors.toMap(Biome.Precipitation::getName, precipitation -> precipitation));
		private final String name;

		private Precipitation(String string2) {
			this.name = string2;
		}

		public String getName() {
			return this.name;
		}

		public static Biome.Precipitation byName(String string) {
			return (Biome.Precipitation)BY_NAME.get(string);
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}

	public static enum TemperatureModifier implements StringRepresentable {
		NONE("none") {
			@Override
			public float modifyTemperature(BlockPos blockPos, float f) {
				return f;
			}
		},
		FROZEN("frozen") {
			@Override
			public float modifyTemperature(BlockPos blockPos, float f) {
				double d = Biome.FROZEN_TEMPERATURE_NOISE.getValue((double)blockPos.getX() * 0.05, (double)blockPos.getZ() * 0.05, false) * 7.0;
				double e = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() * 0.2, (double)blockPos.getZ() * 0.2, false);
				double g = d + e;
				if (g < 0.3) {
					double h = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() * 0.09, (double)blockPos.getZ() * 0.09, false);
					if (h < 0.8) {
						return 0.2F;
					}
				}

				return f;
			}
		};

		private final String name;
		public static final Codec<Biome.TemperatureModifier> CODEC = StringRepresentable.fromEnum(
			Biome.TemperatureModifier::values, Biome.TemperatureModifier::byName
		);
		private static final Map<String, Biome.TemperatureModifier> BY_NAME = (Map<String, Biome.TemperatureModifier>)Arrays.stream(values())
			.collect(Collectors.toMap(Biome.TemperatureModifier::getName, temperatureModifier -> temperatureModifier));

		public abstract float modifyTemperature(BlockPos blockPos, float f);

		private TemperatureModifier(String string2) {
			this.name = string2;
		}

		public String getName() {
			return this.name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Biome.TemperatureModifier byName(String string) {
			return (Biome.TemperatureModifier)BY_NAME.get(string);
		}
	}
}
