/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetBiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetConstantColumnBiomeZoomer;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class DimensionType {
    public static final Codec<ResourceKey<DimensionType>> RESOURCE_KEY_CODEC = ResourceLocation.CODEC.xmap(ResourceKey.elementKey(Registry.DIMENSION_TYPE_REGISTRY), ResourceKey::location);
    private static final Codec<DimensionType> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.LONG.optionalFieldOf("fixed_time").xmap(optional -> optional.map(OptionalLong::of).orElseGet(OptionalLong::empty), optionalLong -> optionalLong.isPresent() ? Optional.of(optionalLong.getAsLong()) : Optional.empty()).forGetter(dimensionType -> dimensionType.fixedTime), ((MapCodec)Codec.BOOL.fieldOf("has_skylight")).forGetter(DimensionType::hasSkyLight), ((MapCodec)Codec.BOOL.fieldOf("has_ceiling")).forGetter(DimensionType::hasCeiling), ((MapCodec)Codec.BOOL.fieldOf("ultrawarm")).forGetter(DimensionType::ultraWarm), ((MapCodec)Codec.BOOL.fieldOf("natural")).forGetter(DimensionType::natural), ((MapCodec)Codec.BOOL.fieldOf("shrunk")).forGetter(DimensionType::shrunk), ((MapCodec)Codec.FLOAT.fieldOf("ambient_light")).forGetter(dimensionType -> Float.valueOf(dimensionType.ambientLight))).apply((Applicative<DimensionType, ?>)instance, DimensionType::new));
    public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0f, 0.75f, 0.5f, 0.25f, 0.0f, 0.25f, 0.5f, 0.75f};
    public static final ResourceKey<DimensionType> OVERWORLD_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<DimensionType> NETHER_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_nether"));
    public static final ResourceKey<DimensionType> END_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_end"));
    private static final LinkedHashSet<ResourceKey<DimensionType>> BUILTIN_ORDER = Sets.newLinkedHashSet(ImmutableList.of(OVERWORLD_LOCATION, NETHER_LOCATION, END_LOCATION));
    private static final DimensionType DEFAULT_OVERWORLD = new DimensionType("", OptionalLong.empty(), true, false, false, true, false, false, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE, Optional.of(OVERWORLD_LOCATION), 0.0f);
    private static final DimensionType DEFAULT_NETHER = new DimensionType("_nether", OptionalLong.of(18000L), false, true, true, false, true, false, FuzzyOffsetBiomeZoomer.INSTANCE, Optional.of(NETHER_LOCATION), 0.1f);
    private static final DimensionType DEFAULT_END = new DimensionType("_end", OptionalLong.of(6000L), false, false, false, false, false, true, FuzzyOffsetBiomeZoomer.INSTANCE, Optional.of(END_LOCATION), 0.0f);
    private static final Map<ResourceKey<DimensionType>, DimensionType> BUILTIN = ImmutableMap.of(OVERWORLD_LOCATION, DEFAULT_OVERWORLD, NETHER_LOCATION, DEFAULT_NETHER, END_LOCATION, DEFAULT_END);
    private static final Codec<DimensionType> BUILTIN_CODEC = RESOURCE_KEY_CODEC.flatXmap(resourceKey -> Optional.ofNullable(BUILTIN.get(resourceKey)).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown builtin dimension: " + resourceKey)), dimensionType -> dimensionType.builtinKey.map(DataResult::success).orElseGet(() -> DataResult.error("Unknown builtin dimension: " + dimensionType))).stable();
    public static final Codec<DimensionType> CODEC = Codec.either(BUILTIN_CODEC, DIRECT_CODEC).flatXmap(either -> either.map(dimensionType -> DataResult.success(dimensionType, Lifecycle.stable()), DataResult::success), dimensionType -> dimensionType.builtinKey.isPresent() ? DataResult.success(Either.left(dimensionType), Lifecycle.stable()) : DataResult.success(Either.right(dimensionType)));
    private final String fileSuffix;
    private final OptionalLong fixedTime;
    private final boolean hasSkylight;
    private final boolean hasCeiling;
    private final boolean ultraWarm;
    private final boolean natural;
    private final boolean shrunk;
    private final boolean createDragonFight;
    private final BiomeZoomer biomeZoomer;
    private final Optional<ResourceKey<DimensionType>> builtinKey;
    private final float ambientLight;
    private final transient float[] brightnessRamp;

    protected DimensionType(OptionalLong optionalLong, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, float f) {
        this("", optionalLong, bl, bl2, bl3, bl4, bl5, false, FuzzyOffsetBiomeZoomer.INSTANCE, Optional.empty(), f);
    }

    protected DimensionType(String string, OptionalLong optionalLong, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, BiomeZoomer biomeZoomer, Optional<ResourceKey<DimensionType>> optional, float f) {
        this.fileSuffix = string;
        this.fixedTime = optionalLong;
        this.hasSkylight = bl;
        this.hasCeiling = bl2;
        this.ultraWarm = bl3;
        this.natural = bl4;
        this.shrunk = bl5;
        this.createDragonFight = bl6;
        this.biomeZoomer = biomeZoomer;
        this.builtinKey = optional;
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
    public static DataResult<ResourceKey<DimensionType>> parseLegacy(Dynamic<?> dynamic) {
        DataResult<Number> dataResult = dynamic.asNumber();
        if (dataResult.result().equals(Optional.of(-1))) {
            return DataResult.success(NETHER_LOCATION);
        }
        if (dataResult.result().equals(Optional.of(0))) {
            return DataResult.success(OVERWORLD_LOCATION);
        }
        if (dataResult.result().equals(Optional.of(1))) {
            return DataResult.success(END_LOCATION);
        }
        return ResourceLocation.CODEC.xmap(ResourceKey.elementKey(Registry.DIMENSION_TYPE_REGISTRY), ResourceKey::location).parse(dynamic);
    }

    @Environment(value=EnvType.CLIENT)
    public static RegistryAccess.RegistryHolder registerBuiltin(RegistryAccess.RegistryHolder registryHolder) {
        registryHolder.registerDimension(OVERWORLD_LOCATION, DEFAULT_OVERWORLD);
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

    public static LinkedHashMap<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>> defaultDimensions(long l) {
        LinkedHashMap<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>> linkedHashMap = Maps.newLinkedHashMap();
        linkedHashMap.put(NETHER_LOCATION, Pair.of(DEFAULT_NETHER, DimensionType.defaultNetherGenerator(l)));
        linkedHashMap.put(END_LOCATION, Pair.of(DEFAULT_END, DimensionType.defaultEndGenerator(l)));
        return linkedHashMap;
    }

    public static DimensionType defaultOverworld() {
        return DEFAULT_OVERWORLD;
    }

    public static boolean stable(long l, LinkedHashMap<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>> linkedHashMap) {
        ArrayList<Map.Entry<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>>> list = Lists.newArrayList(linkedHashMap.entrySet());
        if (list.size() != 3) {
            return false;
        }
        Map.Entry entry = (Map.Entry)list.get(0);
        Map.Entry entry2 = (Map.Entry)list.get(1);
        Map.Entry entry3 = (Map.Entry)list.get(2);
        if (entry.getKey() != OVERWORLD_LOCATION || entry2.getKey() != NETHER_LOCATION || entry3.getKey() != END_LOCATION) {
            return false;
        }
        if (((Pair)entry.getValue()).getFirst() != DEFAULT_OVERWORLD || ((Pair)entry2.getValue()).getFirst() != DEFAULT_NETHER || ((Pair)entry3.getValue()).getFirst() != DEFAULT_END) {
            return false;
        }
        if (!(((Pair)entry2.getValue()).getSecond() instanceof NoiseBasedChunkGenerator) || !(((Pair)entry3.getValue()).getSecond() instanceof NoiseBasedChunkGenerator)) {
            return false;
        }
        NoiseBasedChunkGenerator noiseBasedChunkGenerator = (NoiseBasedChunkGenerator)((Pair)entry2.getValue()).getSecond();
        NoiseBasedChunkGenerator noiseBasedChunkGenerator2 = (NoiseBasedChunkGenerator)((Pair)entry3.getValue()).getSecond();
        if (!noiseBasedChunkGenerator.stable(l, NoiseGeneratorSettings.Preset.NETHER)) {
            return false;
        }
        if (!noiseBasedChunkGenerator2.stable(l, NoiseGeneratorSettings.Preset.END)) {
            return false;
        }
        if (!(noiseBasedChunkGenerator.getBiomeSource() instanceof MultiNoiseBiomeSource)) {
            return false;
        }
        MultiNoiseBiomeSource multiNoiseBiomeSource = (MultiNoiseBiomeSource)noiseBasedChunkGenerator.getBiomeSource();
        if (!multiNoiseBiomeSource.stable(l)) {
            return false;
        }
        if (!(noiseBasedChunkGenerator2.getBiomeSource() instanceof TheEndBiomeSource)) {
            return false;
        }
        TheEndBiomeSource theEndBiomeSource = (TheEndBiomeSource)noiseBasedChunkGenerator2.getBiomeSource();
        return theEndBiomeSource.stable(l);
    }

    public String getFileSuffix() {
        return this.fileSuffix;
    }

    public static File getStorageFolder(ResourceKey<?> resourceKey, File file) {
        if (Objects.equals(resourceKey, OVERWORLD_LOCATION)) {
            return file;
        }
        if (Objects.equals(resourceKey, END_LOCATION)) {
            return new File(file, "DIM1");
        }
        if (Objects.equals(resourceKey, NETHER_LOCATION)) {
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

    public boolean createDragonFight() {
        return this.createDragonFight;
    }

    public BiomeZoomer getBiomeZoomer() {
        return this.biomeZoomer;
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

    public boolean isOverworld() {
        return this == DEFAULT_OVERWORLD;
    }

    public boolean isNether() {
        return this == DEFAULT_NETHER;
    }

    public boolean isEnd() {
        return this == DEFAULT_END;
    }

    public static LinkedHashMap<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>> sortMap(Map<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>> map) {
        LinkedHashMap<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>> linkedHashMap = Maps.newLinkedHashMap();
        for (ResourceKey resourceKey : BUILTIN_ORDER) {
            Pair<DimensionType, ChunkGenerator> pair = map.get(resourceKey);
            if (pair == null) continue;
            linkedHashMap.put(resourceKey, pair);
        }
        for (Map.Entry entry : map.entrySet()) {
            if (BUILTIN_ORDER.contains(entry.getKey())) continue;
            linkedHashMap.put((ResourceKey<DimensionType>)entry.getKey(), (Pair<DimensionType, ChunkGenerator>)entry.getValue());
        }
        return linkedHashMap;
    }
}

