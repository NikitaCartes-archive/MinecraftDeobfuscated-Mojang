/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public final class LevelStem {
    public static final Codec<LevelStem> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)DimensionType.CODEC.fieldOf("type")).forGetter(LevelStem::typeSupplier), ((MapCodec)ChunkGenerator.CODEC.fieldOf("generator")).forGetter(LevelStem::generator)).apply((Applicative<LevelStem, ?>)instance, instance.stable(LevelStem::new)));
    public static final ResourceKey<LevelStem> OVERWORLD = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<LevelStem> NETHER = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_nether"));
    public static final ResourceKey<LevelStem> END = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_end"));
    private static final LinkedHashSet<ResourceKey<LevelStem>> BUILTIN_ORDER = Sets.newLinkedHashSet(ImmutableList.of(OVERWORLD, NETHER, END));
    private final Supplier<DimensionType> type;
    private final ChunkGenerator generator;

    public LevelStem(Supplier<DimensionType> supplier, ChunkGenerator chunkGenerator) {
        this.type = supplier;
        this.generator = chunkGenerator;
    }

    public Supplier<DimensionType> typeSupplier() {
        return this.type;
    }

    public DimensionType type() {
        return this.type.get();
    }

    public ChunkGenerator generator() {
        return this.generator;
    }

    public static MappedRegistry<LevelStem> sortMap(MappedRegistry<LevelStem> mappedRegistry) {
        MappedRegistry<LevelStem> mappedRegistry2 = new MappedRegistry<LevelStem>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
        for (ResourceKey resourceKey : BUILTIN_ORDER) {
            LevelStem levelStem = mappedRegistry.get(resourceKey);
            if (levelStem == null) continue;
            mappedRegistry2.register(resourceKey, levelStem);
        }
        for (Map.Entry entry : mappedRegistry.entrySet()) {
            ResourceKey resourceKey2 = (ResourceKey)entry.getKey();
            if (BUILTIN_ORDER.contains(resourceKey2)) continue;
            mappedRegistry2.register(resourceKey2, entry.getValue());
        }
        return mappedRegistry2;
    }

    public static boolean stable(long l, MappedRegistry<LevelStem> mappedRegistry) {
        ArrayList<Map.Entry<ResourceKey<LevelStem>, LevelStem>> list = Lists.newArrayList(mappedRegistry.entrySet());
        if (list.size() != BUILTIN_ORDER.size()) {
            return false;
        }
        Map.Entry entry = (Map.Entry)list.get(0);
        Map.Entry entry2 = (Map.Entry)list.get(1);
        Map.Entry entry3 = (Map.Entry)list.get(2);
        if (entry.getKey() != OVERWORLD || entry2.getKey() != NETHER || entry3.getKey() != END) {
            return false;
        }
        if (((LevelStem)entry.getValue()).type() != DimensionType.DEFAULT_OVERWORLD && ((LevelStem)entry.getValue()).type() != DimensionType.DEFAULT_OVERWORLD_CAVES) {
            return false;
        }
        if (((LevelStem)entry2.getValue()).type() != DimensionType.DEFAULT_NETHER) {
            return false;
        }
        if (((LevelStem)entry3.getValue()).type() != DimensionType.DEFAULT_END) {
            return false;
        }
        if (!(((LevelStem)entry2.getValue()).generator() instanceof NoiseBasedChunkGenerator) || !(((LevelStem)entry3.getValue()).generator() instanceof NoiseBasedChunkGenerator)) {
            return false;
        }
        NoiseBasedChunkGenerator noiseBasedChunkGenerator = (NoiseBasedChunkGenerator)((LevelStem)entry2.getValue()).generator();
        NoiseBasedChunkGenerator noiseBasedChunkGenerator2 = (NoiseBasedChunkGenerator)((LevelStem)entry3.getValue()).generator();
        if (!noiseBasedChunkGenerator.stable(l, NoiseGeneratorSettings.NETHER)) {
            return false;
        }
        if (!noiseBasedChunkGenerator2.stable(l, NoiseGeneratorSettings.END)) {
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
}

