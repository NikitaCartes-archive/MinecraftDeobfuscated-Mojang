/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class Biome {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Codec<Biome> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Precipitation.CODEC.fieldOf("precipitation")).forGetter(biome -> biome.precipitation), ((MapCodec)BiomeCategory.CODEC.fieldOf("category")).forGetter(biome -> biome.biomeCategory), ((MapCodec)Codec.FLOAT.fieldOf("depth")).forGetter(biome -> Float.valueOf(biome.depth)), ((MapCodec)Codec.FLOAT.fieldOf("scale")).forGetter(biome -> Float.valueOf(biome.scale)), ((MapCodec)Codec.FLOAT.fieldOf("temperature")).forGetter(biome -> Float.valueOf(biome.temperature)), ((MapCodec)Codec.FLOAT.fieldOf("downfall")).forGetter(biome -> Float.valueOf(biome.downfall)), ((MapCodec)BiomeSpecialEffects.CODEC.fieldOf("effects")).forGetter(biome -> biome.specialEffects), ((MapCodec)Codec.INT.fieldOf("sky_color")).forGetter(biome -> biome.skyColor), ((MapCodec)ConfiguredSurfaceBuilder.CODEC.fieldOf("surface_builder")).forGetter(biome -> biome.surfaceBuilder), Codec.simpleMap(GenerationStep.Carving.CODEC, ConfiguredWorldCarver.CODEC.listOf().promotePartial((Consumer)Util.prefix("Carver: ", LOGGER::error)), StringRepresentable.keys(GenerationStep.Carving.values())).fieldOf("carvers").forGetter(biome -> biome.carvers), Codec.simpleMap(GenerationStep.Decoration.CODEC, ConfiguredFeature.CODEC.listOf().promotePartial((Consumer)Util.prefix("Feature: ", LOGGER::error)), StringRepresentable.keys(GenerationStep.Decoration.values())).fieldOf("features").forGetter(biome -> biome.features), ((MapCodec)ConfiguredStructureFeature.CODEC.listOf().promotePartial((Consumer)Util.prefix("Structure start: ", LOGGER::error)).fieldOf("starts")).forGetter(biome -> biome.validFeatureStarts.values().stream().sorted(Comparator.comparing(configuredStructureFeature -> Registry.STRUCTURE_FEATURE.getKey((StructureFeature<?>)configuredStructureFeature.feature))).collect(Collectors.toList())), Codec.simpleMap(MobCategory.CODEC, SpawnerData.CODEC.listOf().promotePartial((Consumer)Util.prefix("Spawn data: ", LOGGER::error)), StringRepresentable.keys(MobCategory.values())).fieldOf("spawners").forGetter(biome -> biome.spawners), ((MapCodec)ClimateParameters.CODEC.listOf().fieldOf("climate_parameters")).forGetter(biome -> biome.optimalParameters), Codec.STRING.optionalFieldOf("parent").forGetter(biome -> Optional.ofNullable(biome.parent))).apply((Applicative<Biome, ?>)instance, Biome::new));
    public static final Set<Biome> EXPLORABLE_BIOMES = Sets.newHashSet();
    public static final IdMapper<Biome> MUTATED_BIOMES = new IdMapper();
    protected static final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(1234L), ImmutableList.of(Integer.valueOf(0)));
    public static final PerlinSimplexNoise BIOME_INFO_NOISE = new PerlinSimplexNoise(new WorldgenRandom(2345L), ImmutableList.of(Integer.valueOf(0)));
    @Nullable
    protected String descriptionId;
    protected final float depth;
    protected final float scale;
    protected final float temperature;
    protected final float downfall;
    private final int skyColor;
    @Nullable
    protected final String parent;
    protected final ConfiguredSurfaceBuilder<?> surfaceBuilder;
    protected final BiomeCategory biomeCategory;
    protected final Precipitation precipitation;
    protected final BiomeSpecialEffects specialEffects;
    protected final Map<GenerationStep.Carving, List<ConfiguredWorldCarver<?>>> carvers;
    protected final Map<GenerationStep.Decoration, List<ConfiguredFeature<?, ?>>> features;
    protected final List<ConfiguredFeature<?, ?>> flowerFeatures = Lists.newArrayList();
    private final Map<StructureFeature<?>, ConfiguredStructureFeature<?, ?>> validFeatureStarts;
    private final Map<MobCategory, List<SpawnerData>> spawners;
    private final Map<EntityType<?>, MobSpawnCost> mobSpawnCosts = Maps.newHashMap();
    private final List<ClimateParameters> optimalParameters;
    private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache = ThreadLocal.withInitial(() -> Util.make(() -> {
        Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(1024, 0.25f){

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
        return new ConfiguredWorldCarver<C>(worldCarver, carverConfiguration);
    }

    protected Biome(BiomeBuilder biomeBuilder) {
        if (biomeBuilder.surfaceBuilder == null || biomeBuilder.precipitation == null || biomeBuilder.biomeCategory == null || biomeBuilder.depth == null || biomeBuilder.scale == null || biomeBuilder.temperature == null || biomeBuilder.downfall == null || biomeBuilder.specialEffects == null) {
            throw new IllegalStateException("You are missing parameters to build a proper biome for " + this.getClass().getSimpleName() + "\n" + biomeBuilder);
        }
        this.surfaceBuilder = biomeBuilder.surfaceBuilder;
        this.precipitation = biomeBuilder.precipitation;
        this.biomeCategory = biomeBuilder.biomeCategory;
        this.depth = biomeBuilder.depth.floatValue();
        this.scale = biomeBuilder.scale.floatValue();
        this.temperature = biomeBuilder.temperature.floatValue();
        this.downfall = biomeBuilder.downfall.floatValue();
        this.skyColor = this.calculateSkyColor();
        this.parent = biomeBuilder.parent;
        this.optimalParameters = biomeBuilder.optimalParameters != null ? biomeBuilder.optimalParameters : ImmutableList.of();
        this.specialEffects = biomeBuilder.specialEffects;
        this.carvers = Maps.newHashMap();
        this.validFeatureStarts = Maps.newHashMap();
        this.features = Maps.newHashMap();
        for (GenerationStep.Decoration decoration : GenerationStep.Decoration.values()) {
            this.features.put(decoration, Lists.newArrayList());
        }
        this.spawners = Maps.newHashMap();
        for (Enum enum_ : MobCategory.values()) {
            this.spawners.put((MobCategory)enum_, Lists.newArrayList());
        }
    }

    private Biome(Precipitation precipitation, BiomeCategory biomeCategory, float f, float g, float h, float i, BiomeSpecialEffects biomeSpecialEffects, int j, ConfiguredSurfaceBuilder<?> configuredSurfaceBuilder, Map<GenerationStep.Carving, List<ConfiguredWorldCarver<?>>> map, Map<GenerationStep.Decoration, List<ConfiguredFeature<?, ?>>> map2, List<ConfiguredStructureFeature<?, ?>> list, Map<MobCategory, List<SpawnerData>> map3, List<ClimateParameters> list2, Optional<String> optional) {
        this.precipitation = precipitation;
        this.biomeCategory = biomeCategory;
        this.depth = f;
        this.scale = g;
        this.temperature = h;
        this.downfall = i;
        this.specialEffects = biomeSpecialEffects;
        this.skyColor = j;
        this.surfaceBuilder = configuredSurfaceBuilder;
        this.carvers = map;
        this.features = map2;
        this.validFeatureStarts = list.stream().collect(Collectors.toMap(configuredStructureFeature -> configuredStructureFeature.feature, Function.identity()));
        this.spawners = map3;
        this.optimalParameters = list2;
        this.parent = optional.orElse(null);
        map2.values().stream().flatMap(Collection::stream).filter(configuredFeature -> configuredFeature.feature == Feature.DECORATED_FLOWER).forEach(this.flowerFeatures::add);
    }

    public boolean isMutated() {
        return this.parent != null;
    }

    private int calculateSkyColor() {
        float f = this.temperature;
        f /= 3.0f;
        f = Mth.clamp(f, -1.0f, 1.0f);
        return Mth.hsvToRgb(0.62222224f - f * 0.05f, 0.5f + f * 0.1f, 1.0f);
    }

    @Environment(value=EnvType.CLIENT)
    public int getSkyColor() {
        return this.skyColor;
    }

    protected void addSpawn(MobCategory mobCategory, SpawnerData spawnerData) {
        this.spawners.get(mobCategory).add(spawnerData);
    }

    protected void addMobCharge(EntityType<?> entityType, double d, double e) {
        this.mobSpawnCosts.put(entityType, new MobSpawnCost(e, d));
    }

    public List<SpawnerData> getMobs(MobCategory mobCategory) {
        return this.spawners.get(mobCategory);
    }

    @Nullable
    public MobSpawnCost getMobSpawnCost(EntityType<?> entityType) {
        return this.mobSpawnCosts.get(entityType);
    }

    public Precipitation getPrecipitation() {
        return this.precipitation;
    }

    public boolean isHumid() {
        return this.getDownfall() > 0.85f;
    }

    public float getCreatureProbability() {
        return 0.1f;
    }

    protected float getTemperatureNoCache(BlockPos blockPos) {
        if (blockPos.getY() > 64) {
            float f = (float)(TEMPERATURE_NOISE.getValue((float)blockPos.getX() / 8.0f, (float)blockPos.getZ() / 8.0f, false) * 4.0);
            return this.getTemperature() - (f + (float)blockPos.getY() - 64.0f) * 0.05f / 30.0f;
        }
        return this.getTemperature();
    }

    public final float getTemperature(BlockPos blockPos) {
        long l = blockPos.asLong();
        Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = this.temperatureCache.get();
        float f = long2FloatLinkedOpenHashMap.get(l);
        if (!Float.isNaN(f)) {
            return f;
        }
        float g = this.getTemperatureNoCache(blockPos);
        if (long2FloatLinkedOpenHashMap.size() == 1024) {
            long2FloatLinkedOpenHashMap.removeFirstFloat();
        }
        long2FloatLinkedOpenHashMap.put(l, g);
        return g;
    }

    public boolean shouldFreeze(LevelReader levelReader, BlockPos blockPos) {
        return this.shouldFreeze(levelReader, blockPos, true);
    }

    public boolean shouldFreeze(LevelReader levelReader, BlockPos blockPos, boolean bl) {
        if (this.getTemperature(blockPos) >= 0.15f) {
            return false;
        }
        if (blockPos.getY() >= 0 && blockPos.getY() < 256 && levelReader.getBrightness(LightLayer.BLOCK, blockPos) < 10) {
            BlockState blockState = levelReader.getBlockState(blockPos);
            FluidState fluidState = levelReader.getFluidState(blockPos);
            if (fluidState.getType() == Fluids.WATER && blockState.getBlock() instanceof LiquidBlock) {
                boolean bl2;
                if (!bl) {
                    return true;
                }
                boolean bl3 = bl2 = levelReader.isWaterAt(blockPos.west()) && levelReader.isWaterAt(blockPos.east()) && levelReader.isWaterAt(blockPos.north()) && levelReader.isWaterAt(blockPos.south());
                if (!bl2) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean shouldSnow(LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState;
        if (this.getTemperature(blockPos) >= 0.15f) {
            return false;
        }
        return blockPos.getY() >= 0 && blockPos.getY() < 256 && levelReader.getBrightness(LightLayer.BLOCK, blockPos) < 10 && (blockState = levelReader.getBlockState(blockPos)).isAir() && Blocks.SNOW.defaultBlockState().canSurvive(levelReader, blockPos);
    }

    public void addFeature(GenerationStep.Decoration decoration, ConfiguredFeature<?, ?> configuredFeature) {
        if (configuredFeature.feature == Feature.DECORATED_FLOWER) {
            this.flowerFeatures.add(configuredFeature);
        }
        this.features.get(decoration).add(configuredFeature);
    }

    public <C extends CarverConfiguration> void addCarver(GenerationStep.Carving carving2, ConfiguredWorldCarver<C> configuredWorldCarver) {
        this.carvers.computeIfAbsent(carving2, carving -> Lists.newArrayList()).add(configuredWorldCarver);
    }

    public List<ConfiguredWorldCarver<?>> getCarvers(GenerationStep.Carving carving2) {
        return this.carvers.computeIfAbsent(carving2, carving -> Lists.newArrayList());
    }

    public void addStructureStart(ConfiguredStructureFeature<?, ?> configuredStructureFeature) {
        this.validFeatureStarts.put((StructureFeature<?>)configuredStructureFeature.feature, configuredStructureFeature);
    }

    public boolean isValidStart(StructureFeature<?> structureFeature) {
        return this.validFeatureStarts.containsKey(structureFeature);
    }

    public Iterable<ConfiguredStructureFeature<?, ?>> structures() {
        return this.validFeatureStarts.values();
    }

    public ConfiguredStructureFeature<?, ?> withBiomeConfig(ConfiguredStructureFeature<?, ?> configuredStructureFeature) {
        return this.validFeatureStarts.getOrDefault(configuredStructureFeature.feature, configuredStructureFeature);
    }

    public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
        return this.flowerFeatures;
    }

    public List<ConfiguredFeature<?, ?>> getFeaturesForStep(GenerationStep.Decoration decoration) {
        return this.features.get(decoration);
    }

    public void generate(GenerationStep.Decoration decoration, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, WorldGenLevel worldGenLevel, long l, WorldgenRandom worldgenRandom, BlockPos blockPos) {
        int i = 0;
        if (structureFeatureManager.shouldGenerateFeatures()) {
            for (StructureFeature structureFeature : Registry.STRUCTURE_FEATURE) {
                if (structureFeature.step() != decoration) continue;
                worldgenRandom.setFeatureSeed(l, i, decoration.ordinal());
                int j = blockPos.getX() >> 4;
                int k = blockPos.getZ() >> 4;
                int m = j << 4;
                int n = k << 4;
                try {
                    structureFeatureManager.startsForFeature(SectionPos.of(blockPos), structureFeature).forEach(structureStart -> structureStart.placeInChunk(worldGenLevel, structureFeatureManager, chunkGenerator, worldgenRandom, new BoundingBox(m, n, m + 15, n + 15), new ChunkPos(j, k)));
                } catch (Exception exception) {
                    CrashReport crashReport = CrashReport.forThrowable(exception, "Feature placement");
                    crashReport.addCategory("Feature").setDetail("Id", Registry.STRUCTURE_FEATURE.getKey(structureFeature)).setDetail("Description", () -> structureFeature.toString());
                    throw new ReportedException(crashReport);
                }
                ++i;
            }
        }
        for (ConfiguredFeature configuredFeature : this.features.get(decoration)) {
            worldgenRandom.setFeatureSeed(l, i, decoration.ordinal());
            try {
                configuredFeature.place(worldGenLevel, structureFeatureManager, chunkGenerator, worldgenRandom, blockPos);
            } catch (Exception exception2) {
                CrashReport crashReport2 = CrashReport.forThrowable(exception2, "Feature placement");
                crashReport2.addCategory("Feature").setDetail("Id", Registry.FEATURE.getKey((Feature<?>)configuredFeature.feature)).setDetail("Config", configuredFeature.config).setDetail("Description", () -> configuredFeature.feature.toString());
                throw new ReportedException(crashReport2);
            }
            ++i;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public int getFogColor() {
        return this.specialEffects.getFogColor();
    }

    @Environment(value=EnvType.CLIENT)
    public int getGrassColor(double d, double e) {
        double f = Mth.clamp(this.getTemperature(), 0.0f, 1.0f);
        double g = Mth.clamp(this.getDownfall(), 0.0f, 1.0f);
        return GrassColor.get(f, g);
    }

    @Environment(value=EnvType.CLIENT)
    public int getFoliageColor() {
        double d = Mth.clamp(this.getTemperature(), 0.0f, 1.0f);
        double e = Mth.clamp(this.getDownfall(), 0.0f, 1.0f);
        return FoliageColor.get(d, e);
    }

    public void buildSurfaceAt(Random random, ChunkAccess chunkAccess, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, long m) {
        this.surfaceBuilder.initNoise(m);
        this.surfaceBuilder.apply(random, chunkAccess, this, i, j, k, d, blockState, blockState2, l, m);
    }

    public BiomeTempCategory getTemperatureCategory() {
        if (this.biomeCategory == BiomeCategory.OCEAN) {
            return BiomeTempCategory.OCEAN;
        }
        if ((double)this.getTemperature() < 0.2) {
            return BiomeTempCategory.COLD;
        }
        if ((double)this.getTemperature() < 1.0) {
            return BiomeTempCategory.MEDIUM;
        }
        return BiomeTempCategory.WARM;
    }

    public final float getDepth() {
        return this.depth;
    }

    public final float getDownfall() {
        return this.downfall;
    }

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

    public BiomeSpecialEffects getSpecialEffects() {
        return this.specialEffects;
    }

    @Environment(value=EnvType.CLIENT)
    public final int getWaterColor() {
        return this.specialEffects.getWaterColor();
    }

    @Environment(value=EnvType.CLIENT)
    public final int getWaterFogColor() {
        return this.specialEffects.getWaterFogColor();
    }

    @Environment(value=EnvType.CLIENT)
    public Optional<AmbientParticleSettings> getAmbientParticle() {
        return this.specialEffects.getAmbientParticleSettings();
    }

    @Environment(value=EnvType.CLIENT)
    public Optional<SoundEvent> getAmbientLoop() {
        return this.specialEffects.getAmbientLoopSoundEvent();
    }

    @Environment(value=EnvType.CLIENT)
    public Optional<AmbientMoodSettings> getAmbientMood() {
        return this.specialEffects.getAmbientMoodSettings();
    }

    @Environment(value=EnvType.CLIENT)
    public Optional<AmbientAdditionsSettings> getAmbientAdditions() {
        return this.specialEffects.getAmbientAdditionsSettings();
    }

    @Environment(value=EnvType.CLIENT)
    public Optional<Music> getBackgroundMusic() {
        return this.specialEffects.getBackgroundMusic();
    }

    public final BiomeCategory getBiomeCategory() {
        return this.biomeCategory;
    }

    public ConfiguredSurfaceBuilder<?> getSurfaceBuilder() {
        return this.surfaceBuilder;
    }

    public SurfaceBuilderConfiguration getSurfaceBuilderConfig() {
        return this.surfaceBuilder.getSurfaceBuilderConfiguration();
    }

    public Stream<ClimateParameters> optimalParameters() {
        return this.optimalParameters.stream();
    }

    @Nullable
    public String getParent() {
        return this.parent;
    }

    public static class ClimateParameters {
        public static final Codec<ClimateParameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("temparature")).forGetter(climateParameters -> Float.valueOf(climateParameters.temperature)), ((MapCodec)Codec.FLOAT.fieldOf("humidity")).forGetter(climateParameters -> Float.valueOf(climateParameters.humidity)), ((MapCodec)Codec.FLOAT.fieldOf("altitude")).forGetter(climateParameters -> Float.valueOf(climateParameters.altitude)), ((MapCodec)Codec.FLOAT.fieldOf("weirdness")).forGetter(climateParameters -> Float.valueOf(climateParameters.weirdness)), ((MapCodec)Codec.FLOAT.fieldOf("offset")).forGetter(climateParameters -> Float.valueOf(climateParameters.offset))).apply((Applicative<ClimateParameters, ?>)instance, ClimateParameters::new));
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
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            ClimateParameters climateParameters = (ClimateParameters)object;
            if (Float.compare(climateParameters.temperature, this.temperature) != 0) {
                return false;
            }
            if (Float.compare(climateParameters.humidity, this.humidity) != 0) {
                return false;
            }
            if (Float.compare(climateParameters.altitude, this.altitude) != 0) {
                return false;
            }
            return Float.compare(climateParameters.weirdness, this.weirdness) == 0;
        }

        public int hashCode() {
            int i = this.temperature != 0.0f ? Float.floatToIntBits(this.temperature) : 0;
            i = 31 * i + (this.humidity != 0.0f ? Float.floatToIntBits(this.humidity) : 0);
            i = 31 * i + (this.altitude != 0.0f ? Float.floatToIntBits(this.altitude) : 0);
            i = 31 * i + (this.weirdness != 0.0f ? Float.floatToIntBits(this.weirdness) : 0);
            return i;
        }

        public float fitness(ClimateParameters climateParameters) {
            return (this.temperature - climateParameters.temperature) * (this.temperature - climateParameters.temperature) + (this.humidity - climateParameters.humidity) * (this.humidity - climateParameters.humidity) + (this.altitude - climateParameters.altitude) * (this.altitude - climateParameters.altitude) + (this.weirdness - climateParameters.weirdness) * (this.weirdness - climateParameters.weirdness) + (this.offset - climateParameters.offset) * (this.offset - climateParameters.offset);
        }
    }

    public static class BiomeBuilder {
        @Nullable
        private ConfiguredSurfaceBuilder<?> surfaceBuilder;
        @Nullable
        private Precipitation precipitation;
        @Nullable
        private BiomeCategory biomeCategory;
        @Nullable
        private Float depth;
        @Nullable
        private Float scale;
        @Nullable
        private Float temperature;
        @Nullable
        private Float downfall;
        @Nullable
        private String parent;
        @Nullable
        private List<ClimateParameters> optimalParameters;
        @Nullable
        private BiomeSpecialEffects specialEffects;

        public <SC extends SurfaceBuilderConfiguration> BiomeBuilder surfaceBuilder(SurfaceBuilder<SC> surfaceBuilder, SC surfaceBuilderConfiguration) {
            this.surfaceBuilder = new ConfiguredSurfaceBuilder<SC>(surfaceBuilder, surfaceBuilderConfiguration);
            return this;
        }

        public BiomeBuilder surfaceBuilder(ConfiguredSurfaceBuilder<?> configuredSurfaceBuilder) {
            this.surfaceBuilder = configuredSurfaceBuilder;
            return this;
        }

        public BiomeBuilder precipitation(Precipitation precipitation) {
            this.precipitation = precipitation;
            return this;
        }

        public BiomeBuilder biomeCategory(BiomeCategory biomeCategory) {
            this.biomeCategory = biomeCategory;
            return this;
        }

        public BiomeBuilder depth(float f) {
            this.depth = Float.valueOf(f);
            return this;
        }

        public BiomeBuilder scale(float f) {
            this.scale = Float.valueOf(f);
            return this;
        }

        public BiomeBuilder temperature(float f) {
            this.temperature = Float.valueOf(f);
            return this;
        }

        public BiomeBuilder downfall(float f) {
            this.downfall = Float.valueOf(f);
            return this;
        }

        public BiomeBuilder parent(@Nullable String string) {
            this.parent = string;
            return this;
        }

        public BiomeBuilder optimalParameters(List<ClimateParameters> list) {
            this.optimalParameters = list;
            return this;
        }

        public BiomeBuilder specialEffects(BiomeSpecialEffects biomeSpecialEffects) {
            this.specialEffects = biomeSpecialEffects;
            return this;
        }

        public String toString() {
            return "BiomeBuilder{\nsurfaceBuilder=" + this.surfaceBuilder + ",\nprecipitation=" + this.precipitation + ",\nbiomeCategory=" + this.biomeCategory + ",\ndepth=" + this.depth + ",\nscale=" + this.scale + ",\ntemperature=" + this.temperature + ",\ndownfall=" + this.downfall + ",\nspecialEffects=" + this.specialEffects + ",\nparent='" + this.parent + '\'' + "\n" + '}';
        }
    }

    public static class SpawnerData
    extends WeighedRandom.WeighedRandomItem {
        public static final Codec<SpawnerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Registry.ENTITY_TYPE.fieldOf("type")).forGetter(spawnerData -> spawnerData.type), ((MapCodec)Codec.INT.fieldOf("weight")).forGetter(spawnerData -> spawnerData.weight), ((MapCodec)Codec.INT.fieldOf("minCount")).forGetter(spawnerData -> spawnerData.minCount), ((MapCodec)Codec.INT.fieldOf("maxCount")).forGetter(spawnerData -> spawnerData.maxCount)).apply((Applicative<SpawnerData, ?>)instance, SpawnerData::new));
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

    public static class MobSpawnCost {
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

    public static enum Precipitation implements StringRepresentable
    {
        NONE("none"),
        RAIN("rain"),
        SNOW("snow");

        public static final Codec<Precipitation> CODEC;
        private static final Map<String, Precipitation> BY_NAME;
        private final String name;

        private Precipitation(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        public static Precipitation byName(String string) {
            return BY_NAME.get(string);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Precipitation::values, Precipitation::byName);
            BY_NAME = Arrays.stream(Precipitation.values()).collect(Collectors.toMap(Precipitation::getName, precipitation -> precipitation));
        }
    }

    public static enum BiomeCategory implements StringRepresentable
    {
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

        public static final Codec<BiomeCategory> CODEC;
        private static final Map<String, BiomeCategory> BY_NAME;
        private final String name;

        private BiomeCategory(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        public static BiomeCategory byName(String string) {
            return BY_NAME.get(string);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(BiomeCategory::values, BiomeCategory::byName);
            BY_NAME = Arrays.stream(BiomeCategory.values()).collect(Collectors.toMap(BiomeCategory::getName, biomeCategory -> biomeCategory));
        }
    }

    public static enum BiomeTempCategory {
        OCEAN("ocean"),
        COLD("cold"),
        MEDIUM("medium"),
        WARM("warm");

        private static final Map<String, BiomeTempCategory> BY_NAME;
        private final String name;

        private BiomeTempCategory(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        static {
            BY_NAME = Arrays.stream(BiomeTempCategory.values()).collect(Collectors.toMap(BiomeTempCategory::getName, biomeTempCategory -> biomeTempCategory));
        }
    }
}

