/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.dimension;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.File;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Codecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetBiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetConstantColumnBiomeZoomer;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class DimensionType {
    public static final MapCodec<DimensionType> DIRECT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.LONG.optionalFieldOf("fixed_time").xmap(optional -> optional.map(OptionalLong::of).orElseGet(OptionalLong::empty), optionalLong -> optionalLong.isPresent() ? Optional.of(optionalLong.getAsLong()) : Optional.empty()).forGetter(dimensionType -> dimensionType.fixedTime), ((MapCodec)Codec.BOOL.fieldOf("has_skylight")).forGetter(DimensionType::hasSkyLight), ((MapCodec)Codec.BOOL.fieldOf("has_ceiling")).forGetter(DimensionType::hasCeiling), ((MapCodec)Codec.BOOL.fieldOf("ultrawarm")).forGetter(DimensionType::ultraWarm), ((MapCodec)Codec.BOOL.fieldOf("natural")).forGetter(DimensionType::natural), ((MapCodec)Codec.BOOL.fieldOf("shrunk")).forGetter(DimensionType::shrunk), ((MapCodec)Codec.BOOL.fieldOf("piglin_safe")).forGetter(DimensionType::piglinSafe), ((MapCodec)Codec.BOOL.fieldOf("bed_works")).forGetter(DimensionType::bedWorks), ((MapCodec)Codec.BOOL.fieldOf("respawn_anchor_works")).forGetter(DimensionType::respawnAnchorWorks), ((MapCodec)Codec.BOOL.fieldOf("has_raids")).forGetter(DimensionType::hasRaids), ((MapCodec)Codecs.intRange(0, 256).fieldOf("logical_height")).forGetter(DimensionType::logicalHeight), ((MapCodec)ResourceLocation.CODEC.fieldOf("infiniburn")).forGetter(dimensionType -> dimensionType.infiniburn), ((MapCodec)Codec.FLOAT.fieldOf("ambient_light")).forGetter(dimensionType -> Float.valueOf(dimensionType.ambientLight))).apply((Applicative<DimensionType, ?>)instance, DimensionType::new));
    public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0f, 0.75f, 0.5f, 0.25f, 0.0f, 0.25f, 0.5f, 0.75f};
    public static final ResourceKey<DimensionType> OVERWORLD_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<DimensionType> NETHER_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_nether"));
    public static final ResourceKey<DimensionType> END_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_end"));
    protected static final DimensionType DEFAULT_OVERWORLD = new DimensionType(OptionalLong.empty(), true, false, false, true, false, false, false, true, false, true, 256, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE, BlockTags.INFINIBURN_OVERWORLD.getName(), 0.0f);
    protected static final DimensionType DEFAULT_NETHER = new DimensionType(OptionalLong.of(18000L), false, true, true, false, true, false, true, false, true, false, 128, FuzzyOffsetBiomeZoomer.INSTANCE, BlockTags.INFINIBURN_NETHER.getName(), 0.1f);
    protected static final DimensionType DEFAULT_END = new DimensionType(OptionalLong.of(6000L), false, false, false, false, false, true, false, false, false, true, 256, FuzzyOffsetBiomeZoomer.INSTANCE, BlockTags.INFINIBURN_END.getName(), 0.0f);
    public static final ResourceKey<DimensionType> OVERWORLD_CAVES_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld_caves"));
    protected static final DimensionType DEFAULT_OVERWORLD_CAVES = new DimensionType(OptionalLong.empty(), true, true, false, true, false, false, false, true, false, true, 256, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE, BlockTags.INFINIBURN_OVERWORLD.getName(), 0.0f);
    public static final Codec<Supplier<DimensionType>> CODEC = RegistryFileCodec.create(Registry.DIMENSION_TYPE_REGISTRY, DIRECT_CODEC);
    private final OptionalLong fixedTime;
    private final boolean hasSkylight;
    private final boolean hasCeiling;
    private final boolean ultraWarm;
    private final boolean natural;
    private final boolean shrunk;
    private final boolean createDragonFight;
    private final boolean piglinSafe;
    private final boolean bedWorks;
    private final boolean respawnAnchorWorks;
    private final boolean hasRaids;
    private final int logicalHeight;
    private final BiomeZoomer biomeZoomer;
    private final ResourceLocation infiniburn;
    private final float ambientLight;
    private final transient float[] brightnessRamp;

    public static DimensionType defaultOverworld() {
        return DEFAULT_OVERWORLD;
    }

    @Environment(value=EnvType.CLIENT)
    public static DimensionType defaultOverworldCaves() {
        return DEFAULT_OVERWORLD_CAVES;
    }

    protected DimensionType(OptionalLong optionalLong, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, boolean bl7, boolean bl8, boolean bl9, int i, ResourceLocation resourceLocation, float f) {
        this(optionalLong, bl, bl2, bl3, bl4, bl5, false, bl6, bl7, bl8, bl9, i, FuzzyOffsetBiomeZoomer.INSTANCE, resourceLocation, f);
    }

    protected DimensionType(OptionalLong optionalLong, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, boolean bl7, boolean bl8, boolean bl9, boolean bl10, int i, BiomeZoomer biomeZoomer, ResourceLocation resourceLocation, float f) {
        this.fixedTime = optionalLong;
        this.hasSkylight = bl;
        this.hasCeiling = bl2;
        this.ultraWarm = bl3;
        this.natural = bl4;
        this.shrunk = bl5;
        this.createDragonFight = bl6;
        this.piglinSafe = bl7;
        this.bedWorks = bl8;
        this.respawnAnchorWorks = bl9;
        this.hasRaids = bl10;
        this.logicalHeight = i;
        this.biomeZoomer = biomeZoomer;
        this.infiniburn = resourceLocation;
        this.ambientLight = f;
        this.brightnessRamp = DimensionType.fillBrightnessRamp(f);
    }

    private static float[] fillBrightnessRamp(float f) {
        float[] fs = new float[16];
        for (int i = 0; i <= 15; ++i) {
            float g = (float)i / 15.0f;
            float h = g / (4.0f - 3.0f * g);
            fs[i] = Mth.lerp(f, h, 1.0f);
        }
        return fs;
    }

    @Deprecated
    public static DataResult<ResourceKey<Level>> parseLegacy(Dynamic<?> dynamic) {
        Optional<Number> optional = dynamic.asNumber().result();
        if (optional.isPresent()) {
            int i = optional.get().intValue();
            if (i == -1) {
                return DataResult.success(Level.NETHER);
            }
            if (i == 0) {
                return DataResult.success(Level.OVERWORLD);
            }
            if (i == 1) {
                return DataResult.success(Level.END);
            }
        }
        return Level.RESOURCE_KEY_CODEC.parse(dynamic);
    }

    public static RegistryAccess.RegistryHolder registerBuiltin(RegistryAccess.RegistryHolder registryHolder) {
        registryHolder.registerDimension(OVERWORLD_LOCATION, DEFAULT_OVERWORLD);
        registryHolder.registerDimension(OVERWORLD_CAVES_LOCATION, DEFAULT_OVERWORLD_CAVES);
        registryHolder.registerDimension(NETHER_LOCATION, DEFAULT_NETHER);
        registryHolder.registerDimension(END_LOCATION, DEFAULT_END);
        return registryHolder;
    }

    private static ChunkGenerator defaultEndGenerator(long l) {
        return new NoiseBasedChunkGenerator(new TheEndBiomeSource(l), l, NoiseGeneratorSettings.Preset.END.settings());
    }

    private static ChunkGenerator defaultNetherGenerator(long l) {
        return new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.Preset.NETHER.biomeSource(l), l, NoiseGeneratorSettings.Preset.NETHER.settings());
    }

    public static MappedRegistry<LevelStem> defaultDimensions(long l) {
        MappedRegistry<LevelStem> mappedRegistry = new MappedRegistry<LevelStem>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
        mappedRegistry.register(LevelStem.NETHER, new LevelStem(() -> DEFAULT_NETHER, DimensionType.defaultNetherGenerator(l)));
        mappedRegistry.register(LevelStem.END, new LevelStem(() -> DEFAULT_END, DimensionType.defaultEndGenerator(l)));
        mappedRegistry.setPersistent(LevelStem.NETHER);
        mappedRegistry.setPersistent(LevelStem.END);
        return mappedRegistry;
    }

    @Deprecated
    public String getFileSuffix() {
        if (this == DEFAULT_END) {
            return "_end";
        }
        return "";
    }

    public static File getStorageFolder(ResourceKey<Level> resourceKey, File file) {
        if (resourceKey == Level.OVERWORLD) {
            return file;
        }
        if (resourceKey == Level.END) {
            return new File(file, "DIM1");
        }
        if (resourceKey == Level.NETHER) {
            return new File(file, "DIM-1");
        }
        return new File(file, "dimensions/" + resourceKey.location().getNamespace() + "/" + resourceKey.location().getPath());
    }

    public boolean hasSkyLight() {
        return this.hasSkylight;
    }

    public boolean hasCeiling() {
        return this.hasCeiling;
    }

    public boolean ultraWarm() {
        return this.ultraWarm;
    }

    public boolean natural() {
        return this.natural;
    }

    public boolean shrunk() {
        return this.shrunk;
    }

    public boolean piglinSafe() {
        return this.piglinSafe;
    }

    public boolean bedWorks() {
        return this.bedWorks;
    }

    public boolean respawnAnchorWorks() {
        return this.respawnAnchorWorks;
    }

    public boolean hasRaids() {
        return this.hasRaids;
    }

    public int logicalHeight() {
        return this.logicalHeight;
    }

    public boolean createDragonFight() {
        return this.createDragonFight;
    }

    public BiomeZoomer getBiomeZoomer() {
        return this.biomeZoomer;
    }

    public boolean hasFixedTime() {
        return this.fixedTime.isPresent();
    }

    public float timeOfDay(long l) {
        double d = Mth.frac((double)this.fixedTime.orElse(l) / 24000.0 - 0.25);
        double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
        return (float)(d * 2.0 + e) / 3.0f;
    }

    public int moonPhase(long l) {
        return (int)(l / 24000L % 8L + 8L) % 8;
    }

    public float brightness(int i) {
        return this.brightnessRamp[i];
    }

    public Tag<Block> infiniburn() {
        return BlockTags.getAllTags().getTag(this.infiniburn);
    }
}

