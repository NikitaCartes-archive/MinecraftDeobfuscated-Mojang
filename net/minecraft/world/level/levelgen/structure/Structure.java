/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public abstract class Structure {
    public static final Codec<Structure> DIRECT_CODEC = BuiltInRegistries.STRUCTURE_TYPE.byNameCodec().dispatch(Structure::type, StructureType::codec);
    public static final Codec<Holder<Structure>> CODEC = RegistryFileCodec.create(Registries.STRUCTURE, DIRECT_CODEC);
    protected final StructureSettings settings;

    public static <S extends Structure> RecordCodecBuilder<S, StructureSettings> settingsCodec(RecordCodecBuilder.Instance<S> instance) {
        return StructureSettings.CODEC.forGetter(structure -> structure.settings);
    }

    public static <S extends Structure> Codec<S> simpleCodec(Function<StructureSettings, S> function) {
        return RecordCodecBuilder.create(instance -> instance.group(Structure.settingsCodec(instance)).apply((Applicative)instance, function));
    }

    protected Structure(StructureSettings structureSettings) {
        this.settings = structureSettings;
    }

    public HolderSet<Biome> biomes() {
        return this.settings.biomes;
    }

    public Map<MobCategory, StructureSpawnOverride> spawnOverrides() {
        return this.settings.spawnOverrides;
    }

    public GenerationStep.Decoration step() {
        return this.settings.step;
    }

    public TerrainAdjustment terrainAdaptation() {
        return this.settings.terrainAdaptation;
    }

    public BoundingBox adjustBoundingBox(BoundingBox boundingBox) {
        if (this.terrainAdaptation() != TerrainAdjustment.NONE) {
            return boundingBox.inflatedBy(12);
        }
        return boundingBox;
    }

    public StructureStart generate(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource, RandomState randomState, StructureTemplateManager structureTemplateManager, long l, ChunkPos chunkPos, int i, LevelHeightAccessor levelHeightAccessor, Predicate<Holder<Biome>> predicate) {
        StructurePiecesBuilder structurePiecesBuilder;
        StructureStart structureStart;
        GenerationContext generationContext = new GenerationContext(registryAccess, chunkGenerator, biomeSource, randomState, structureTemplateManager, l, chunkPos, levelHeightAccessor, predicate);
        Optional<GenerationStub> optional = this.findValidGenerationPoint(generationContext);
        if (optional.isPresent() && (structureStart = new StructureStart(this, chunkPos, i, (structurePiecesBuilder = optional.get().getPiecesBuilder()).build())).isValid()) {
            return structureStart;
        }
        return StructureStart.INVALID_START;
    }

    protected static Optional<GenerationStub> onTopOfChunkCenter(GenerationContext generationContext, Heightmap.Types types, Consumer<StructurePiecesBuilder> consumer) {
        ChunkPos chunkPos = generationContext.chunkPos();
        int i = chunkPos.getMiddleBlockX();
        int j = chunkPos.getMiddleBlockZ();
        int k = generationContext.chunkGenerator().getFirstOccupiedHeight(i, j, types, generationContext.heightAccessor(), generationContext.randomState());
        return Optional.of(new GenerationStub(new BlockPos(i, k, j), consumer));
    }

    private static boolean isValidBiome(GenerationStub generationStub, GenerationContext generationContext) {
        BlockPos blockPos = generationStub.position();
        return generationContext.validBiome.test(generationContext.chunkGenerator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(blockPos.getX()), QuartPos.fromBlock(blockPos.getY()), QuartPos.fromBlock(blockPos.getZ()), generationContext.randomState.sampler()));
    }

    public void afterPlace(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, PiecesContainer piecesContainer) {
    }

    private static int[] getCornerHeights(GenerationContext generationContext, int i, int j, int k, int l) {
        ChunkGenerator chunkGenerator = generationContext.chunkGenerator();
        LevelHeightAccessor levelHeightAccessor = generationContext.heightAccessor();
        RandomState randomState = generationContext.randomState();
        return new int[]{chunkGenerator.getFirstOccupiedHeight(i, k, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState), chunkGenerator.getFirstOccupiedHeight(i, k + l, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState), chunkGenerator.getFirstOccupiedHeight(i + j, k, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState), chunkGenerator.getFirstOccupiedHeight(i + j, k + l, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState)};
    }

    protected static int getLowestY(GenerationContext generationContext, int i, int j) {
        ChunkPos chunkPos = generationContext.chunkPos();
        int k = chunkPos.getMinBlockX();
        int l = chunkPos.getMinBlockZ();
        return Structure.getLowestY(generationContext, k, l, i, j);
    }

    protected static int getLowestY(GenerationContext generationContext, int i, int j, int k, int l) {
        int[] is = Structure.getCornerHeights(generationContext, i, k, j, l);
        return Math.min(Math.min(is[0], is[1]), Math.min(is[2], is[3]));
    }

    @Deprecated
    protected BlockPos getLowestYIn5by5BoxOffset7Blocks(GenerationContext generationContext, Rotation rotation) {
        int i = 5;
        int j = 5;
        if (rotation == Rotation.CLOCKWISE_90) {
            i = -5;
        } else if (rotation == Rotation.CLOCKWISE_180) {
            i = -5;
            j = -5;
        } else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
            j = -5;
        }
        ChunkPos chunkPos = generationContext.chunkPos();
        int k = chunkPos.getBlockX(7);
        int l = chunkPos.getBlockZ(7);
        return new BlockPos(k, Structure.getLowestY(generationContext, k, l, i, j), l);
    }

    protected abstract Optional<GenerationStub> findGenerationPoint(GenerationContext var1);

    public Optional<GenerationStub> findValidGenerationPoint(GenerationContext generationContext) {
        return this.findGenerationPoint(generationContext).filter(generationStub -> Structure.isValidBiome(generationStub, generationContext));
    }

    public abstract StructureType<?> type();

    public record StructureSettings(HolderSet<Biome> biomes, Map<MobCategory, StructureSpawnOverride> spawnOverrides, GenerationStep.Decoration step, TerrainAdjustment terrainAdaptation) {
        public static final MapCodec<StructureSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes")).forGetter(StructureSettings::biomes), Codec.simpleMap(MobCategory.CODEC, StructureSpawnOverride.CODEC, StringRepresentable.keys(MobCategory.values())).fieldOf("spawn_overrides").forGetter(StructureSettings::spawnOverrides), ((MapCodec)GenerationStep.Decoration.CODEC.fieldOf("step")).forGetter(StructureSettings::step), TerrainAdjustment.CODEC.optionalFieldOf("terrain_adaptation", TerrainAdjustment.NONE).forGetter(StructureSettings::terrainAdaptation)).apply((Applicative<StructureSettings, ?>)instance, StructureSettings::new));
    }

    public record GenerationContext(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource, RandomState randomState, StructureTemplateManager structureTemplateManager, WorldgenRandom random, long seed, ChunkPos chunkPos, LevelHeightAccessor heightAccessor, Predicate<Holder<Biome>> validBiome) {
        public GenerationContext(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource, RandomState randomState, StructureTemplateManager structureTemplateManager, long l, ChunkPos chunkPos, LevelHeightAccessor levelHeightAccessor, Predicate<Holder<Biome>> predicate) {
            this(registryAccess, chunkGenerator, biomeSource, randomState, structureTemplateManager, GenerationContext.makeRandom(l, chunkPos), l, chunkPos, levelHeightAccessor, predicate);
        }

        private static WorldgenRandom makeRandom(long l, ChunkPos chunkPos) {
            WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
            worldgenRandom.setLargeFeatureSeed(l, chunkPos.x, chunkPos.z);
            return worldgenRandom;
        }
    }

    public record GenerationStub(BlockPos position, Either<Consumer<StructurePiecesBuilder>, StructurePiecesBuilder> generator) {
        public GenerationStub(BlockPos blockPos, Consumer<StructurePiecesBuilder> consumer) {
            this(blockPos, Either.left(consumer));
        }

        public StructurePiecesBuilder getPiecesBuilder() {
            return this.generator.map(consumer -> {
                StructurePiecesBuilder structurePiecesBuilder = new StructurePiecesBuilder();
                consumer.accept(structurePiecesBuilder);
                return structurePiecesBuilder;
            }, structurePiecesBuilder -> structurePiecesBuilder);
        }
    }
}

