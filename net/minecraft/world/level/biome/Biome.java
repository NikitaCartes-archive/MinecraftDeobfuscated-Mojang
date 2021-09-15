/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public final class Biome {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Codec<Biome> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(ClimateSettings.CODEC.forGetter(biome -> biome.climateSettings), ((MapCodec)BiomeCategory.CODEC.fieldOf("category")).forGetter(biome -> biome.biomeCategory), ((MapCodec)BiomeSpecialEffects.CODEC.fieldOf("effects")).forGetter(biome -> biome.specialEffects), BiomeGenerationSettings.CODEC.forGetter(biome -> biome.generationSettings), MobSpawnSettings.CODEC.forGetter(biome -> biome.mobSettings)).apply((Applicative<Biome, ?>)instance, Biome::new));
    public static final Codec<Biome> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(ClimateSettings.CODEC.forGetter(biome -> biome.climateSettings), ((MapCodec)BiomeCategory.CODEC.fieldOf("category")).forGetter(biome -> biome.biomeCategory), ((MapCodec)BiomeSpecialEffects.CODEC.fieldOf("effects")).forGetter(biome -> biome.specialEffects)).apply((Applicative<Biome, ?>)instance, (climateSettings, biomeCategory, biomeSpecialEffects) -> new Biome((ClimateSettings)climateSettings, (BiomeCategory)biomeCategory, (BiomeSpecialEffects)biomeSpecialEffects, BiomeGenerationSettings.EMPTY, MobSpawnSettings.EMPTY)));
    public static final Codec<Supplier<Biome>> CODEC = RegistryFileCodec.create(Registry.BIOME_REGISTRY, DIRECT_CODEC);
    public static final Codec<List<Supplier<Biome>>> LIST_CODEC = RegistryFileCodec.homogeneousList(Registry.BIOME_REGISTRY, DIRECT_CODEC);
    private static final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise((RandomSource)new WorldgenRandom(1234L), ImmutableList.of(Integer.valueOf(0)));
    static final PerlinSimplexNoise FROZEN_TEMPERATURE_NOISE = new PerlinSimplexNoise((RandomSource)new WorldgenRandom(3456L), ImmutableList.of(Integer.valueOf(-2), Integer.valueOf(-1), Integer.valueOf(0)));
    public static final PerlinSimplexNoise BIOME_INFO_NOISE = new PerlinSimplexNoise((RandomSource)new WorldgenRandom(2345L), ImmutableList.of(Integer.valueOf(0)));
    private static final int TEMPERATURE_CACHE_SIZE = 1024;
    private final ClimateSettings climateSettings;
    private final BiomeGenerationSettings generationSettings;
    private final MobSpawnSettings mobSettings;
    private final BiomeCategory biomeCategory;
    private final BiomeSpecialEffects specialEffects;
    private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache = ThreadLocal.withInitial(() -> Util.make(() -> {
        Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(1024, 0.25f){

            @Override
            protected void rehash(int i) {
            }
        };
        long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
        return long2FloatLinkedOpenHashMap;
    }));

    Biome(ClimateSettings climateSettings, BiomeCategory biomeCategory, BiomeSpecialEffects biomeSpecialEffects, BiomeGenerationSettings biomeGenerationSettings, MobSpawnSettings mobSpawnSettings) {
        this.climateSettings = climateSettings;
        this.generationSettings = biomeGenerationSettings;
        this.mobSettings = mobSpawnSettings;
        this.biomeCategory = biomeCategory;
        this.specialEffects = biomeSpecialEffects;
    }

    public int getSkyColor() {
        return this.specialEffects.getSkyColor();
    }

    public MobSpawnSettings getMobSettings() {
        return this.mobSettings;
    }

    public Precipitation getPrecipitation() {
        return this.climateSettings.precipitation;
    }

    public boolean isHumid() {
        return this.getDownfall() > 0.85f;
    }

    private float getHeightAdjustedTemperature(BlockPos blockPos) {
        float f = this.climateSettings.temperatureModifier.modifyTemperature(blockPos, this.getBaseTemperature());
        if (blockPos.getY() > 80) {
            float g = (float)(TEMPERATURE_NOISE.getValue((float)blockPos.getX() / 8.0f, (float)blockPos.getZ() / 8.0f, false) * 8.0);
            return f - (g + (float)blockPos.getY() - 80.0f) * 0.05f / 40.0f;
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
        if (blockPos.getY() >= levelReader.getMinBuildHeight() && blockPos.getY() < levelReader.getMaxBuildHeight() && levelReader.getBrightness(LightLayer.BLOCK, blockPos) < 10) {
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

    public boolean isColdEnoughToSnow(BlockPos blockPos) {
        return this.getTemperature(blockPos) < 0.15f;
    }

    public boolean shouldSnow(LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState;
        if (!this.isColdEnoughToSnow(blockPos)) {
            return false;
        }
        return blockPos.getY() >= levelReader.getMinBuildHeight() && blockPos.getY() < levelReader.getMaxBuildHeight() && levelReader.getBrightness(LightLayer.BLOCK, blockPos) < 10 && (blockState = levelReader.getBlockState(blockPos)).isAir() && Blocks.SNOW.defaultBlockState().canSurvive(levelReader, blockPos);
    }

    public BiomeGenerationSettings getGenerationSettings() {
        return this.generationSettings;
    }

    public int getFogColor() {
        return this.specialEffects.getFogColor();
    }

    public int getGrassColor(double d, double e) {
        int i = this.specialEffects.getGrassColorOverride().orElseGet(this::getGrassColorFromTexture);
        return this.specialEffects.getGrassColorModifier().modifyColor(d, e, i);
    }

    private int getGrassColorFromTexture() {
        double d = Mth.clamp(this.climateSettings.temperature, 0.0f, 1.0f);
        double e = Mth.clamp(this.climateSettings.downfall, 0.0f, 1.0f);
        return GrassColor.get(d, e);
    }

    public int getFoliageColor() {
        return this.specialEffects.getFoliageColorOverride().orElseGet(this::getFoliageColorFromTexture);
    }

    private int getFoliageColorFromTexture() {
        double d = Mth.clamp(this.climateSettings.temperature, 0.0f, 1.0f);
        double e = Mth.clamp(this.climateSettings.downfall, 0.0f, 1.0f);
        return FoliageColor.get(d, e);
    }

    public void buildSurfaceAt(Random random, BlockColumn blockColumn, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, int m, long n) {
        ConfiguredSurfaceBuilder<?> configuredSurfaceBuilder = this.generationSettings.getSurfaceBuilder().get();
        configuredSurfaceBuilder.initNoise(n);
        configuredSurfaceBuilder.apply(random, blockColumn, this, i, j, k, d, blockState, blockState2, l, m, n);
    }

    public final float getDownfall() {
        return this.climateSettings.downfall;
    }

    public final float getBaseTemperature() {
        return this.climateSettings.temperature;
    }

    public BiomeSpecialEffects getSpecialEffects() {
        return this.specialEffects;
    }

    public final int getWaterColor() {
        return this.specialEffects.getWaterColor();
    }

    public final int getWaterFogColor() {
        return this.specialEffects.getWaterFogColor();
    }

    public Optional<AmbientParticleSettings> getAmbientParticle() {
        return this.specialEffects.getAmbientParticleSettings();
    }

    public Optional<SoundEvent> getAmbientLoop() {
        return this.specialEffects.getAmbientLoopSoundEvent();
    }

    public Optional<AmbientMoodSettings> getAmbientMood() {
        return this.specialEffects.getAmbientMoodSettings();
    }

    public Optional<AmbientAdditionsSettings> getAmbientAdditions() {
        return this.specialEffects.getAmbientAdditionsSettings();
    }

    public Optional<Music> getBackgroundMusic() {
        return this.specialEffects.getBackgroundMusic();
    }

    public final BiomeCategory getBiomeCategory() {
        return this.biomeCategory;
    }

    public String toString() {
        ResourceLocation resourceLocation = BuiltinRegistries.BIOME.getKey(this);
        return resourceLocation == null ? super.toString() : resourceLocation.toString();
    }

    static class ClimateSettings {
        public static final MapCodec<ClimateSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Precipitation.CODEC.fieldOf("precipitation")).forGetter(climateSettings -> climateSettings.precipitation), ((MapCodec)Codec.FLOAT.fieldOf("temperature")).forGetter(climateSettings -> Float.valueOf(climateSettings.temperature)), TemperatureModifier.CODEC.optionalFieldOf("temperature_modifier", TemperatureModifier.NONE).forGetter(climateSettings -> climateSettings.temperatureModifier), ((MapCodec)Codec.FLOAT.fieldOf("downfall")).forGetter(climateSettings -> Float.valueOf(climateSettings.downfall))).apply((Applicative<ClimateSettings, ?>)instance, ClimateSettings::new));
        final Precipitation precipitation;
        final float temperature;
        final TemperatureModifier temperatureModifier;
        final float downfall;

        ClimateSettings(Precipitation precipitation, float f, TemperatureModifier temperatureModifier, float g) {
            this.precipitation = precipitation;
            this.temperature = f;
            this.temperatureModifier = temperatureModifier;
            this.downfall = g;
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
        NETHER("nether"),
        UNDERGROUND("underground"),
        MOUNTAIN("mountain");

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

        TemperatureModifier(String string2) {
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

    public static class BiomeBuilder {
        @Nullable
        private Precipitation precipitation;
        @Nullable
        private BiomeCategory biomeCategory;
        @Nullable
        private Float temperature;
        private TemperatureModifier temperatureModifier = TemperatureModifier.NONE;
        @Nullable
        private Float downfall;
        @Nullable
        private BiomeSpecialEffects specialEffects;
        @Nullable
        private MobSpawnSettings mobSpawnSettings;
        @Nullable
        private BiomeGenerationSettings generationSettings;

        public BiomeBuilder precipitation(Precipitation precipitation) {
            this.precipitation = precipitation;
            return this;
        }

        public BiomeBuilder biomeCategory(BiomeCategory biomeCategory) {
            this.biomeCategory = biomeCategory;
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

        public BiomeBuilder specialEffects(BiomeSpecialEffects biomeSpecialEffects) {
            this.specialEffects = biomeSpecialEffects;
            return this;
        }

        public BiomeBuilder mobSpawnSettings(MobSpawnSettings mobSpawnSettings) {
            this.mobSpawnSettings = mobSpawnSettings;
            return this;
        }

        public BiomeBuilder generationSettings(BiomeGenerationSettings biomeGenerationSettings) {
            this.generationSettings = biomeGenerationSettings;
            return this;
        }

        public BiomeBuilder temperatureAdjustment(TemperatureModifier temperatureModifier) {
            this.temperatureModifier = temperatureModifier;
            return this;
        }

        public Biome build() {
            if (this.precipitation == null || this.biomeCategory == null || this.temperature == null || this.downfall == null || this.specialEffects == null || this.mobSpawnSettings == null || this.generationSettings == null) {
                throw new IllegalStateException("You are missing parameters to build a proper biome\n" + this);
            }
            return new Biome(new ClimateSettings(this.precipitation, this.temperature.floatValue(), this.temperatureModifier, this.downfall.floatValue()), this.biomeCategory, this.specialEffects, this.generationSettings, this.mobSpawnSettings);
        }

        public String toString() {
            return "BiomeBuilder{\nprecipitation=" + this.precipitation + ",\nbiomeCategory=" + this.biomeCategory + ",\ntemperature=" + this.temperature + ",\ntemperatureModifier=" + this.temperatureModifier + ",\ndownfall=" + this.downfall + ",\nspecialEffects=" + this.specialEffects + ",\nmobSpawnSettings=" + this.mobSpawnSettings + ",\ngenerationSettings=" + this.generationSettings + ",\n}";
        }
    }
}

