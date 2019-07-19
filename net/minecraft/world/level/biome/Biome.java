/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
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
import net.minecraft.world.level.levelgen.feature.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.FlowerFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public abstract class Biome {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Set<Biome> EXPLORABLE_BIOMES = Sets.newHashSet();
    public static final IdMapper<Biome> MUTATED_BIOMES = new IdMapper();
    protected static final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise(new Random(1234L), 1);
    public static final PerlinSimplexNoise BIOME_INFO_NOISE = new PerlinSimplexNoise(new Random(2345L), 1);
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
    protected final BiomeCategory biomeCategory;
    protected final Precipitation precipitation;
    protected final Map<GenerationStep.Carving, List<ConfiguredWorldCarver<?>>> carvers = Maps.newHashMap();
    protected final Map<GenerationStep.Decoration, List<ConfiguredFeature<?>>> features = Maps.newHashMap();
    protected final List<ConfiguredFeature<?>> flowerFeatures = Lists.newArrayList();
    protected final Map<StructureFeature<?>, FeatureConfiguration> validFeatureStarts = Maps.newHashMap();
    private final Map<MobCategory, List<SpawnerData>> spawners = Maps.newHashMap();
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

    public static <F extends FeatureConfiguration, D extends DecoratorConfiguration> ConfiguredFeature<?> makeComposite(Feature<F> feature, F featureConfiguration, FeatureDecorator<D> featureDecorator, D decoratorConfiguration) {
        Feature<DecoratedFeatureConfiguration> feature2 = feature instanceof FlowerFeature ? Feature.DECORATED_FLOWER : Feature.DECORATED;
        return new ConfiguredFeature<DecoratedFeatureConfiguration>(feature2, new DecoratedFeatureConfiguration(feature, featureConfiguration, featureDecorator, decoratorConfiguration));
    }

    protected Biome(BiomeBuilder biomeBuilder) {
        if (biomeBuilder.surfaceBuilder == null || biomeBuilder.precipitation == null || biomeBuilder.biomeCategory == null || biomeBuilder.depth == null || biomeBuilder.scale == null || biomeBuilder.temperature == null || biomeBuilder.downfall == null || biomeBuilder.waterColor == null || biomeBuilder.waterFogColor == null) {
            throw new IllegalStateException("You are missing parameters to build a proper biome for " + this.getClass().getSimpleName() + "\n" + biomeBuilder);
        }
        this.surfaceBuilder = biomeBuilder.surfaceBuilder;
        this.precipitation = biomeBuilder.precipitation;
        this.biomeCategory = biomeBuilder.biomeCategory;
        this.depth = biomeBuilder.depth.floatValue();
        this.scale = biomeBuilder.scale.floatValue();
        this.temperature = biomeBuilder.temperature.floatValue();
        this.downfall = biomeBuilder.downfall.floatValue();
        this.waterColor = biomeBuilder.waterColor;
        this.waterFogColor = biomeBuilder.waterFogColor;
        this.parent = biomeBuilder.parent;
        for (GenerationStep.Decoration decoration : GenerationStep.Decoration.values()) {
            this.features.put(decoration, Lists.newArrayList());
        }
        for (Enum enum_ : MobCategory.values()) {
            this.spawners.put((MobCategory)enum_, Lists.newArrayList());
        }
    }

    public boolean isMutated() {
        return this.parent != null;
    }

    @Environment(value=EnvType.CLIENT)
    public int getSkyColor(float f) {
        f /= 3.0f;
        f = Mth.clamp(f, -1.0f, 1.0f);
        return Mth.hsvToRgb(0.62222224f - f * 0.05f, 0.5f + f * 0.1f, 1.0f);
    }

    protected void addSpawn(MobCategory mobCategory, SpawnerData spawnerData) {
        this.spawners.get((Object)mobCategory).add(spawnerData);
    }

    public List<SpawnerData> getMobs(MobCategory mobCategory) {
        return this.spawners.get((Object)mobCategory);
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
            float f = (float)(TEMPERATURE_NOISE.getValue((float)blockPos.getX() / 8.0f, (float)blockPos.getZ() / 8.0f) * 4.0);
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

    public void addFeature(GenerationStep.Decoration decoration, ConfiguredFeature<?> configuredFeature) {
        if (configuredFeature.feature == Feature.DECORATED_FLOWER) {
            this.flowerFeatures.add(configuredFeature);
        }
        this.features.get((Object)decoration).add(configuredFeature);
    }

    public <C extends CarverConfiguration> void addCarver(GenerationStep.Carving carving2, ConfiguredWorldCarver<C> configuredWorldCarver) {
        this.carvers.computeIfAbsent(carving2, carving -> Lists.newArrayList()).add(configuredWorldCarver);
    }

    public List<ConfiguredWorldCarver<?>> getCarvers(GenerationStep.Carving carving2) {
        return this.carvers.computeIfAbsent(carving2, carving -> Lists.newArrayList());
    }

    public <C extends FeatureConfiguration> void addStructureStart(StructureFeature<C> structureFeature, C featureConfiguration) {
        this.validFeatureStarts.put(structureFeature, featureConfiguration);
    }

    public <C extends FeatureConfiguration> boolean isValidStart(StructureFeature<C> structureFeature) {
        return this.validFeatureStarts.containsKey(structureFeature);
    }

    @Nullable
    public <C extends FeatureConfiguration> C getStructureConfiguration(StructureFeature<C> structureFeature) {
        return (C)this.validFeatureStarts.get(structureFeature);
    }

    public List<ConfiguredFeature<?>> getFlowerFeatures() {
        return this.flowerFeatures;
    }

    public List<ConfiguredFeature<?>> getFeaturesForStep(GenerationStep.Decoration decoration) {
        return this.features.get((Object)decoration);
    }

    public void generate(GenerationStep.Decoration decoration, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, LevelAccessor levelAccessor, long l, WorldgenRandom worldgenRandom, BlockPos blockPos) {
        int i = 0;
        for (ConfiguredFeature<?> configuredFeature : this.features.get((Object)decoration)) {
            worldgenRandom.setFeatureSeed(l, i, decoration.ordinal());
            try {
                configuredFeature.place(levelAccessor, chunkGenerator, worldgenRandom, blockPos);
            } catch (Exception exception) {
                CrashReport crashReport = CrashReport.forThrowable(exception, "Feature placement");
                crashReport.addCategory("Feature").setDetail("Id", Registry.FEATURE.getKey(configuredFeature.feature)).setDetail("Description", configuredFeature.feature::toString);
                throw new ReportedException(crashReport);
            }
            ++i;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public int getGrassColor(BlockPos blockPos) {
        double d = Mth.clamp(this.getTemperature(blockPos), 0.0f, 1.0f);
        double e = Mth.clamp(this.getDownfall(), 0.0f, 1.0f);
        return GrassColor.get(d, e);
    }

    @Environment(value=EnvType.CLIENT)
    public int getFoliageColor(BlockPos blockPos) {
        double d = Mth.clamp(this.getTemperature(blockPos), 0.0f, 1.0f);
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

    @Environment(value=EnvType.CLIENT)
    public Component getName() {
        return new TranslatableComponent(this.getDescriptionId(), new Object[0]);
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

    public final BiomeCategory getBiomeCategory() {
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
        private Integer waterColor;
        @Nullable
        private Integer waterFogColor;
        @Nullable
        private String parent;

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

        public BiomeBuilder waterColor(int i) {
            this.waterColor = i;
            return this;
        }

        public BiomeBuilder waterFogColor(int i) {
            this.waterFogColor = i;
            return this;
        }

        public BiomeBuilder parent(@Nullable String string) {
            this.parent = string;
            return this;
        }

        public String toString() {
            return "BiomeBuilder{\nsurfaceBuilder=" + this.surfaceBuilder + ",\nprecipitation=" + (Object)((Object)this.precipitation) + ",\nbiomeCategory=" + (Object)((Object)this.biomeCategory) + ",\ndepth=" + this.depth + ",\nscale=" + this.scale + ",\ntemperature=" + this.temperature + ",\ndownfall=" + this.downfall + ",\nwaterColor=" + this.waterColor + ",\nwaterFogColor=" + this.waterFogColor + ",\nparent='" + this.parent + '\'' + "\n" + '}';
        }
    }

    public static class SpawnerData
    extends WeighedRandom.WeighedRandomItem {
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

    public static enum Precipitation {
        NONE("none"),
        RAIN("rain"),
        SNOW("snow");

        private static final Map<String, Precipitation> BY_NAME;
        private final String name;

        private Precipitation(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        static {
            BY_NAME = Arrays.stream(Precipitation.values()).collect(Collectors.toMap(Precipitation::getName, precipitation -> precipitation));
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

        private static final Map<String, BiomeCategory> BY_NAME;
        private final String name;

        private BiomeCategory(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        static {
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

