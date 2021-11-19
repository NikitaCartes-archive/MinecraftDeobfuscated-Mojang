package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class ChunkStatus {
	public static final int MAX_STRUCTURE_DISTANCE = 8;
	private static final EnumSet<Heightmap.Types> PRE_FEATURES = EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG);
	public static final EnumSet<Heightmap.Types> POST_FEATURES = EnumSet.of(
		Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE, Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES
	);
	private static final ChunkStatus.LoadingTask PASSTHROUGH_LOAD_TASK = (chunkStatus, serverLevel, structureManager, threadedLevelLightEngine, function, chunkAccess) -> {
		if (chunkAccess instanceof ProtoChunk protoChunk && !chunkAccess.getStatus().isOrAfter(chunkStatus)) {
			protoChunk.setStatus(chunkStatus);
		}

		return CompletableFuture.completedFuture(Either.left(chunkAccess));
	};
	public static final ChunkStatus EMPTY = registerSimple(
		"empty", null, -1, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {
		}
	);
	public static final ChunkStatus STRUCTURE_STARTS = register(
		"structure_starts",
		EMPTY,
		0,
		PRE_FEATURES,
		ChunkStatus.ChunkType.PROTOCHUNK,
		(chunkStatus, executor, serverLevel, chunkGenerator, structureManager, threadedLevelLightEngine, function, list, chunkAccess, bl) -> {
			if (!chunkAccess.getStatus().isOrAfter(chunkStatus)) {
				if (serverLevel.getServer().getWorldData().worldGenSettings().generateFeatures()) {
					chunkGenerator.createStructures(serverLevel.registryAccess(), serverLevel.structureFeatureManager(), chunkAccess, structureManager, serverLevel.getSeed());
				}

				if (chunkAccess instanceof ProtoChunk protoChunk) {
					protoChunk.setStatus(chunkStatus);
				}

				serverLevel.onStructureStartsAvailable(chunkAccess);
			}

			return CompletableFuture.completedFuture(Either.left(chunkAccess));
		},
		(chunkStatus, serverLevel, structureManager, threadedLevelLightEngine, function, chunkAccess) -> {
			if (!chunkAccess.getStatus().isOrAfter(chunkStatus)) {
				if (chunkAccess instanceof ProtoChunk protoChunk) {
					protoChunk.setStatus(chunkStatus);
				}

				serverLevel.onStructureStartsAvailable(chunkAccess);
			}

			return CompletableFuture.completedFuture(Either.left(chunkAccess));
		}
	);
	public static final ChunkStatus STRUCTURE_REFERENCES = registerSimple(
		"structure_references",
		STRUCTURE_STARTS,
		8,
		PRE_FEATURES,
		ChunkStatus.ChunkType.PROTOCHUNK,
		(chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {
			WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, -1);
			chunkGenerator.createReferences(worldGenRegion, serverLevel.structureFeatureManager().forWorldGenRegion(worldGenRegion), chunkAccess);
		}
	);
	public static final ChunkStatus BIOMES = register(
		"biomes",
		STRUCTURE_REFERENCES,
		8,
		PRE_FEATURES,
		ChunkStatus.ChunkType.PROTOCHUNK,
		(chunkStatus, executor, serverLevel, chunkGenerator, structureManager, threadedLevelLightEngine, function, list, chunkAccess, bl) -> {
			if (!bl && chunkAccess.getStatus().isOrAfter(chunkStatus)) {
				return CompletableFuture.completedFuture(Either.left(chunkAccess));
			} else {
				WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, -1);
				return chunkGenerator.createBiomes(
						serverLevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY),
						executor,
						Blender.of(worldGenRegion),
						serverLevel.structureFeatureManager().forWorldGenRegion(worldGenRegion),
						chunkAccess
					)
					.thenApply(chunkAccessx -> {
						if (chunkAccessx instanceof ProtoChunk) {
							((ProtoChunk)chunkAccessx).setStatus(chunkStatus);
						}

						return Either.left(chunkAccessx);
					});
			}
		}
	);
	public static final ChunkStatus NOISE = register(
		"noise",
		BIOMES,
		8,
		PRE_FEATURES,
		ChunkStatus.ChunkType.PROTOCHUNK,
		(chunkStatus, executor, serverLevel, chunkGenerator, structureManager, threadedLevelLightEngine, function, list, chunkAccess, bl) -> {
			if (!bl && chunkAccess.getStatus().isOrAfter(chunkStatus)) {
				return CompletableFuture.completedFuture(Either.left(chunkAccess));
			} else {
				WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, 0);
				return chunkGenerator.fillFromNoise(
						executor, Blender.of(worldGenRegion), serverLevel.structureFeatureManager().forWorldGenRegion(worldGenRegion), chunkAccess
					)
					.thenApply(chunkAccessx -> {
						if (chunkAccessx instanceof ProtoChunk protoChunk) {
							BelowZeroRetrogen belowZeroRetrogen = protoChunk.getBelowZeroRetrogen();
							if (belowZeroRetrogen != null) {
								BelowZeroRetrogen.replaceOldBedrock(protoChunk);
								if (belowZeroRetrogen.hasBedrockHoles()) {
									belowZeroRetrogen.applyBedrockMask(protoChunk);
								}
							}

							protoChunk.setStatus(chunkStatus);
						}

						return Either.left(chunkAccessx);
					});
			}
		}
	);
	public static final ChunkStatus SURFACE = registerSimple(
		"surface", NOISE, 8, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {
			WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, 0);
			chunkGenerator.buildSurface(worldGenRegion, serverLevel.structureFeatureManager().forWorldGenRegion(worldGenRegion), chunkAccess);
		}
	);
	public static final ChunkStatus CARVERS = registerSimple(
		"carvers",
		SURFACE,
		8,
		PRE_FEATURES,
		ChunkStatus.ChunkType.PROTOCHUNK,
		(chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {
			WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, 0);
			if (chunkAccess instanceof ProtoChunk protoChunk) {
				Blender.addAroundOldChunksCarvingMaskFilter(worldGenRegion, protoChunk);
			}

			chunkGenerator.applyCarvers(
				worldGenRegion,
				serverLevel.getSeed(),
				serverLevel.getBiomeManager(),
				serverLevel.structureFeatureManager().forWorldGenRegion(worldGenRegion),
				chunkAccess,
				GenerationStep.Carving.AIR
			);
		}
	);
	public static final ChunkStatus LIQUID_CARVERS = registerSimple(
		"liquid_carvers", CARVERS, 8, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {
		}
	);
	public static final ChunkStatus FEATURES = register(
		"features",
		LIQUID_CARVERS,
		8,
		POST_FEATURES,
		ChunkStatus.ChunkType.PROTOCHUNK,
		(chunkStatus, executor, serverLevel, chunkGenerator, structureManager, threadedLevelLightEngine, function, list, chunkAccess, bl) -> {
			ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
			protoChunk.setLightEngine(threadedLevelLightEngine);
			if (bl || !chunkAccess.getStatus().isOrAfter(chunkStatus)) {
				Heightmap.primeHeightmaps(
					chunkAccess,
					EnumSet.of(Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE)
				);
				WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, 1);
				chunkGenerator.applyBiomeDecoration(worldGenRegion, chunkAccess, serverLevel.structureFeatureManager().forWorldGenRegion(worldGenRegion));
				Blender.generateBorderTicks(worldGenRegion, chunkAccess);
				protoChunk.setStatus(chunkStatus);
			}

			return CompletableFuture.completedFuture(Either.left(chunkAccess));
		}
	);
	public static final ChunkStatus LIGHT = register(
		"light",
		FEATURES,
		1,
		POST_FEATURES,
		ChunkStatus.ChunkType.PROTOCHUNK,
		(chunkStatus, executor, serverLevel, chunkGenerator, structureManager, threadedLevelLightEngine, function, list, chunkAccess, bl) -> lightChunk(
				chunkStatus, threadedLevelLightEngine, chunkAccess
			),
		(chunkStatus, serverLevel, structureManager, threadedLevelLightEngine, function, chunkAccess) -> lightChunk(
				chunkStatus, threadedLevelLightEngine, chunkAccess
			)
	);
	public static final ChunkStatus SPAWN = registerSimple(
		"spawn", LIGHT, 0, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {
			if (!chunkAccess.isUpgrading()) {
				chunkGenerator.spawnOriginalMobs(new WorldGenRegion(serverLevel, list, chunkStatus, -1));
			}
		}
	);
	public static final ChunkStatus HEIGHTMAPS = registerSimple(
		"heightmaps", SPAWN, 0, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (chunkStatus, serverLevel, chunkGenerator, list, chunkAccess) -> {
		}
	);
	public static final ChunkStatus FULL = register(
		"full",
		HEIGHTMAPS,
		0,
		POST_FEATURES,
		ChunkStatus.ChunkType.LEVELCHUNK,
		(chunkStatus, executor, serverLevel, chunkGenerator, structureManager, threadedLevelLightEngine, function, list, chunkAccess, bl) -> (CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>)function.apply(
				chunkAccess
			),
		(chunkStatus, serverLevel, structureManager, threadedLevelLightEngine, function, chunkAccess) -> (CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>)function.apply(
				chunkAccess
			)
	);
	private static final List<ChunkStatus> STATUS_BY_RANGE = ImmutableList.of(
		FULL,
		FEATURES,
		LIQUID_CARVERS,
		BIOMES,
		STRUCTURE_STARTS,
		STRUCTURE_STARTS,
		STRUCTURE_STARTS,
		STRUCTURE_STARTS,
		STRUCTURE_STARTS,
		STRUCTURE_STARTS,
		STRUCTURE_STARTS,
		STRUCTURE_STARTS
	);
	private static final IntList RANGE_BY_STATUS = Util.make(new IntArrayList(getStatusList().size()), intArrayList -> {
		int i = 0;

		for (int j = getStatusList().size() - 1; j >= 0; j--) {
			while (i + 1 < STATUS_BY_RANGE.size() && j <= ((ChunkStatus)STATUS_BY_RANGE.get(i + 1)).getIndex()) {
				i++;
			}

			intArrayList.add(0, i);
		}
	});
	private final String name;
	private final int index;
	private final ChunkStatus parent;
	private final ChunkStatus.GenerationTask generationTask;
	private final ChunkStatus.LoadingTask loadingTask;
	private final int range;
	private final ChunkStatus.ChunkType chunkType;
	private final EnumSet<Heightmap.Types> heightmapsAfter;

	private static CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> lightChunk(
		ChunkStatus chunkStatus, ThreadedLevelLightEngine threadedLevelLightEngine, ChunkAccess chunkAccess
	) {
		boolean bl = isLighted(chunkStatus, chunkAccess);
		if (!chunkAccess.getStatus().isOrAfter(chunkStatus)) {
			((ProtoChunk)chunkAccess).setStatus(chunkStatus);
		}

		return threadedLevelLightEngine.lightChunk(chunkAccess, bl).thenApply(Either::left);
	}

	private static ChunkStatus registerSimple(
		String string,
		@Nullable ChunkStatus chunkStatus,
		int i,
		EnumSet<Heightmap.Types> enumSet,
		ChunkStatus.ChunkType chunkType,
		ChunkStatus.SimpleGenerationTask simpleGenerationTask
	) {
		return register(string, chunkStatus, i, enumSet, chunkType, simpleGenerationTask);
	}

	private static ChunkStatus register(
		String string,
		@Nullable ChunkStatus chunkStatus,
		int i,
		EnumSet<Heightmap.Types> enumSet,
		ChunkStatus.ChunkType chunkType,
		ChunkStatus.GenerationTask generationTask
	) {
		return register(string, chunkStatus, i, enumSet, chunkType, generationTask, PASSTHROUGH_LOAD_TASK);
	}

	private static ChunkStatus register(
		String string,
		@Nullable ChunkStatus chunkStatus,
		int i,
		EnumSet<Heightmap.Types> enumSet,
		ChunkStatus.ChunkType chunkType,
		ChunkStatus.GenerationTask generationTask,
		ChunkStatus.LoadingTask loadingTask
	) {
		return Registry.register(Registry.CHUNK_STATUS, string, new ChunkStatus(string, chunkStatus, i, enumSet, chunkType, generationTask, loadingTask));
	}

	public static List<ChunkStatus> getStatusList() {
		List<ChunkStatus> list = Lists.<ChunkStatus>newArrayList();

		ChunkStatus chunkStatus;
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
		} else {
			return i < 0 ? FULL : (ChunkStatus)STATUS_BY_RANGE.get(i);
		}
	}

	public static int maxDistance() {
		return STATUS_BY_RANGE.size();
	}

	public static int getDistance(ChunkStatus chunkStatus) {
		return RANGE_BY_STATUS.getInt(chunkStatus.getIndex());
	}

	ChunkStatus(
		String string,
		@Nullable ChunkStatus chunkStatus,
		int i,
		EnumSet<Heightmap.Types> enumSet,
		ChunkStatus.ChunkType chunkType,
		ChunkStatus.GenerationTask generationTask,
		ChunkStatus.LoadingTask loadingTask
	) {
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

	public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> generate(
		Executor executor,
		ServerLevel serverLevel,
		ChunkGenerator chunkGenerator,
		StructureManager structureManager,
		ThreadedLevelLightEngine threadedLevelLightEngine,
		Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function,
		List<ChunkAccess> list,
		boolean bl
	) {
		ChunkAccess chunkAccess = (ChunkAccess)list.get(list.size() / 2);
		ProfiledDuration profiledDuration = JvmProfiler.INSTANCE.onChunkGenerate(chunkAccess.getPos(), serverLevel.dimension(), this.name);
		CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = this.generationTask
			.doWork(this, executor, serverLevel, chunkGenerator, structureManager, threadedLevelLightEngine, function, list, chunkAccess, bl);
		return profiledDuration != null ? completableFuture.thenApply(either -> {
			profiledDuration.finish();
			return either;
		}) : completableFuture;
	}

	public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> load(
		ServerLevel serverLevel,
		StructureManager structureManager,
		ThreadedLevelLightEngine threadedLevelLightEngine,
		Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function,
		ChunkAccess chunkAccess
	) {
		return this.loadingTask.doWork(this, serverLevel, structureManager, threadedLevelLightEngine, function, chunkAccess);
	}

	public int getRange() {
		return this.range;
	}

	public ChunkStatus.ChunkType getChunkType() {
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

	interface GenerationTask {
		CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(
			ChunkStatus chunkStatus,
			Executor executor,
			ServerLevel serverLevel,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			ThreadedLevelLightEngine threadedLevelLightEngine,
			Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function,
			List<ChunkAccess> list,
			ChunkAccess chunkAccess,
			boolean bl
		);
	}

	interface LoadingTask {
		CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(
			ChunkStatus chunkStatus,
			ServerLevel serverLevel,
			StructureManager structureManager,
			ThreadedLevelLightEngine threadedLevelLightEngine,
			Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function,
			ChunkAccess chunkAccess
		);
	}

	interface SimpleGenerationTask extends ChunkStatus.GenerationTask {
		@Override
		default CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(
			ChunkStatus chunkStatus,
			Executor executor,
			ServerLevel serverLevel,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			ThreadedLevelLightEngine threadedLevelLightEngine,
			Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function,
			List<ChunkAccess> list,
			ChunkAccess chunkAccess,
			boolean bl
		) {
			if (bl || !chunkAccess.getStatus().isOrAfter(chunkStatus)) {
				this.doWork(chunkStatus, serverLevel, chunkGenerator, list, chunkAccess);
				if (chunkAccess instanceof ProtoChunk) {
					((ProtoChunk)chunkAccess).setStatus(chunkStatus);
				}
			}

			return CompletableFuture.completedFuture(Either.left(chunkAccess));
		}

		void doWork(ChunkStatus chunkStatus, ServerLevel serverLevel, ChunkGenerator chunkGenerator, List<ChunkAccess> list, ChunkAccess chunkAccess);
	}
}
