package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
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
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
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
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Biome {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final MapCodec<Biome> DIRECT_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Biome.ClimateSettings.CODEC.forGetter(biome -> biome.climateSettings),
					Biome.BiomeCategory.CODEC.fieldOf("category").forGetter(biome -> biome.biomeCategory),
					Codec.FLOAT.fieldOf("depth").forGetter(biome -> biome.depth),
					Codec.FLOAT.fieldOf("scale").forGetter(biome -> biome.scale),
					BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter(biome -> biome.specialEffects),
					Biome.WorldGenerationSettings.CODEC.forGetter(biome -> biome.worldGenerationSettings),
					Biome.MobSettings.CODEC.forGetter(biome -> biome.mobSettings),
					Codec.STRING.optionalFieldOf("parent").forGetter(biome -> Optional.ofNullable(biome.parent))
				)
				.apply(instance, Biome::new)
	);
	public static final Codec<Supplier<Biome>> CODEC = RegistryFileCodec.create(Registry.BIOME_REGISTRY, DIRECT_CODEC);
	public static final Set<Biome> EXPLORABLE_BIOMES = Sets.<Biome>newHashSet();
	private static final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(1234L), ImmutableList.of(0));
	private static final PerlinSimplexNoise FROZEN_TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(3456L), ImmutableList.of(-2, -1, 0));
	public static final PerlinSimplexNoise BIOME_INFO_NOISE = new PerlinSimplexNoise(new WorldgenRandom(2345L), ImmutableList.of(0));
	private final Biome.ClimateSettings climateSettings;
	private final Biome.WorldGenerationSettings worldGenerationSettings;
	private final Biome.MobSettings mobSettings;
	private final float depth;
	private final float scale;
	@Nullable
	protected final String parent;
	private final Biome.BiomeCategory biomeCategory;
	private final BiomeSpecialEffects specialEffects;
	private final List<ConfiguredFeature<?, ?>> flowerFeatures;
	private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache = ThreadLocal.withInitial(() -> Util.make(() -> {
			Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(1024, 0.25F) {
				@Override
				protected void rehash(int i) {
				}
			};
			long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
			return long2FloatLinkedOpenHashMap;
		}));

	public Biome(Biome.BiomeBuilder biomeBuilder) {
		if (biomeBuilder.surfaceBuilder != null
			&& biomeBuilder.precipitation != null
			&& biomeBuilder.biomeCategory != null
			&& biomeBuilder.depth != null
			&& biomeBuilder.scale != null
			&& biomeBuilder.temperature != null
			&& biomeBuilder.downfall != null
			&& biomeBuilder.specialEffects != null) {
			this.climateSettings = new Biome.ClimateSettings(
				biomeBuilder.precipitation, biomeBuilder.temperature, biomeBuilder.temperatureModifier, biomeBuilder.downfall
			);
			this.biomeCategory = biomeBuilder.biomeCategory;
			this.depth = biomeBuilder.depth;
			this.scale = biomeBuilder.scale;
			this.parent = biomeBuilder.parent;
			this.specialEffects = biomeBuilder.specialEffects;
			this.mobSettings = new Biome.MobSettings(biomeBuilder.creatureGenerationProbability);
			this.worldGenerationSettings = new Biome.WorldGenerationSettings(biomeBuilder.surfaceBuilder);
			this.flowerFeatures = Lists.<ConfiguredFeature<?, ?>>newArrayList();
		} else {
			throw new IllegalStateException("You are missing parameters to build a proper biome for " + this.getClass().getSimpleName() + "\n" + biomeBuilder);
		}
	}

	private Biome(
		Biome.ClimateSettings climateSettings,
		Biome.BiomeCategory biomeCategory,
		float f,
		float g,
		BiomeSpecialEffects biomeSpecialEffects,
		Biome.WorldGenerationSettings worldGenerationSettings,
		Biome.MobSettings mobSettings,
		Optional<String> optional
	) {
		this.climateSettings = climateSettings;
		this.worldGenerationSettings = worldGenerationSettings;
		this.mobSettings = mobSettings;
		this.biomeCategory = biomeCategory;
		this.depth = f;
		this.scale = g;
		this.specialEffects = biomeSpecialEffects;
		this.parent = (String)optional.orElse(null);
		this.flowerFeatures = (List<ConfiguredFeature<?, ?>>)worldGenerationSettings.features
			.stream()
			.flatMap(Collection::stream)
			.map(Supplier::get)
			.flatMap(ConfiguredFeature::getFeatures)
			.filter(configuredFeature -> configuredFeature.feature == Feature.FLOWER)
			.collect(Collectors.toList());
	}

	public boolean isMutated() {
		return this.parent != null;
	}

	@Environment(EnvType.CLIENT)
	public int getSkyColor() {
		return this.specialEffects.getSkyColor();
	}

	public void addSpawn(MobCategory mobCategory, Biome.SpawnerData spawnerData) {
		((List)this.mobSettings.spawners.get(mobCategory)).add(spawnerData);
	}

	public void addMobCharge(EntityType<?> entityType, double d, double e) {
		this.mobSettings.mobSpawnCosts.put(entityType, new Biome.MobSpawnCost(e, d));
	}

	public List<Biome.SpawnerData> getMobs(MobCategory mobCategory) {
		return (List<Biome.SpawnerData>)this.mobSettings.spawners.get(mobCategory);
	}

	@Nullable
	public Biome.MobSpawnCost getMobSpawnCost(EntityType<?> entityType) {
		return (Biome.MobSpawnCost)this.mobSettings.mobSpawnCosts.get(entityType);
	}

	public Biome.Precipitation getPrecipitation() {
		return this.climateSettings.precipitation;
	}

	public boolean isHumid() {
		return this.getDownfall() > 0.85F;
	}

	public float getCreatureProbability() {
		return this.mobSettings.creatureGenerationProbability;
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
			if (blockPos.getY() >= 0 && blockPos.getY() < 256 && levelReader.getBrightness(LightLayer.BLOCK, blockPos) < 10) {
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
			if (blockPos.getY() >= 0 && blockPos.getY() < 256 && levelReader.getBrightness(LightLayer.BLOCK, blockPos) < 10) {
				BlockState blockState = levelReader.getBlockState(blockPos);
				if (blockState.isAir() && Blocks.SNOW.defaultBlockState().canSurvive(levelReader, blockPos)) {
					return true;
				}
			}

			return false;
		}
	}

	public void addFeature(GenerationStep.Decoration decoration, ConfiguredFeature<?, ?> configuredFeature) {
		this.addFeature(decoration.ordinal(), () -> configuredFeature);
	}

	public void addFeature(int i, Supplier<ConfiguredFeature<?, ?>> supplier) {
		((ConfiguredFeature)supplier.get()).getFeatures().filter(configuredFeature -> configuredFeature.feature == Feature.FLOWER).forEach(this.flowerFeatures::add);
		this.addFeatureStepsUpTo(i);
		((List)this.worldGenerationSettings.features.get(i)).add(supplier);
	}

	public <C extends CarverConfiguration> void addCarver(GenerationStep.Carving carving, ConfiguredWorldCarver<C> configuredWorldCarver) {
		((List)this.worldGenerationSettings.carvers.computeIfAbsent(carving, carvingx -> Lists.newArrayList())).add((Supplier)() -> configuredWorldCarver);
	}

	public List<Supplier<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving carving) {
		return (List<Supplier<ConfiguredWorldCarver<?>>>)this.worldGenerationSettings.carvers.getOrDefault(carving, ImmutableList.of());
	}

	public void addStructureStart(ConfiguredStructureFeature<?, ?> configuredStructureFeature) {
		this.worldGenerationSettings.structureStarts.add((Supplier)() -> configuredStructureFeature);
		this.addFeatureStepsUpTo(configuredStructureFeature.feature.step().ordinal());
	}

	public boolean isValidStart(StructureFeature<?> structureFeature) {
		return this.worldGenerationSettings.structureStarts.stream().anyMatch(supplier -> ((ConfiguredStructureFeature)supplier.get()).feature == structureFeature);
	}

	public Iterable<Supplier<ConfiguredStructureFeature<?, ?>>> structures() {
		return this.worldGenerationSettings.structureStarts;
	}

	private void addFeatureStepsUpTo(int i) {
		while (this.worldGenerationSettings.features.size() <= i) {
			this.worldGenerationSettings.features.add(Lists.newArrayList());
		}
	}

	public ConfiguredStructureFeature<?, ?> withBiomeConfig(ConfiguredStructureFeature<?, ?> configuredStructureFeature) {
		return DataFixUtils.orElse(
			this.worldGenerationSettings
				.structureStarts
				.stream()
				.map(Supplier::get)
				.filter(configuredStructureFeature2 -> configuredStructureFeature2.feature == configuredStructureFeature.feature)
				.findAny(),
			configuredStructureFeature
		);
	}

	public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
		return this.flowerFeatures;
	}

	public List<List<Supplier<ConfiguredFeature<?, ?>>>> features() {
		return this.worldGenerationSettings.features;
	}

	public void generate(
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		WorldGenRegion worldGenRegion,
		long l,
		WorldgenRandom worldgenRandom,
		BlockPos blockPos
	) {
		for (int i = 0; i < this.worldGenerationSettings.features.size(); i++) {
			int j = 0;
			if (structureFeatureManager.shouldGenerateFeatures()) {
				for (StructureFeature<?> structureFeature : Registry.STRUCTURE_FEATURE) {
					if (structureFeature.step().ordinal() == i) {
						worldgenRandom.setFeatureSeed(l, j, i);
						int k = blockPos.getX() >> 4;
						int m = blockPos.getZ() >> 4;
						int n = k << 4;
						int o = m << 4;

						try {
							structureFeatureManager.startsForFeature(SectionPos.of(blockPos), structureFeature)
								.forEach(
									structureStart -> structureStart.placeInChunk(
											worldGenRegion, structureFeatureManager, chunkGenerator, worldgenRandom, new BoundingBox(n, o, n + 15, o + 15), new ChunkPos(k, m)
										)
								);
						} catch (Exception var18) {
							CrashReport crashReport = CrashReport.forThrowable(var18, "Feature placement");
							crashReport.addCategory("Feature")
								.setDetail("Id", Registry.STRUCTURE_FEATURE.getKey(structureFeature))
								.setDetail("Description", (CrashReportDetail<String>)(() -> structureFeature.toString()));
							throw new ReportedException(crashReport);
						}

						j++;
					}
				}
			}

			for (Supplier<ConfiguredFeature<?, ?>> supplier : (List)this.worldGenerationSettings.features.get(i)) {
				ConfiguredFeature<?, ?> configuredFeature = (ConfiguredFeature<?, ?>)supplier.get();
				worldgenRandom.setFeatureSeed(l, j, i);

				try {
					configuredFeature.place(worldGenRegion, chunkGenerator, worldgenRandom, blockPos);
				} catch (Exception var19) {
					CrashReport crashReport2 = CrashReport.forThrowable(var19, "Feature placement");
					crashReport2.addCategory("Feature")
						.setDetail("Id", Registry.FEATURE.getKey(configuredFeature.feature))
						.setDetail("Config", configuredFeature.config)
						.setDetail("Description", (CrashReportDetail<String>)(() -> configuredFeature.feature.toString()));
					throw new ReportedException(crashReport2);
				}

				j++;
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
		ConfiguredSurfaceBuilder<?> configuredSurfaceBuilder = (ConfiguredSurfaceBuilder<?>)this.worldGenerationSettings.surfaceBuilder.get();
		configuredSurfaceBuilder.initNoise(m);
		configuredSurfaceBuilder.apply(random, chunkAccess, this, i, j, k, d, blockState, blockState2, l, m);
	}

	public Biome.BiomeTempCategory getTemperatureCategory() {
		if (this.biomeCategory == Biome.BiomeCategory.OCEAN) {
			return Biome.BiomeTempCategory.OCEAN;
		} else if ((double)this.getBaseTemperature() < 0.2) {
			return Biome.BiomeTempCategory.COLD;
		} else {
			return (double)this.getBaseTemperature() < 1.0 ? Biome.BiomeTempCategory.MEDIUM : Biome.BiomeTempCategory.WARM;
		}
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

	public Supplier<ConfiguredSurfaceBuilder<?>> getSurfaceBuilder() {
		return this.worldGenerationSettings.surfaceBuilder;
	}

	public SurfaceBuilderConfiguration getSurfaceBuilderConfig() {
		return ((ConfiguredSurfaceBuilder)this.worldGenerationSettings.surfaceBuilder.get()).config();
	}

	@Nullable
	public String getParent() {
		return this.parent;
	}

	public String toString() {
		ResourceLocation resourceLocation = BuiltinRegistries.BIOME.getKey(this);
		return resourceLocation == null ? super.toString() : resourceLocation.toString();
	}

	public static class BiomeBuilder {
		@Nullable
		private Supplier<ConfiguredSurfaceBuilder<?>> surfaceBuilder;
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
		private String parent;
		@Nullable
		private BiomeSpecialEffects specialEffects;
		private float creatureGenerationProbability = 0.1F;

		public Biome.BiomeBuilder surfaceBuilder(ConfiguredSurfaceBuilder<?> configuredSurfaceBuilder) {
			return this.surfaceBuilder(() -> configuredSurfaceBuilder);
		}

		public Biome.BiomeBuilder surfaceBuilder(Supplier<ConfiguredSurfaceBuilder<?>> supplier) {
			this.surfaceBuilder = supplier;
			return this;
		}

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

		public Biome.BiomeBuilder parent(@Nullable String string) {
			this.parent = string;
			return this;
		}

		public Biome.BiomeBuilder specialEffects(BiomeSpecialEffects biomeSpecialEffects) {
			this.specialEffects = biomeSpecialEffects;
			return this;
		}

		public Biome.BiomeBuilder creatureGenerationProbability(float f) {
			this.creatureGenerationProbability = f;
			return this;
		}

		public Biome.BiomeBuilder temperatureAdjustment(Biome.TemperatureModifier temperatureModifier) {
			this.temperatureModifier = temperatureModifier;
			return this;
		}

		public String toString() {
			return "BiomeBuilder{\nsurfaceBuilder="
				+ this.surfaceBuilder
				+ ",\nprecipitation="
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
				+ ",\ncreatureGenerationProbability="
				+ this.creatureGenerationProbability
				+ ",\nparent='"
				+ this.parent
				+ '\''
				+ "\n"
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

	public static enum BiomeTempCategory {
		OCEAN("ocean"),
		COLD("cold"),
		MEDIUM("medium"),
		WARM("warm");

		private static final Map<String, Biome.BiomeTempCategory> BY_NAME = (Map<String, Biome.BiomeTempCategory>)Arrays.stream(values())
			.collect(Collectors.toMap(Biome.BiomeTempCategory::getName, biomeTempCategory -> biomeTempCategory));
		private final String name;

		private BiomeTempCategory(String string2) {
			this.name = string2;
		}

		public String getName() {
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

	static class MobSettings {
		public static final MapCodec<Biome.MobSettings> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						Codec.FLOAT.optionalFieldOf("creature_spawn_probability", Float.valueOf(0.1F)).forGetter(mobSettings -> mobSettings.creatureGenerationProbability),
						Codec.simpleMap(
								MobCategory.CODEC,
								Biome.SpawnerData.CODEC.listOf().promotePartial(Util.prefix("Spawn data: ", Biome.LOGGER::error)),
								StringRepresentable.keys(MobCategory.values())
							)
							.fieldOf("spawners")
							.forGetter(mobSettings -> mobSettings.spawners),
						Codec.simpleMap(Registry.ENTITY_TYPE, Biome.MobSpawnCost.CODEC, Registry.ENTITY_TYPE)
							.fieldOf("spawn_costs")
							.forGetter(mobSettings -> mobSettings.mobSpawnCosts)
					)
					.apply(instance, Biome.MobSettings::new)
		);
		private final float creatureGenerationProbability;
		private final Map<MobCategory, List<Biome.SpawnerData>> spawners;
		private final Map<EntityType<?>, Biome.MobSpawnCost> mobSpawnCosts;

		private MobSettings(float f, Map<MobCategory, List<Biome.SpawnerData>> map, Map<EntityType<?>, Biome.MobSpawnCost> map2) {
			this.creatureGenerationProbability = f;
			this.spawners = map;
			this.mobSpawnCosts = map2;
		}

		private MobSettings(float f) {
			this(f, Maps.<MobCategory, List<Biome.SpawnerData>>newLinkedHashMap(), Maps.<EntityType<?>, Biome.MobSpawnCost>newLinkedHashMap());

			for (MobCategory mobCategory : MobCategory.values()) {
				this.spawners.put(mobCategory, Lists.newArrayList());
			}
		}
	}

	public static class MobSpawnCost {
		public static final Codec<Biome.MobSpawnCost> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.DOUBLE.fieldOf("energy_budget").forGetter(Biome.MobSpawnCost::getEnergyBudget),
						Codec.DOUBLE.fieldOf("charge").forGetter(Biome.MobSpawnCost::getCharge)
					)
					.apply(instance, Biome.MobSpawnCost::new)
		);
		private final double energyBudget;
		private final double charge;

		public MobSpawnCost(double d, double e) {
			this.energyBudget = d;
			this.charge = e;
		}

		public double getEnergyBudget() {
			return this.energyBudget;
		}

		public double getCharge() {
			return this.charge;
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

	public static class SpawnerData extends WeighedRandom.WeighedRandomItem {
		public static final Codec<Biome.SpawnerData> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Registry.ENTITY_TYPE.fieldOf("type").forGetter(spawnerData -> spawnerData.type),
						Codec.INT.fieldOf("weight").forGetter(spawnerData -> spawnerData.weight),
						Codec.INT.fieldOf("minCount").forGetter(spawnerData -> spawnerData.minCount),
						Codec.INT.fieldOf("maxCount").forGetter(spawnerData -> spawnerData.maxCount)
					)
					.apply(instance, Biome.SpawnerData::new)
		);
		public final EntityType<?> type;
		public final int minCount;
		public final int maxCount;

		public SpawnerData(EntityType<?> entityType, int i, int j, int k) {
			super(i);
			this.type = entityType.getCategory() == MobCategory.MISC ? EntityType.PIG : entityType;
			this.minCount = j;
			this.maxCount = k;
		}

		public String toString() {
			return EntityType.getKey(this.type) + "*(" + this.minCount + "-" + this.maxCount + "):" + this.weight;
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

	static class WorldGenerationSettings {
		public static final MapCodec<Biome.WorldGenerationSettings> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						ConfiguredSurfaceBuilder.CODEC.fieldOf("surface_builder").forGetter(worldGenerationSettings -> worldGenerationSettings.surfaceBuilder),
						Codec.simpleMap(
								GenerationStep.Carving.CODEC,
								ConfiguredWorldCarver.CODEC.listOf().promotePartial(Util.prefix("Carver: ", Biome.LOGGER::error)),
								StringRepresentable.keys(GenerationStep.Carving.values())
							)
							.fieldOf("carvers")
							.forGetter(worldGenerationSettings -> worldGenerationSettings.carvers),
						ConfiguredFeature.CODEC
							.listOf()
							.promotePartial(Util.prefix("Feature: ", Biome.LOGGER::error))
							.listOf()
							.fieldOf("features")
							.forGetter(worldGenerationSettings -> worldGenerationSettings.features),
						ConfiguredStructureFeature.CODEC
							.listOf()
							.promotePartial(Util.prefix("Structure start: ", Biome.LOGGER::error))
							.fieldOf("starts")
							.forGetter(worldGenerationSettings -> worldGenerationSettings.structureStarts)
					)
					.apply(instance, Biome.WorldGenerationSettings::new)
		);
		private final Supplier<ConfiguredSurfaceBuilder<?>> surfaceBuilder;
		private final Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> carvers;
		private final List<List<Supplier<ConfiguredFeature<?, ?>>>> features;
		private final List<Supplier<ConfiguredStructureFeature<?, ?>>> structureStarts;

		private WorldGenerationSettings(
			Supplier<ConfiguredSurfaceBuilder<?>> supplier,
			Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> map,
			List<List<Supplier<ConfiguredFeature<?, ?>>>> list,
			List<Supplier<ConfiguredStructureFeature<?, ?>>> list2
		) {
			this.surfaceBuilder = supplier;
			this.carvers = map;
			this.features = list;
			this.structureStarts = list2;
		}

		private WorldGenerationSettings(Supplier<ConfiguredSurfaceBuilder<?>> supplier) {
			this(
				supplier,
				Maps.<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>>newLinkedHashMap(),
				Lists.<List<Supplier<ConfiguredFeature<?, ?>>>>newArrayList(),
				Lists.<Supplier<ConfiguredStructureFeature<?, ?>>>newArrayList()
			);
		}
	}
}
