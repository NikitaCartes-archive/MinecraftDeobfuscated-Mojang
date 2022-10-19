/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.Nullable;

public class ChunkStatus {
    public static final int MAX_STRUCTURE_DISTANCE = 8;
    private static final EnumSet<Heightmap.Types> PRE_FEATURES = EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG);
    public static final EnumSet<Heightmap.Types> POST_FEATURES = EnumSet.of(Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE, Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES);
    private static final LoadingTask PASSTHROUGH_LOAD_TASK = (chunkStatus, serverLevel, structureTemplateManager, threadedLevelLightEngine, function, chunkAccess) -> {
        if (chunkAccess instanceof ProtoChunk) {
            ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
            if (!chunkAccess.getStatus().isOrAfter(chunkStatus)) {
                protoChunk.setStatus(chunkStatus);
            }
        }
        return CompletableFuture.completedFuture(Either.left(chunkAccess));
    };
    public static final ChunkStatus EMPTY = ChunkStatus.registerSimple("empty", null, -1, PRE_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {});
    public static final ChunkStatus STRUCTURE_STARTS = ChunkStatus.register("structure_starts", EMPTY, 0, PRE_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, executor, serverLevel, chunkGenerator, structureTemplateManager, threadedLevelLightEngine, function, list, chunkAccess, bl) -> {
        if (!chunkAccess.getStatus().isOrAfter(chunkStatus)) {
            if (serverLevel.getServer().getWorldData().worldGenOptions().generateStructures()) {
                chunkGenerator.createStructures(serverLevel.registryAccess(), serverLevel.getChunkSource().randomState(), serverLevel.structureManager(), chunkAccess, structureTemplateManager, serverLevel.getSeed());
            }
            if (chunkAccess instanceof ProtoChunk) {
                ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
                protoChunk.setStatus(chunkStatus);
            }
            serverLevel.onStructureStartsAvailable(chunkAccess);
        }
        return CompletableFuture.completedFuture(Either.left(chunkAccess));
    }, (chunkStatus, serverLevel, structureTemplateManager, threadedLevelLightEngine, function, chunkAccess) -> {
        if (!chunkAccess.getStatus().isOrAfter(chunkStatus)) {
            if (chunkAccess instanceof ProtoChunk) {
                ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
                protoChunk.setStatus(chunkStatus);
            }
            serverLevel.onStructureStartsAvailable(chunkAccess);
        }
        return CompletableFuture.completedFuture(Either.left(chunkAccess));
    });
    public static final ChunkStatus STRUCTURE_REFERENCES = ChunkStatus.registerSimple("structure_references", STRUCTURE_STARTS, 8, PRE_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {
        WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, -1);
        chunkGenerator.createReferences(worldGenRegion, serverLevel.structureManager().forWorldGenRegion(worldGenRegion), chunkAccess);
    });
    public static final ChunkStatus BIOMES = ChunkStatus.register("biomes", STRUCTURE_REFERENCES, 8, PRE_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, executor, serverLevel, chunkGenerator, structureTemplateManager, threadedLevelLightEngine, function, list, chunkAccess2, bl) -> {
        if (bl || !chunkAccess2.getStatus().isOrAfter(chunkStatus)) {
            WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, -1);
            return chunkGenerator.createBiomes(serverLevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), executor, serverLevel.getChunkSource().randomState(), Blender.of(worldGenRegion), serverLevel.structureManager().forWorldGenRegion(worldGenRegion), chunkAccess2).thenApply(chunkAccess -> {
                if (chunkAccess instanceof ProtoChunk) {
                    ((ProtoChunk)chunkAccess).setStatus(chunkStatus);
                }
                return Either.left(chunkAccess);
            });
        }
        return CompletableFuture.completedFuture(Either.left(chunkAccess2));
    });
    public static final ChunkStatus NOISE = ChunkStatus.register("noise", BIOMES, 8, PRE_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, executor, serverLevel, chunkGenerator, structureTemplateManager, threadedLevelLightEngine, function, list, chunkAccess2, bl) -> {
        if (bl || !chunkAccess2.getStatus().isOrAfter(chunkStatus)) {
            WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, 0);
            return chunkGenerator.fillFromNoise(executor, Blender.of(worldGenRegion), serverLevel.getChunkSource().randomState(), serverLevel.structureManager().forWorldGenRegion(worldGenRegion), chunkAccess2).thenApply(chunkAccess -> {
                if (chunkAccess instanceof ProtoChunk) {
                    ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
                    BelowZeroRetrogen belowZeroRetrogen = protoChunk.getBelowZeroRetrogen();
                    if (belowZeroRetrogen != null) {
                        BelowZeroRetrogen.replaceOldBedrock(protoChunk);
                        if (belowZeroRetrogen.hasBedrockHoles()) {
                            belowZeroRetrogen.applyBedrockMask(protoChunk);
                        }
                    }
                    protoChunk.setStatus(chunkStatus);
                }
                return Either.left(chunkAccess);
            });
        }
        return CompletableFuture.completedFuture(Either.left(chunkAccess2));
    });
    public static final ChunkStatus SURFACE = ChunkStatus.registerSimple("surface", NOISE, 8, PRE_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {
        WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, 0);
        chunkGenerator.buildSurface(worldGenRegion, serverLevel.structureManager().forWorldGenRegion(worldGenRegion), serverLevel.getChunkSource().randomState(), chunkAccess);
    });
    public static final ChunkStatus CARVERS = ChunkStatus.registerSimple("carvers", SURFACE, 8, PRE_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {
        WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, 0);
        if (chunkAccess instanceof ProtoChunk) {
            ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
            Blender.addAroundOldChunksCarvingMaskFilter(worldGenRegion, protoChunk);
        }
        chunkGenerator.applyCarvers(worldGenRegion, serverLevel.getSeed(), serverLevel.getChunkSource().randomState(), serverLevel.getBiomeManager(), serverLevel.structureManager().forWorldGenRegion(worldGenRegion), chunkAccess, GenerationStep.Carving.AIR);
    });
    public static final ChunkStatus LIQUID_CARVERS = ChunkStatus.registerSimple("liquid_carvers", CARVERS, 8, POST_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {});
    public static final ChunkStatus FEATURES = ChunkStatus.register("features", LIQUID_CARVERS, 8, POST_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, executor, serverLevel, chunkGenerator, structureTemplateManager, threadedLevelLightEngine, function, list, chunkAccess, bl) -> {
        ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
        protoChunk.setLightEngine(threadedLevelLightEngine);
        if (bl || !chunkAccess.getStatus().isOrAfter(chunkStatus)) {
            Heightmap.primeHeightmaps(chunkAccess, EnumSet.of(Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE));
            WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, 1);
            chunkGenerator.applyBiomeDecoration(worldGenRegion, chunkAccess, serverLevel.structureManager().forWorldGenRegion(worldGenRegion));
            Blender.generateBorderTicks(worldGenRegion, chunkAccess);
            protoChunk.setStatus(chunkStatus);
        }
        return threadedLevelLightEngine.retainData(chunkAccess).thenApply(Either::left);
    }, (chunkStatus, serverLevel, structureTemplateManager, threadedLevelLightEngine, function, chunkAccess) -> threadedLevelLightEngine.retainData(chunkAccess).thenApply(Either::left));
    public static final ChunkStatus LIGHT = ChunkStatus.register("light", FEATURES, 1, POST_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, executor, serverLevel, chunkGenerator, structureTemplateManager, threadedLevelLightEngine, function, list, chunkAccess, bl) -> ChunkStatus.lightChunk(chunkStatus, threadedLevelLightEngine, chunkAccess), (chunkStatus, serverLevel, structureTemplateManager, threadedLevelLightEngine, function, chunkAccess) -> ChunkStatus.lightChunk(chunkStatus, threadedLevelLightEngine, chunkAccess));
    public static final ChunkStatus SPAWN = ChunkStatus.registerSimple("spawn", LIGHT, 0, POST_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {
        if (!chunkAccess.isUpgrading()) {
            chunkGenerator.spawnOriginalMobs(new WorldGenRegion(serverLevel, list, chunkStatus, -1));
        }
    });
    public static final ChunkStatus HEIGHTMAPS = ChunkStatus.registerSimple("heightmaps", SPAWN, 0, POST_FEATURES, ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {});
    public static final ChunkStatus FULL = ChunkStatus.register("full", HEIGHTMAPS, 0, POST_FEATURES, ChunkType.LEVELCHUNK, (chunkStatus, executor, serverLevel, chunkGenerator, structureTemplateManager, threadedLevelLightEngine, function, list, chunkAccess, bl) -> (CompletableFuture)function.apply(chunkAccess), (chunkStatus, serverLevel, structureTemplateManager, threadedLevelLightEngine, function, chunkAccess) -> (CompletableFuture)function.apply(chunkAccess));
    private static final List<ChunkStatus> STATUS_BY_RANGE = ImmutableList.of(FULL, FEATURES, LIQUID_CARVERS, BIOMES, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, new ChunkStatus[0]);
    private static final IntList RANGE_BY_STATUS = Util.make(new IntArrayList(ChunkStatus.getStatusList().size()), intArrayList -> {
        int i = 0;
        for (int j = ChunkStatus.getStatusList().size() - 1; j >= 0; --j) {
            while (i + 1 < STATUS_BY_RANGE.size() && j <= STATUS_BY_RANGE.get(i + 1).getIndex()) {
                ++i;
            }
            intArrayList.add(0, i);
        }
    });
    private final String name;
    private final int index;
    private final ChunkStatus parent;
    private final GenerationTask generationTask;
    private final LoadingTask loadingTask;
    private final int range;
    private final ChunkType chunkType;
    private final EnumSet<Heightmap.Types> heightmapsAfter;

    private static CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> lightChunk(ChunkStatus chunkStatus, ThreadedLevelLightEngine threadedLevelLightEngine, ChunkAccess chunkAccess) {
        boolean bl = ChunkStatus.isLighted(chunkStatus, chunkAccess);
        if (!chunkAccess.getStatus().isOrAfter(chunkStatus)) {
            ((ProtoChunk)chunkAccess).setStatus(chunkStatus);
        }
        return threadedLevelLightEngine.lightChunk(chunkAccess, bl).thenApply(Either::left);
    }

    private static ChunkStatus registerSimple(String string, @Nullable ChunkStatus chunkStatus, int i, EnumSet<Heightmap.Types> enumSet, ChunkType chunkType, SimpleGenerationTask simpleGenerationTask) {
        return ChunkStatus.register(string, chunkStatus, i, enumSet, chunkType, simpleGenerationTask);
    }

    private static ChunkStatus register(String string, @Nullable ChunkStatus chunkStatus, int i, EnumSet<Heightmap.Types> enumSet, ChunkType chunkType, GenerationTask generationTask) {
        return ChunkStatus.register(string, chunkStatus, i, enumSet, chunkType, generationTask, PASSTHROUGH_LOAD_TASK);
    }

    private static ChunkStatus register(String string, @Nullable ChunkStatus chunkStatus, int i, EnumSet<Heightmap.Types> enumSet, ChunkType chunkType, GenerationTask generationTask, LoadingTask loadingTask) {
        return Registry.register(Registry.CHUNK_STATUS, string, new ChunkStatus(string, chunkStatus, i, enumSet, chunkType, generationTask, loadingTask));
    }

    public static List<ChunkStatus> getStatusList() {
        ChunkStatus chunkStatus;
        ArrayList<ChunkStatus> list = Lists.newArrayList();
        for (chunkStatus = FULL; chunkStatus.getParent() != chunkStatus; chunkStatus = chunkStatus.getParent()) {
            list.add(chunkStatus);
        }
        list.add(chunkStatus);
        Collections.reverse(list);
        return list;
    }

    private static boolean isLighted(ChunkStatus chunkStatus, ChunkAccess chunkAccess) {
        return chunkAccess.getStatus().isOrAfter(chunkStatus) && chunkAccess.isLightCorrect();
    }

    public static ChunkStatus getStatusAroundFullChunk(int i) {
        if (i >= STATUS_BY_RANGE.size()) {
            return EMPTY;
        }
        if (i < 0) {
            return FULL;
        }
        return STATUS_BY_RANGE.get(i);
    }

    public static int maxDistance() {
        return STATUS_BY_RANGE.size();
    }

    public static int getDistance(ChunkStatus chunkStatus) {
        return RANGE_BY_STATUS.getInt(chunkStatus.getIndex());
    }

    ChunkStatus(String string, @Nullable ChunkStatus chunkStatus, int i, EnumSet<Heightmap.Types> enumSet, ChunkType chunkType, GenerationTask generationTask, LoadingTask loadingTask) {
        this.name = string;
        this.parent = chunkStatus == null ? this : chunkStatus;
        this.generationTask = generationTask;
        this.loadingTask = loadingTask;
        this.range = i;
        this.chunkType = chunkType;
        this.heightmapsAfter = enumSet;
        this.index = chunkStatus == null ? 0 : chunkStatus.getIndex() + 1;
    }

    public int getIndex() {
        return this.index;
    }

    public String getName() {
        return this.name;
    }

    public ChunkStatus getParent() {
        return this.parent;
    }

    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> generate(Executor executor, ServerLevel serverLevel, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, ThreadedLevelLightEngine threadedLevelLightEngine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function, List<ChunkAccess> list, boolean bl) {
        ChunkAccess chunkAccess = list.get(list.size() / 2);
        ProfiledDuration profiledDuration = JvmProfiler.INSTANCE.onChunkGenerate(chunkAccess.getPos(), serverLevel.dimension(), this.name);
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = this.generationTask.doWork(this, executor, serverLevel, chunkGenerator, structureTemplateManager, threadedLevelLightEngine, function, list, chunkAccess, bl);
        return profiledDuration != null ? completableFuture.thenApply(either -> {
            profiledDuration.finish();
            return either;
        }) : completableFuture;
    }

    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> load(ServerLevel serverLevel, StructureTemplateManager structureTemplateManager, ThreadedLevelLightEngine threadedLevelLightEngine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function, ChunkAccess chunkAccess) {
        return this.loadingTask.doWork(this, serverLevel, structureTemplateManager, threadedLevelLightEngine, function, chunkAccess);
    }

    public int getRange() {
        return this.range;
    }

    public ChunkType getChunkType() {
        return this.chunkType;
    }

    public static ChunkStatus byName(String string) {
        return Registry.CHUNK_STATUS.get(ResourceLocation.tryParse(string));
    }

    public EnumSet<Heightmap.Types> heightmapsAfter() {
        return this.heightmapsAfter;
    }

    public boolean isOrAfter(ChunkStatus chunkStatus) {
        return this.getIndex() >= chunkStatus.getIndex();
    }

    public String toString() {
        return Registry.CHUNK_STATUS.getKey(this).toString();
    }

    public static enum ChunkType {
        PROTOCHUNK,
        LEVELCHUNK;

    }

    static interface GenerationTask {
        public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(ChunkStatus var1, Executor var2, ServerLevel var3, ChunkGenerator var4, StructureTemplateManager var5, ThreadedLevelLightEngine var6, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> var7, List<ChunkAccess> var8, ChunkAccess var9, boolean var10);
    }

    static interface LoadingTask {
        public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(ChunkStatus var1, ServerLevel var2, StructureTemplateManager var3, ThreadedLevelLightEngine var4, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> var5, ChunkAccess var6);
    }

    static interface SimpleGenerationTask
    extends GenerationTask {
        @Override
        default public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(ChunkStatus chunkStatus, Executor executor, ServerLevel serverLevel, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, ThreadedLevelLightEngine threadedLevelLightEngine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function, List<ChunkAccess> list, ChunkAccess chunkAccess, boolean bl) {
            if (bl || !chunkAccess.getStatus().isOrAfter(chunkStatus)) {
                this.doWork(chunkStatus, serverLevel, chunkGenerator, list, chunkAccess);
                if (chunkAccess instanceof ProtoChunk) {
                    ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
                    protoChunk.setStatus(chunkStatus);
                }
            }
            return CompletableFuture.completedFuture(Either.left(chunkAccess));
        }

        public void doWork(ChunkStatus var1, ServerLevel var2, ChunkGenerator var3, List<ChunkAccess> var4, ChunkAccess var5);
    }
}

