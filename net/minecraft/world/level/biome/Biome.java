/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.kinds.Applicative;
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
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
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
import org.jetbrains.annotations.Nullable;

public final class Biome {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final MapCodec<Biome> DIRECT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(ClimateSettings.CODEC.forGetter(biome -> biome.climateSettings), ((MapCodec)BiomeCategory.CODEC.fieldOf("category")).forGetter(biome -> biome.biomeCategory), ((MapCodec)Codec.FLOAT.fieldOf("depth")).forGetter(biome -> Float.valueOf(biome.depth)), ((MapCodec)Codec.FLOAT.fieldOf("scale")).forGetter(biome -> Float.valueOf(biome.scale)), ((MapCodec)BiomeSpecialEffects.CODEC.fieldOf("effects")).forGetter(biome -> biome.specialEffects), WorldGenerationSettings.CODEC.forGetter(biome -> biome.worldGenerationSettings), MobSettings.CODEC.forGetter(biome -> biome.mobSettings), Codec.STRING.optionalFieldOf("parent").forGetter(biome -> Optional.ofNullable(biome.parent))).apply((Applicative<Biome, ?>)instance, Biome::new));
    public static final Codec<Supplier<Biome>> CODEC = RegistryFileCodec.create(Registry.BIOME_REGISTRY, DIRECT_CODEC);
    public static final Set<Biome> EXPLORABLE_BIOMES = Sets.newHashSet();
    private static final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(1234L), ImmutableList.of(Integer.valueOf(0)));
    private static final PerlinSimplexNoise FROZEN_TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(3456L), ImmutableList.of(Integer.valueOf(-2), Integer.valueOf(-1), Integer.valueOf(0)));
    public static final PerlinSimplexNoise BIOME_INFO_NOISE = new PerlinSimplexNoise(new WorldgenRandom(2345L), ImmutableList.of(Integer.valueOf(0)));
    private final ClimateSettings climateSettings;
    private final WorldGenerationSettings worldGenerationSettings;
    private final MobSettings mobSettings;
    private final float depth;
    private final float scale;
    @Nullable
    protected final String parent;
    private final BiomeCategory biomeCategory;
    private final BiomeSpecialEffects specialEffects;
    private final List<ConfiguredFeature<?, ?>> flowerFeatures;
    private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache = ThreadLocal.withInitial(() -> Util.make(() -> {
        Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(1024, 0.25f){

            @Override
            protected void rehash(int i) {
            }
        };
        long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
        return long2FloatLinkedOpenHashMap;
    }));

    public Biome(BiomeBuilder biomeBuilder) {
        if (biomeBuilder.surfaceBuilder == null || biomeBuilder.precipitation == null || biomeBuilder.biomeCategory == null || biomeBuilder.depth == null || biomeBuilder.scale == null || biomeBuilder.temperature == null || biomeBuilder.downfall == null || biomeBuilder.specialEffects == null) {
            throw new IllegalStateException("You are missing parameters to build a proper biome for " + this.getClass().getSimpleName() + "\n" + biomeBuilder);
        }
        this.climateSettings = new ClimateSettings(biomeBuilder.precipitation, biomeBuilder.temperature.floatValue(), biomeBuilder.temperatureModifier, biomeBuilder.downfall.floatValue());
        this.biomeCategory = biomeBuilder.biomeCategory;
        this.depth = biomeBuilder.depth.floatValue();
        this.scale = biomeBuilder.scale.floatValue();
        this.parent = biomeBuilder.parent;
        this.specialEffects = biomeBuilder.specialEffects;
        this.mobSettings = new MobSettings(biomeBuilder.creatureGenerationProbability);
        this.worldGenerationSettings = new WorldGenerationSettings(biomeBuilder.surfaceBuilder);
        this.flowerFeatures = Lists.newArrayList();
    }

    private Biome(ClimateSettings climateSettings, BiomeCategory biomeCategory, float f, float g, BiomeSpecialEffects biomeSpecialEffects, WorldGenerationSettings worldGenerationSettings, MobSettings mobSettings, Optional<String> optional) {
        this.climateSettings = climateSettings;
        this.worldGenerationSettings = worldGenerationSettings;
        this.mobSettings = mobSettings;
        this.biomeCategory = biomeCategory;
        this.depth = f;
        this.scale = g;
        this.specialEffects = biomeSpecialEffects;
        this.parent = optional.orElse(null);
        this.flowerFeatures = worldGenerationSettings.features.stream().flatMap(Collection::stream).map(Supplier::get).flatMap(ConfiguredFeature::getFeatures).filter(configuredFeature -> configuredFeature.feature == Feature.FLOWER).collect(Collectors.toList());
    }

    public boolean isMutated() {
        return this.parent != null;
    }

    @Environment(value=EnvType.CLIENT)
    public int getSkyColor() {
        return this.specialEffects.getSkyColor();
    }

    public void addSpawn(MobCategory mobCategory, SpawnerData spawnerData) {
        ((List)this.mobSettings.spawners.get(mobCategory)).add(spawnerData);
    }

    public void addMobCharge(EntityType<?> entityType, double d, double e) {
        this.mobSettings.mobSpawnCosts.put(entityType, new MobSpawnCost(e, d));
    }

    public List<SpawnerData> getMobs(MobCategory mobCategory) {
        return (List)this.mobSettings.spawners.get(mobCategory);
    }

    @Nullable
    public MobSpawnCost getMobSpawnCost(EntityType<?> entityType) {
        return (MobSpawnCost)this.mobSettings.mobSpawnCosts.get(entityType);
    }

    public Precipitation getPrecipitation() {
        return this.climateSettings.precipitation;
    }

    public boolean isHumid() {
        return this.getDownfall() > 0.85f;
    }

    public float getCreatureProbability() {
        return this.mobSettings.creatureGenerationProbability;
    }

    private float getHeightAdjustedTemperature(BlockPos blockPos) {
        float f = this.climateSettings.temperatureModifier.modifyTemperature(blockPos, this.getBaseTemperature());
        if (blockPos.getY() > 64) {
            float g = (float)(TEMPERATURE_NOISE.getValue((float)blockPos.getX() / 8.0f, (float)blockPos.getZ() / 8.0f, false) * 4.0);
            return f - (g + (float)blockPos.getY() - 64.0f) * 0.05f / 30.0f;
        }
        return f;
    }

    public final float getTemperature(BlockPos blockPos) {
        long l = blockPos.asLong();
        Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = this.temperatureCache.get();
        float f = long2FloatLinkedOpenHashMap.get(l);
        if (!Float.isNaN(f)) {
            return f;
        }
        float g = this.getHeightAdjustedTemperature(blockPos);
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
        this.addFeature(decoration.ordinal(), () -> configuredFeature);
    }

    public void addFeature(int i, Supplier<ConfiguredFeature<?, ?>> supplier) {
        supplier.get().getFeatures().filter(configuredFeature -> configuredFeature.feature == Feature.FLOWER).forEach(this.flowerFeatures::add);
        this.addFeatureStepsUpTo(i);
        ((List)this.worldGenerationSettings.features.get(i)).add(supplier);
    }

    public <C extends CarverConfiguration> void addCarver(GenerationStep.Carving carving2, ConfiguredWorldCarver<C> configuredWorldCarver) {
        this.worldGenerationSettings.carvers.computeIfAbsent(carving2, carving -> Lists.newArrayList()).add(() -> configuredWorldCarver);
    }

    public List<Supplier<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving carving) {
        return this.worldGenerationSettings.carvers.getOrDefault(carving, ImmutableList.of());
    }

    public void addStructureStart(ConfiguredStructureFeature<?, ?> configuredStructureFeature) {
        this.worldGenerationSettings.structureStarts.add(() -> configuredStructureFeature);
        this.addFeatureStepsUpTo(((StructureFeature)configuredStructureFeature.feature).step().ordinal());
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
        return DataFixUtils.orElse(this.worldGenerationSettings.structureStarts.stream().map(Supplier::get).filter(configuredStructureFeature2 -> configuredStructureFeature2.feature == configuredStructureFeature.feature).findAny(), configuredStructureFeature);
    }

    public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
        return this.flowerFeatures;
    }

    public List<List<Supplier<ConfiguredFeature<?, ?>>>> features() {
        return this.worldGenerationSettings.features;
    }

    public void generate(StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, WorldGenRegion worldGenRegion, long l, WorldgenRandom worldgenRandom, BlockPos blockPos) {
        for (int i = 0; i < this.worldGenerationSettings.features.size(); ++i) {
            int j = 0;
            if (structureFeatureManager.shouldGenerateFeatures()) {
                for (StructureFeature structureFeature : Registry.STRUCTURE_FEATURE) {
                    if (structureFeature.step().ordinal() != i) continue;
                    worldgenRandom.setFeatureSeed(l, j, i);
                    int k = blockPos.getX() >> 4;
                    int m = blockPos.getZ() >> 4;
                    int n = k << 4;
                    int o = m << 4;
                    try {
                        structureFeatureManager.startsForFeature(SectionPos.of(blockPos), structureFeature).forEach(structureStart -> structureStart.placeInChunk(worldGenRegion, structureFeatureManager, chunkGenerator, worldgenRandom, new BoundingBox(n, o, n + 15, o + 15), new ChunkPos(k, m)));
                    } catch (Exception exception) {
                        CrashReport crashReport = CrashReport.forThrowable(exception, "Feature placement");
                        crashReport.addCategory("Feature").setDetail("Id", Registry.STRUCTURE_FEATURE.getKey(structureFeature)).setDetail("Description", () -> structureFeature.toString());
                        throw new ReportedException(crashReport);
                    }
                    ++j;
                }
            }
            for (Supplier supplier : (List)this.worldGenerationSettings.features.get(i)) {
                ConfiguredFeature configuredFeature = (ConfiguredFeature)supplier.get();
                worldgenRandom.setFeatureSeed(l, j, i);
                try {
                    configuredFeature.place(worldGenRegion, chunkGenerator, worldgenRandom, blockPos);
                } catch (Exception exception2) {
                    CrashReport crashReport2 = CrashReport.forThrowable(exception2, "Feature placement");
                    crashReport2.addCategory("Feature").setDetail("Id", Registry.FEATURE.getKey((Feature<?>)configuredFeature.feature)).setDetail("Config", configuredFeature.config).setDetail("Description", () -> configuredFeature.feature.toString());
                    throw new ReportedException(crashReport2);
                }
                ++j;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public int getFogColor() {
        return this.specialEffects.getFogColor();
    }

    @Environment(value=EnvType.CLIENT)
    public int getGrassColor(double d, double e) {
        int i = this.specialEffects.getGrassColorOverride().orElseGet(this::getGrassColorFromTexture);
        return this.specialEffects.getGrassColorModifier().modifyColor(d, e, i);
    }

    @Environment(value=EnvType.CLIENT)
    private int getGrassColorFromTexture() {
        double d = Mth.clamp(this.climateSettings.temperature, 0.0f, 1.0f);
        double e = Mth.clamp(this.climateSettings.downfall, 0.0f, 1.0f);
        return GrassColor.get(d, e);
    }

    @Environment(value=EnvType.CLIENT)
    public int getFoliageColor() {
        return this.specialEffects.getFoliageColorOverride().orElseGet(this::getFoliageColorFromTexture);
    }

    @Environment(value=EnvType.CLIENT)
    private int getFoliageColorFromTexture() {
        double d = Mth.clamp(this.climateSettings.temperature, 0.0f, 1.0f);
        double e = Mth.clamp(this.climateSettings.downfall, 0.0f, 1.0f);
        return FoliageColor.get(d, e);
    }

    public void buildSurfaceAt(Random random, ChunkAccess chunkAccess, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, long m) {
        ConfiguredSurfaceBuilder configuredSurfaceBuilder = (ConfiguredSurfaceBuilder)this.worldGenerationSettings.surfaceBuilder.get();
        configuredSurfaceBuilder.initNoise(m);
        configuredSurfaceBuilder.apply(random, chunkAccess, this, i, j, k, d, blockState, blockState2, l, m);
    }

    public BiomeTempCategory getTemperatureCategory() {
        if (this.biomeCategory == BiomeCategory.OCEAN) {
            return BiomeTempCategory.OCEAN;
        }
        if ((double)this.getBaseTemperature() < 0.2) {
            return BiomeTempCategory.COLD;
        }
        if ((double)this.getBaseTemperature() < 1.0) {
            return BiomeTempCategory.MEDIUM;
        }
        return BiomeTempCategory.WARM;
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

    static class ClimateSettings {
        public static final MapCodec<ClimateSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Precipitation.CODEC.fieldOf("precipitation")).forGetter(climateSettings -> climateSettings.precipitation), ((MapCodec)Codec.FLOAT.fieldOf("temperature")).forGetter(climateSettings -> Float.valueOf(climateSettings.temperature)), TemperatureModifier.CODEC.optionalFieldOf("temperature_modifier", TemperatureModifier.NONE).forGetter(climateSettings -> climateSettings.temperatureModifier), ((MapCodec)Codec.FLOAT.fieldOf("downfall")).forGetter(climateSettings -> Float.valueOf(climateSettings.downfall))).apply((Applicative<ClimateSettings, ?>)instance, ClimateSettings::new));
        private final Precipitation precipitation;
        private final float temperature;
        private final TemperatureModifier temperatureModifier;
        private final float downfall;

        private ClimateSettings(Precipitation precipitation, float f, TemperatureModifier temperatureModifier, float g) {
            this.precipitation = precipitation;
            this.temperature = f;
            this.temperatureModifier = temperatureModifier;
            this.downfall = g;
        }
    }

    static class WorldGenerationSettings {
        public static final MapCodec<WorldGenerationSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)ConfiguredSurfaceBuilder.CODEC.fieldOf("surface_builder")).forGetter(worldGenerationSettings -> worldGenerationSettings.surfaceBuilder), Codec.simpleMap(GenerationStep.Carving.CODEC, ConfiguredWorldCarver.CODEC.listOf().promotePartial((Consumer)Util.prefix("Carver: ", LOGGER::error)), StringRepresentable.keys(GenerationStep.Carving.values())).fieldOf("carvers").forGetter(worldGenerationSettings -> worldGenerationSettings.carvers), ((MapCodec)ConfiguredFeature.CODEC.listOf().promotePartial((Consumer)Util.prefix("Feature: ", LOGGER::error)).listOf().fieldOf("features")).forGetter(worldGenerationSettings -> worldGenerationSettings.features), ((MapCodec)ConfiguredStructureFeature.CODEC.listOf().promotePartial((Consumer)Util.prefix("Structure start: ", LOGGER::error)).fieldOf("starts")).forGetter(worldGenerationSettings -> worldGenerationSettings.structureStarts)).apply((Applicative<WorldGenerationSettings, ?>)instance, WorldGenerationSettings::new));
        private final Supplier<ConfiguredSurfaceBuilder<?>> surfaceBuilder;
        private final Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> carvers;
        private final List<List<Supplier<ConfiguredFeature<?, ?>>>> features;
        private final List<Supplier<ConfiguredStructureFeature<?, ?>>> structureStarts;

        private WorldGenerationSettings(Supplier<ConfiguredSurfaceBuilder<?>> supplier, Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> map, List<List<Supplier<ConfiguredFeature<?, ?>>>> list, List<Supplier<ConfiguredStructureFeature<?, ?>>> list2) {
            this.surfaceBuilder = supplier;
            this.carvers = map;
            this.features = list;
            this.structureStarts = list2;
        }

        private WorldGenerationSettings(Supplier<ConfiguredSurfaceBuilder<?>> supplier) {
            this(supplier, Maps.newLinkedHashMap(), Lists.newArrayList(), Lists.newArrayList());
        }
    }

    static class MobSettings {
        public static final MapCodec<MobSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.FLOAT.optionalFieldOf("creature_spawn_probability", Float.valueOf(0.1f)).forGetter(mobSettings -> Float.valueOf(mobSettings.creatureGenerationProbability)), Codec.simpleMap(MobCategory.CODEC, SpawnerData.CODEC.listOf().promotePartial((Consumer)Util.prefix("Spawn data: ", LOGGER::error)), StringRepresentable.keys(MobCategory.values())).fieldOf("spawners").forGetter(mobSettings -> mobSettings.spawners), Codec.simpleMap(Registry.ENTITY_TYPE, MobSpawnCost.CODEC, Registry.ENTITY_TYPE).fieldOf("spawn_costs").forGetter(mobSettings -> mobSettings.mobSpawnCosts)).apply((Applicative<MobSettings, ?>)instance, MobSettings::new));
        private final float creatureGenerationProbability;
        private final Map<MobCategory, List<SpawnerData>> spawners;
        private final Map<EntityType<?>, MobSpawnCost> mobSpawnCosts;

        private MobSettings(float f, Map<MobCategory, List<SpawnerData>> map, Map<EntityType<?>, MobSpawnCost> map2) {
            this.creatureGenerationProbability = f;
            this.spawners = map;
            this.mobSpawnCosts = map2;
        }

        private MobSettings(float f) {
            this(f, Maps.newLinkedHashMap(), Maps.newLinkedHashMap());
            for (MobCategory mobCategory : MobCategory.values()) {
                this.spawners.put(mobCategory, Lists.newArrayList());
            }
        }
    }

    public static class ClimateParameters {
        public static final Codec<ClimateParameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.floatRange(-2.0f, 2.0f).fieldOf("temperature")).forGetter(climateParameters -> Float.valueOf(climateParameters.temperature)), ((MapCodec)Codec.floatRange(-2.0f, 2.0f).fieldOf("humidity")).forGetter(climateParameters -> Float.valueOf(climateParameters.humidity)), ((MapCodec)Codec.floatRange(-2.0f, 2.0f).fieldOf("altitude")).forGetter(climateParameters -> Float.valueOf(climateParameters.altitude)), ((MapCodec)Codec.floatRange(-2.0f, 2.0f).fieldOf("weirdness")).forGetter(climateParameters -> Float.valueOf(climateParameters.weirdness)), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("offset")).forGetter(climateParameters -> Float.valueOf(climateParameters.offset))).apply((Applicative<ClimateParameters, ?>)instance, ClimateParameters::new));
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
        private Supplier<ConfiguredSurfaceBuilder<?>> surfaceBuilder;
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
        private TemperatureModifier temperatureModifier = TemperatureModifier.NONE;
        @Nullable
        private Float downfall;
        @Nullable
        private String parent;
        @Nullable
        private BiomeSpecialEffects specialEffects;
        private float creatureGenerationProbability = 0.1f;

        public BiomeBuilder surfaceBuilder(ConfiguredSurfaceBuilder<?> configuredSurfaceBuilder) {
            return this.surfaceBuilder(() -> configuredSurfaceBuilder);
        }

        public BiomeBuilder surfaceBuilder(Supplier<ConfiguredSurfaceBuilder<?>> supplier) {
            this.surfaceBuilder = supplier;
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

        public BiomeBuilder specialEffects(BiomeSpecialEffects biomeSpecialEffects) {
            this.specialEffects = biomeSpecialEffects;
            return this;
        }

        public BiomeBuilder creatureGenerationProbability(float f) {
            this.creatureGenerationProbability = f;
            return this;
        }

        public BiomeBuilder temperatureAdjustment(TemperatureModifier temperatureModifier) {
            this.temperatureModifier = temperatureModifier;
            return this;
        }

        public String toString() {
            return "BiomeBuilder{\nsurfaceBuilder=" + this.surfaceBuilder + ",\nprecipitation=" + this.precipitation + ",\nbiomeCategory=" + this.biomeCategory + ",\ndepth=" + this.depth + ",\nscale=" + this.scale + ",\ntemperature=" + this.temperature + ",\ntemperatureModifier=" + this.temperatureModifier + ",\ndownfall=" + this.downfall + ",\nspecialEffects=" + this.specialEffects + ",\ncreatureGenerationProbability=" + this.creatureGenerationProbability + ",\nparent='" + this.parent + '\'' + "\n" + '}';
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

    public static enum TemperatureModifier implements StringRepresentable
    {
        NONE("none"){

            @Override
            public float modifyTemperature(BlockPos blockPos, float f) {
                return f;
            }
        }
        ,
        FROZEN("frozen"){

            @Override
            public float modifyTemperature(BlockPos blockPos, float f) {
                double h;
                double e;
                double d = FROZEN_TEMPERATURE_NOISE.getValue((double)blockPos.getX() * 0.05, (double)blockPos.getZ() * 0.05, false) * 7.0;
                double g = d + (e = BIOME_INFO_NOISE.getValue((double)blockPos.getX() * 0.2, (double)blockPos.getZ() * 0.2, false));
                if (g < 0.3 && (h = BIOME_INFO_NOISE.getValue((double)blockPos.getX() * 0.09, (double)blockPos.getZ() * 0.09, false)) < 0.8) {
                    return 0.2f;
                }
                return f;
            }
        };

        private final String name;
        public static final Codec<TemperatureModifier> CODEC;
        private static final Map<String, TemperatureModifier> BY_NAME;

        public abstract float modifyTemperature(BlockPos var1, float var2);

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

        public static TemperatureModifier byName(String string) {
            return BY_NAME.get(string);
        }

        static {
            CODEC = StringRepresentable.fromEnum(TemperatureModifier::values, TemperatureModifier::byName);
            BY_NAME = Arrays.stream(TemperatureModifier.values()).collect(Collectors.toMap(TemperatureModifier::getName, temperatureModifier -> temperatureModifier));
        }
    }

    public static class MobSpawnCost {
        public static final Codec<MobSpawnCost> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.DOUBLE.fieldOf("energy_budget")).forGetter(MobSpawnCost::getEnergyBudget), ((MapCodec)Codec.DOUBLE.fieldOf("charge")).forGetter(MobSpawnCost::getCharge)).apply((Applicative<MobSpawnCost, ?>)instance, MobSpawnCost::new));
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

