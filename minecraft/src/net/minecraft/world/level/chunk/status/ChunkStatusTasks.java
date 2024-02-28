package net.minecraft.world.level.chunk.status;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.Blender;

public class ChunkStatusTasks {
	private static boolean isLighted(ChunkAccess chunkAccess) {
		return chunkAccess.getStatus().isOrAfter(ChunkStatus.LIGHT) && chunkAccess.isLightCorrect();
	}

	static CompletableFuture<ChunkAccess> generateEmpty(
		WorldGenContext worldGenContext, ChunkStatus chunkStatus, Executor executor, ToFullChunk toFullChunk, List<ChunkAccess> list, ChunkAccess chunkAccess
	) {
		return CompletableFuture.completedFuture(chunkAccess);
	}

	static CompletableFuture<ChunkAccess> loadPassThrough(
		WorldGenContext worldGenContext, ChunkStatus chunkStatus, ToFullChunk toFullChunk, ChunkAccess chunkAccess
	) {
		return CompletableFuture.completedFuture(chunkAccess);
	}

	static CompletableFuture<ChunkAccess> generateStructureStarts(
		WorldGenContext worldGenContext, ChunkStatus chunkStatus, Executor executor, ToFullChunk toFullChunk, List<ChunkAccess> list, ChunkAccess chunkAccess
	) {
		ServerLevel serverLevel = worldGenContext.level();
		if (serverLevel.getServer().getWorldData().worldGenOptions().generateStructures()) {
			worldGenContext.generator()
				.createStructures(
					serverLevel.registryAccess(),
					serverLevel.getChunkSource().getGeneratorState(),
					serverLevel.structureManager(),
					chunkAccess,
					worldGenContext.structureManager()
				);
		}

		serverLevel.onStructureStartsAvailable(chunkAccess);
		return CompletableFuture.completedFuture(chunkAccess);
	}

	static CompletableFuture<ChunkAccess> loadStructureStarts(
		WorldGenContext worldGenContext, ChunkStatus chunkStatus, ToFullChunk toFullChunk, ChunkAccess chunkAccess
	) {
		worldGenContext.level().onStructureStartsAvailable(chunkAccess);
		return CompletableFuture.completedFuture(chunkAccess);
	}

	static CompletableFuture<ChunkAccess> generateStructureReferences(
		WorldGenContext worldGenContext, ChunkStatus chunkStatus, Executor executor, ToFullChunk toFullChunk, List<ChunkAccess> list, ChunkAccess chunkAccess
	) {
		ServerLevel serverLevel = worldGenContext.level();
		WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, -1);
		worldGenContext.generator().createReferences(worldGenRegion, serverLevel.structureManager().forWorldGenRegion(worldGenRegion), chunkAccess);
		return CompletableFuture.completedFuture(chunkAccess);
	}

	static CompletableFuture<ChunkAccess> generateBiomes(
		WorldGenContext worldGenContext, ChunkStatus chunkStatus, Executor executor, ToFullChunk toFullChunk, List<ChunkAccess> list, ChunkAccess chunkAccess
	) {
		ServerLevel serverLevel = worldGenContext.level();
		WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, -1);
		return worldGenContext.generator()
			.createBiomes(
				executor,
				serverLevel.getChunkSource().randomState(),
				Blender.of(worldGenRegion),
				serverLevel.structureManager().forWorldGenRegion(worldGenRegion),
				chunkAccess
			);
	}

	static CompletableFuture<ChunkAccess> generateNoise(
		WorldGenContext worldGenContext, ChunkStatus chunkStatus, Executor executor, ToFullChunk toFullChunk, List<ChunkAccess> list, ChunkAccess chunkAccess
	) {
		ServerLevel serverLevel = worldGenContext.level();
		WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, 0);
		return worldGenContext.generator()
			.fillFromNoise(
				executor,
				Blender.of(worldGenRegion),
				serverLevel.getChunkSource().randomState(),
				serverLevel.structureManager().forWorldGenRegion(worldGenRegion),
				chunkAccess
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
				}

				return chunkAccessx;
			});
	}

	static CompletableFuture<ChunkAccess> generateSurface(
		WorldGenContext worldGenContext, ChunkStatus chunkStatus, Executor executor, ToFullChunk toFullChunk, List<ChunkAccess> list, ChunkAccess chunkAccess
	) {
		ServerLevel serverLevel = worldGenContext.level();
		WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, 0);
		worldGenContext.generator()
			.buildSurface(worldGenRegion, serverLevel.structureManager().forWorldGenRegion(worldGenRegion), serverLevel.getChunkSource().randomState(), chunkAccess);
		return CompletableFuture.completedFuture(chunkAccess);
	}

	static CompletableFuture<ChunkAccess> generateCarvers(
		WorldGenContext worldGenContext, ChunkStatus chunkStatus, Executor executor, ToFullChunk toFullChunk, List<ChunkAccess> list, ChunkAccess chunkAccess
	) {
		ServerLevel serverLevel = worldGenContext.level();
		WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, 0);
		if (chunkAccess instanceof ProtoChunk protoChunk) {
			Blender.addAroundOldChunksCarvingMaskFilter(worldGenRegion, protoChunk);
		}

		worldGenContext.generator()
			.applyCarvers(
				worldGenRegion,
				serverLevel.getSeed(),
				serverLevel.getChunkSource().randomState(),
				serverLevel.getBiomeManager(),
				serverLevel.structureManager().forWorldGenRegion(worldGenRegion),
				chunkAccess,
				GenerationStep.Carving.AIR
			);
		return CompletableFuture.completedFuture(chunkAccess);
	}

	static CompletableFuture<ChunkAccess> generateFeatures(
		WorldGenContext worldGenContext, ChunkStatus chunkStatus, Executor executor, ToFullChunk toFullChunk, List<ChunkAccess> list, ChunkAccess chunkAccess
	) {
		ServerLevel serverLevel = worldGenContext.level();
		Heightmap.primeHeightmaps(
			chunkAccess,
			EnumSet.of(Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE)
		);
		WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, list, chunkStatus, 1);
		worldGenContext.generator().applyBiomeDecoration(worldGenRegion, chunkAccess, serverLevel.structureManager().forWorldGenRegion(worldGenRegion));
		Blender.generateBorderTicks(worldGenRegion, chunkAccess);
		return CompletableFuture.completedFuture(chunkAccess);
	}

	static CompletableFuture<ChunkAccess> generateInitializeLight(
		WorldGenContext worldGenContext, ChunkStatus chunkStatus, Executor executor, ToFullChunk toFullChunk, List<ChunkAccess> list, ChunkAccess chunkAccess
	) {
		return initializeLight(worldGenContext.lightEngine(), chunkAccess);
	}

	static CompletableFuture<ChunkAccess> loadInitializeLight(
		WorldGenContext worldGenContext, ChunkStatus chunkStatus, ToFullChunk toFullChunk, ChunkAccess chunkAccess
	) {
		return initializeLight(worldGenContext.lightEngine(), chunkAccess);
	}

	private static CompletableFuture<ChunkAccess> initializeLight(ThreadedLevelLightEngine threadedLevelLightEngine, ChunkAccess chunkAccess) {
		chunkAccess.initializeLightSources();
		((ProtoChunk)chunkAccess).setLightEngine(threadedLevelLightEngine);
		boolean bl = isLighted(chunkAccess);
		return threadedLevelLightEngine.initializeLight(chunkAccess, bl);
	}

	static CompletableFuture<ChunkAccess> generateLight(
		WorldGenContext worldGenContext, ChunkStatus chunkStatus, Executor executor, ToFullChunk toFullChunk, List<ChunkAccess> list, ChunkAccess chunkAccess
	) {
		return lightChunk(worldGenContext.lightEngine(), chunkAccess);
	}

	static CompletableFuture<ChunkAccess> loadLight(WorldGenContext worldGenContext, ChunkStatus chunkStatus, ToFullChunk toFullChunk, ChunkAccess chunkAccess) {
		return lightChunk(worldGenContext.lightEngine(), chunkAccess);
	}

	private static CompletableFuture<ChunkAccess> lightChunk(ThreadedLevelLightEngine threadedLevelLightEngine, ChunkAccess chunkAccess) {
		boolean bl = isLighted(chunkAccess);
		return threadedLevelLightEngine.lightChunk(chunkAccess, bl);
	}

	static CompletableFuture<ChunkAccess> generateSpawn(
		WorldGenContext worldGenContext, ChunkStatus chunkStatus, Executor executor, ToFullChunk toFullChunk, List<ChunkAccess> list, ChunkAccess chunkAccess
	) {
		if (!chunkAccess.isUpgrading()) {
			worldGenContext.generator().spawnOriginalMobs(new WorldGenRegion(worldGenContext.level(), list, chunkStatus, -1));
		}

		return CompletableFuture.completedFuture(chunkAccess);
	}

	static CompletableFuture<ChunkAccess> generateFull(
		WorldGenContext worldGenContext, ChunkStatus chunkStatus, Executor executor, ToFullChunk toFullChunk, List<ChunkAccess> list, ChunkAccess chunkAccess
	) {
		return toFullChunk.apply(chunkAccess);
	}

	static CompletableFuture<ChunkAccess> loadFull(WorldGenContext worldGenContext, ChunkStatus chunkStatus, ToFullChunk toFullChunk, ChunkAccess chunkAccess) {
		return toFullChunk.apply(chunkAccess);
	}
}
