package net.minecraft.world.level.biome;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Biome {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final Set<Biome> EXPLORABLE_BIOMES = Sets.<Biome>newHashSet();
	public static final IdMapper<Biome> MUTATED_BIOMES = new IdMapper<>();
	protected static final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(1234L), 0, 0);
	public static final PerlinSimplexNoise BIOME_INFO_NOISE = new PerlinSimplexNoise(new WorldgenRandom(2345L), 0, 0);
	@Nullable
	protected String descriptionId;
	protected final float depth;
	protected final float scale;
	protected final float temperature;
	protected final float downfall;
	protected final int waterColor;
	protected final int waterFogColor;
	@Nullable
	protected final String parent;
	protected final ConfiguredSurfaceBuilder<?> surfaceBuilder;
	protected final Biome.BiomeCategory biomeCategory;
	protected final Biome.Precipitation precipitation;
	protected final Map<GenerationStep.Carving, List<ConfiguredWorldCarver<?>>> carvers = Maps.<GenerationStep.Carving, List<ConfiguredWorldCarver<?>>>newHashMap();
	protected final Map<GenerationStep.Decoration, List<ConfiguredFeature<?, ?>>> features = Maps.<GenerationStep.Decoration, List<ConfiguredFeature<?, ?>>>newHashMap();
	protected final List<ConfiguredFeature<?, ?>> flowerFeatures = Lists.<ConfiguredFeature<?, ?>>newArrayList();
	protected final Map<StructureFeature<?>, FeatureConfiguration> validFeatureStarts = Maps.<StructureFeature<?>, FeatureConfiguration>newHashMap();
	private final Map<MobCategory, List<Biome.SpawnerData>> spawners = Maps.<MobCategory, List<Biome.SpawnerData>>newHashMap();
	private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache = ThreadLocal.withInitial(() -> Util.make(() -> {
			Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(1024, 0.25F) {
				@Override
				protected void rehash(int i) {
				}
			};
			long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
			return long2FloatLinkedOpenHashMap;
		}));

	@Nullable
	public static Biome getMutatedVariant(Biome biome) {
		return MUTATED_BIOMES.byId(Registry.BIOME.getId(biome));
	}

	public static <C extends CarverConfiguration> ConfiguredWorldCarver<C> makeCarver(WorldCarver<C> worldCarver, C carverConfiguration) {
		return new ConfiguredWorldCarver<>(worldCarver, carverConfiguration);
	}

	protected Biome(Biome.BiomeBuilder biomeBuilder) {
		if (biomeBuilder.surfaceBuilder != null
			&& biomeBuilder.precipitation != null
			&& biomeBuilder.biomeCategory != null
			&& biomeBuilder.depth != null
			&& biomeBuilder.scale != null
			&& biomeBuilder.temperature != null
			&& biomeBuilder.downfall != null
			&& biomeBuilder.waterColor != null
			&& biomeBuilder.waterFogColor != null) {
			this.surfaceBuilder = biomeBuilder.surfaceBuilder;
			this.precipitation = biomeBuilder.precipitation;
			this.biomeCategory = biomeBuilder.biomeCategory;
			this.depth = biomeBuilder.depth;
			this.scale = biomeBuilder.scale;
			this.temperature = biomeBuilder.temperature;
			this.downfall = biomeBuilder.downfall;
			this.waterColor = biomeBuilder.waterColor;
			this.waterFogColor = biomeBuilder.waterFogColor;
			this.parent = biomeBuilder.parent;

			for (GenerationStep.Decoration decoration : GenerationStep.Decoration.values()) {
				this.features.put(decoration, Lists.newArrayList());
			}

			for (MobCategory mobCategory : MobCategory.values()) {
				this.spawners.put(mobCategory, Lists.newArrayList());
			}
		} else {
			throw new IllegalStateException("You are missing parameters to build a proper biome for " + this.getClass().getSimpleName() + "\n" + biomeBuilder);
		}
	}

	public boolean isMutated() {
		return this.parent != null;
	}

	@Environment(EnvType.CLIENT)
	public int getSkyColor() {
		return 8364543;
	}

	protected void addSpawn(MobCategory mobCategory, Biome.SpawnerData spawnerData) {
		((List)this.spawners.get(mobCategory)).add(spawnerData);
	}

	public List<Biome.SpawnerData> getMobs(MobCategory mobCategory) {
		return (List<Biome.SpawnerData>)this.spawners.get(mobCategory);
	}

	public Biome.Precipitation getPrecipitation() {
		return this.precipitation;
	}

	public boolean isHumid() {
		return this.getDownfall() > 0.85F;
	}

	public float getCreatureProbability() {
		return 0.1F;
	}

	protected float getTemperatureNoCache(BlockPos blockPos) {
		if (blockPos.getY() > 64) {
			float f = (float)(TEMPERATURE_NOISE.getValue((double)((float)blockPos.getX() / 8.0F), (double)((float)blockPos.getZ() / 8.0F), false) * 4.0);
			return this.getTemperature() - (f + (float)blockPos.getY() - 64.0F) * 0.05F / 30.0F;
		} else {
			return this.getTemperature();
		}
	}

	public final float getTemperature(BlockPos blockPos) {
		long l = blockPos.asLong();
		Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = (Long2FloatLinkedOpenHashMap)this.temperatureCache.get();
		float f = long2FloatLinkedOpenHashMap.get(l);
		if (!Float.isNaN(f)) {
			return f;
		} else {
			float g = this.getTemperatureNoCache(blockPos);
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
		if (configuredFeature.feature == Feature.DECORATED_FLOWER) {
			this.flowerFeatures.add(configuredFeature);
		}

		((List)this.features.get(decoration)).add(configuredFeature);
	}

	public <C extends CarverConfiguration> void addCarver(GenerationStep.Carving carving, ConfiguredWorldCarver<C> configuredWorldCarver) {
		((List)this.carvers.computeIfAbsent(carving, carvingx -> Lists.newArrayList())).add(configuredWorldCarver);
	}

	public List<ConfiguredWorldCarver<?>> getCarvers(GenerationStep.Carving carving) {
		return (List<ConfiguredWorldCarver<?>>)this.carvers.computeIfAbsent(carving, carvingx -> Lists.newArrayList());
	}

	public <C extends FeatureConfiguration> void addStructureStart(ConfiguredFeature<C, ? extends StructureFeature<C>> configuredFeature) {
		this.validFeatureStarts.put(configuredFeature.feature, configuredFeature.config);
	}

	public <C extends FeatureConfiguration> boolean isValidStart(StructureFeature<C> structureFeature) {
		return this.validFeatureStarts.containsKey(structureFeature);
	}

	@Nullable
	public <C extends FeatureConfiguration> C getStructureConfiguration(StructureFeature<C> structureFeature) {
		return (C)this.validFeatureStarts.get(structureFeature);
	}

	public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
		return this.flowerFeatures;
	}

	public List<ConfiguredFeature<?, ?>> getFeaturesForStep(GenerationStep.Decoration decoration) {
		return (List<ConfiguredFeature<?, ?>>)this.features.get(decoration);
	}

	public void generate(
		GenerationStep.Decoration decoration,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		LevelAccessor levelAccessor,
		long l,
		WorldgenRandom worldgenRandom,
		BlockPos blockPos
	) {
		int i = 0;

		for (ConfiguredFeature<?, ?> configuredFeature : (List)this.features.get(decoration)) {
			worldgenRandom.setFeatureSeed(l, i, decoration.ordinal());

			try {
				configuredFeature.place(levelAccessor, chunkGenerator, worldgenRandom, blockPos);
			} catch (Exception var13) {
				CrashReport crashReport = CrashReport.forThrowable(var13, "Feature placement");
				crashReport.addCategory("Feature")
					.setDetail("Id", Registry.FEATURE.getKey(configuredFeature.feature))
					.setDetail("Description", (CrashReportDetail<String>)(() -> configuredFeature.feature.toString()));
				throw new ReportedException(crashReport);
			}

			i++;
		}
	}

	@Environment(EnvType.CLIENT)
	public int getGrassColor(double d, double e) {
		double f = (double)Mth.clamp(this.getTemperature(), 0.0F, 1.0F);
		double g = (double)Mth.clamp(this.getDownfall(), 0.0F, 1.0F);
		return GrassColor.get(f, g);
	}

	@Environment(EnvType.CLIENT)
	public int getFoliageColor() {
		double d = (double)Mth.clamp(this.getTemperature(), 0.0F, 1.0F);
		double e = (double)Mth.clamp(this.getDownfall(), 0.0F, 1.0F);
		return FoliageColor.get(d, e);
	}

	public void buildSurfaceAt(Random random, ChunkAccess chunkAccess, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, long m) {
		this.surfaceBuilder.initNoise(m);
		this.surfaceBuilder.apply(random, chunkAccess, this, i, j, k, d, blockState, blockState2, l, m);
	}

	public Biome.BiomeTempCategory getTemperatureCategory() {
		if (this.biomeCategory == Biome.BiomeCategory.OCEAN) {
			return Biome.BiomeTempCategory.OCEAN;
		} else if ((double)this.getTemperature() < 0.2) {
			return Biome.BiomeTempCategory.COLD;
		} else {
			return (double)this.getTemperature() < 1.0 ? Biome.BiomeTempCategory.MEDIUM : Biome.BiomeTempCategory.WARM;
		}
	}

	public final float getDepth() {
		return this.depth;
	}

	public final float getDownfall() {
		return this.downfall;
	}

	@Environment(EnvType.CLIENT)
	public Component getName() {
		return new TranslatableComponent(this.getDescriptionId());
	}

	public String getDescriptionId() {
		if (this.descriptionId == null) {
			this.descriptionId = Util.makeDescriptionId("biome", Registry.BIOME.getKey(this));
		}

		return this.descriptionId;
	}

	public final float getScale() {
		return this.scale;
	}

	public final float getTemperature() {
		return this.temperature;
	}

	public final int getWaterColor() {
		return this.waterColor;
	}

	public final int getWaterFogColor() {
		return this.waterFogColor;
	}

	public final Biome.BiomeCategory getBiomeCategory() {
		return this.biomeCategory;
	}

	public ConfiguredSurfaceBuilder<?> getSurfaceBuilder() {
		return this.surfaceBuilder;
	}

	public SurfaceBuilderConfiguration getSurfaceBuilderConfig() {
		return this.surfaceBuilder.getSurfaceBuilderConfiguration();
	}

	@Nullable
	public String getParent() {
		return this.parent;
	}

	public static class BiomeBuilder {
		@Nullable
		private ConfiguredSurfaceBuilder<?> surfaceBuilder;
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
		@Nullable
		private Float downfall;
		@Nullable
		private Integer waterColor;
		@Nullable
		private Integer waterFogColor;
		@Nullable
		private String parent;

		public <SC extends SurfaceBuilderConfiguration> Biome.BiomeBuilder surfaceBuilder(SurfaceBuilder<SC> surfaceBuilder, SC surfaceBuilderConfiguration) {
			this.surfaceBuilder = new ConfiguredSurfaceBuilder<>(surfaceBuilder, surfaceBuilderConfiguration);
			return this;
		}

		public Biome.BiomeBuilder surfaceBuilder(ConfiguredSurfaceBuilder<?> configuredSurfaceBuilder) {
			this.surfaceBuilder = configuredSurfaceBuilder;
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

		public Biome.BiomeBuilder waterColor(int i) {
			this.waterColor = i;
			return this;
		}

		public Biome.BiomeBuilder waterFogColor(int i) {
			this.waterFogColor = i;
			return this;
		}

		public Biome.BiomeBuilder parent(@Nullable String string) {
			this.parent = string;
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
				+ ",\ndownfall="
				+ this.downfall
				+ ",\nwaterColor="
				+ this.waterColor
				+ ",\nwaterFogColor="
				+ this.waterFogColor
				+ ",\nparent='"
				+ this.parent
				+ '\''
				+ "\n"
				+ '}';
		}
	}

	public static enum BiomeCategory {
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

		private static final Map<String, Biome.BiomeCategory> BY_NAME = (Map<String, Biome.BiomeCategory>)Arrays.stream(values())
			.collect(Collectors.toMap(Biome.BiomeCategory::getName, biomeCategory -> biomeCategory));
		private final String name;

		private BiomeCategory(String string2) {
			this.name = string2;
		}

		public String getName() {
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

	public static enum Precipitation {
		NONE("none"),
		RAIN("rain"),
		SNOW("snow");

		private static final Map<String, Biome.Precipitation> BY_NAME = (Map<String, Biome.Precipitation>)Arrays.stream(values())
			.collect(Collectors.toMap(Biome.Precipitation::getName, precipitation -> precipitation));
		private final String name;

		private Precipitation(String string2) {
			this.name = string2;
		}

		public String getName() {
			return this.name;
		}
	}

	public static class SpawnerData extends WeighedRandom.WeighedRandomItem {
		public final EntityType<?> type;
		public final int minCount;
		public final int maxCount;

		public SpawnerData(EntityType<?> entityType, int i, int j, int k) {
			super(i);
			this.type = entityType;
			this.minCount = j;
			this.maxCount = k;
		}

		public String toString() {
			return EntityType.getKey(this.type) + "*(" + this.minCount + "-" + this.maxCount + "):" + this.weight;
		}
	}
}
