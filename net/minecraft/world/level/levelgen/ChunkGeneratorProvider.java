/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.DynamicLike;
import com.mojang.datafixers.OptionalDynamic;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.CheckerboardBiomeSourceSettings;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSourceSettings;
import net.minecraft.world.level.biome.OverworldBiomeSourceSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldLevelSource;
import net.minecraft.world.level.levelgen.TheEndGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

public class ChunkGeneratorProvider {
    private final LevelType type;
    private final Dynamic<?> settings;
    private final Function<LevelAccessor, ChunkGenerator<?>> supplier;

    public ChunkGeneratorProvider(LevelType levelType, Dynamic<?> dynamic, Function<LevelAccessor, ChunkGenerator<?>> function) {
        this.type = levelType;
        this.settings = dynamic;
        this.supplier = function;
    }

    public LevelType getType() {
        return this.type;
    }

    public Dynamic<?> getSettings() {
        return this.settings;
    }

    public ChunkGenerator<?> create(LevelAccessor levelAccessor) {
        return this.supplier.apply(levelAccessor);
    }

    public static ChunkGeneratorProvider createNormal(LevelType levelType, Dynamic<?> dynamic) {
        OverworldGeneratorSettings overworldGeneratorSettings = ChunkGeneratorType.SURFACE.createSettings();
        return new ChunkGeneratorProvider(levelType, dynamic, levelAccessor -> {
            OverworldBiomeSourceSettings overworldBiomeSourceSettings = BiomeSourceType.VANILLA_LAYERED.createSettings(levelAccessor.getSeed()).setLevelType(levelType).setGeneratorSettings(overworldGeneratorSettings);
            return ChunkGeneratorType.SURFACE.create((LevelAccessor)levelAccessor, BiomeSourceType.VANILLA_LAYERED.create(overworldBiomeSourceSettings), overworldGeneratorSettings);
        });
    }

    public static ChunkGeneratorProvider createFlat(LevelType levelType, Dynamic<?> dynamic) {
        FlatLevelGeneratorSettings flatLevelGeneratorSettings = FlatLevelGeneratorSettings.fromObject(dynamic);
        return new ChunkGeneratorProvider(levelType, dynamic, levelAccessor -> {
            FixedBiomeSourceSettings fixedBiomeSourceSettings = BiomeSourceType.FIXED.createSettings(levelAccessor.getSeed()).setBiome(flatLevelGeneratorSettings.getBiome());
            return ChunkGeneratorType.FLAT.create((LevelAccessor)levelAccessor, BiomeSourceType.FIXED.create(fixedBiomeSourceSettings), flatLevelGeneratorSettings);
        });
    }

    private static <T> T getRegistryValue(DynamicLike<?> dynamicLike, Registry<T> registry, T object) {
        return (T)dynamicLike.asString().map(ResourceLocation::new).flatMap(registry::getOptional).orElse(object);
    }

    private static LongFunction<BiomeSource> createBuffetBiomeSource(DynamicLike<?> dynamicLike) {
        BiomeSourceType<FixedBiomeSourceSettings, FixedBiomeSource> biomeSourceType = ChunkGeneratorProvider.getRegistryValue(dynamicLike.get("type"), Registry.BIOME_SOURCE_TYPE, BiomeSourceType.FIXED);
        OptionalDynamic<?> dynamicLike2 = dynamicLike.get("options");
        Stream stream2 = ((DynamicLike)dynamicLike2).get("biomes").asStreamOpt().map(stream -> stream.map(dynamic -> ChunkGeneratorProvider.getRegistryValue(dynamic, Registry.BIOME, Biomes.OCEAN))).orElseGet(Stream::empty);
        if (BiomeSourceType.CHECKERBOARD == biomeSourceType) {
            Biome[] biomeArray;
            int i = ((DynamicLike)dynamicLike2).get("size").asInt(2);
            Biome[] biomes = (Biome[])stream2.toArray(Biome[]::new);
            if (biomes.length > 0) {
                biomeArray = biomes;
            } else {
                Biome[] biomeArray2 = new Biome[1];
                biomeArray = biomeArray2;
                biomeArray2[0] = Biomes.OCEAN;
            }
            Biome[] biomes2 = biomeArray;
            return l -> {
                CheckerboardBiomeSourceSettings checkerboardBiomeSourceSettings = BiomeSourceType.CHECKERBOARD.createSettings(l).setAllowedBiomes(biomes2).setSize(i);
                return BiomeSourceType.CHECKERBOARD.create(checkerboardBiomeSourceSettings);
            };
        }
        if (BiomeSourceType.VANILLA_LAYERED == biomeSourceType) {
            return l -> {
                OverworldBiomeSourceSettings overworldBiomeSourceSettings = BiomeSourceType.VANILLA_LAYERED.createSettings(l);
                return BiomeSourceType.VANILLA_LAYERED.create(overworldBiomeSourceSettings);
            };
        }
        Biome biome = stream2.findFirst().orElse(Biomes.OCEAN);
        return l -> {
            FixedBiomeSourceSettings fixedBiomeSourceSettings = BiomeSourceType.FIXED.createSettings(l).setBiome(biome);
            return BiomeSourceType.FIXED.create(fixedBiomeSourceSettings);
        };
    }

    private static void decorateCommonGeneratorSettings(ChunkGeneratorSettings chunkGeneratorSettings, DynamicLike<?> dynamicLike) {
        BlockState blockState = ChunkGeneratorProvider.getRegistryValue(dynamicLike.get("default_block"), Registry.BLOCK, Blocks.STONE).defaultBlockState();
        chunkGeneratorSettings.setDefaultBlock(blockState);
        BlockState blockState2 = ChunkGeneratorProvider.getRegistryValue(dynamicLike.get("default_fluid"), Registry.BLOCK, Blocks.WATER).defaultBlockState();
        chunkGeneratorSettings.setDefaultFluid(blockState2);
    }

    private static Function<LevelAccessor, ChunkGenerator<?>> createBuffetGenerator(DynamicLike<?> dynamicLike, LongFunction<BiomeSource> longFunction) {
        ChunkGeneratorType<OverworldGeneratorSettings, OverworldLevelSource> chunkGeneratorType = ChunkGeneratorProvider.getRegistryValue(dynamicLike.get("type"), Registry.CHUNK_GENERATOR_TYPE, ChunkGeneratorType.SURFACE);
        return ChunkGeneratorProvider.createBuffetGeneratorCap(dynamicLike, chunkGeneratorType, longFunction);
    }

    private static <C extends ChunkGeneratorSettings, T extends ChunkGenerator<C>> Function<LevelAccessor, ChunkGenerator<?>> createBuffetGeneratorCap(DynamicLike<?> dynamicLike, ChunkGeneratorType<C, T> chunkGeneratorType, LongFunction<BiomeSource> longFunction) {
        Object chunkGeneratorSettings = chunkGeneratorType.createSettings();
        if (chunkGeneratorType == ChunkGeneratorType.FLOATING_ISLANDS) {
            TheEndGeneratorSettings theEndGeneratorSettings = (TheEndGeneratorSettings)chunkGeneratorSettings;
            theEndGeneratorSettings.setSpawnPosition(new BlockPos(0, 64, 0));
        }
        ChunkGeneratorProvider.decorateCommonGeneratorSettings(chunkGeneratorSettings, dynamicLike.get("options"));
        return levelAccessor -> chunkGeneratorType.create((LevelAccessor)levelAccessor, (BiomeSource)longFunction.apply(levelAccessor.getSeed()), chunkGeneratorSettings);
    }

    public static ChunkGeneratorProvider createBuffet(LevelType levelType, Dynamic<?> dynamic) {
        LongFunction<BiomeSource> longFunction = ChunkGeneratorProvider.createBuffetBiomeSource(dynamic.get("biome_source"));
        Function<LevelAccessor, ChunkGenerator<?>> function = ChunkGeneratorProvider.createBuffetGenerator(dynamic.get("chunk_generator"), longFunction);
        return new ChunkGeneratorProvider(levelType, dynamic, function);
    }

    public static ChunkGeneratorProvider createDebug(LevelType levelType, Dynamic<?> dynamic) {
        return new ChunkGeneratorProvider(levelType, dynamic, levelAccessor -> {
            FixedBiomeSourceSettings fixedBiomeSourceSettings = BiomeSourceType.FIXED.createSettings(levelAccessor.getSeed()).setBiome(Biomes.PLAINS);
            return ChunkGeneratorType.DEBUG.create((LevelAccessor)levelAccessor, BiomeSourceType.FIXED.create(fixedBiomeSourceSettings), ChunkGeneratorType.DEBUG.createSettings());
        });
    }
}

