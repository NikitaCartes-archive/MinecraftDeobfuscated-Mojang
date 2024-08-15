package net.minecraft.world.level.chunk.status;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.Blender;

public class ChunkStatusTasks {
	private static boolean isLighted(ChunkAccess chunkAccess) {
		return chunkAccess.getPersistedStatus().isOrAfter(ChunkStatus.LIGHT) && chunkAccess.isLightCorrect();
	}

	static CompletableFuture<ChunkAccess> passThrough(
		WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess
	) {
		return CompletableFuture.completedFuture(chunkAccess);
	}

	static CompletableFuture<ChunkAccess> generateStructureStarts(
		WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess
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
		WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess
	) {
		worldGenContext.level().onStructureStartsAvailable(chunkAccess);
		return CompletableFuture.completedFuture(chunkAccess);
	}

	static CompletableFuture<ChunkAccess> generateStructureReferences(
		WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess
	) {
		ServerLevel serverLevel = worldGenContext.level();
		WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, staticCache2D, chunkStep, chunkAccess);
		worldGenContext.generator().createReferences(worldGenRegion, serverLevel.structureManager().forWorldGenRegion(worldGenRegion), chunkAccess);
		return CompletableFuture.completedFuture(chunkAccess);
	}

	static CompletableFuture<ChunkAccess> generateBiomes(
		WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess
	) {
		ServerLevel serverLevel = worldGenContext.level();
		WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, staticCache2D, chunkStep, chunkAccess);
		return worldGenContext.generator()
			.createBiomes(
				serverLevel.getChunkSource().randomState(), Blender.of(worldGenRegion), serverLevel.structureManager().forWorldGenRegion(worldGenRegion), chunkAccess
			);
	}

	static CompletableFuture<ChunkAccess> generateNoise(
		WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess
	) {
		ServerLevel serverLevel = worldGenContext.level();
		WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, staticCache2D, chunkStep, chunkAccess);
		return worldGenContext.generator()
			.fillFromNoise(
				Blender.of(worldGenRegion), serverLevel.getChunkSource().randomState(), serverLevel.structureManager().forWorldGenRegion(worldGenRegion), chunkAccess
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
		WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess
	) {
		ServerLevel serverLevel = worldGenContext.level();
		WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, staticCache2D, chunkStep, chunkAccess);
		worldGenContext.generator()
			.buildSurface(worldGenRegion, serverLevel.structureManager().forWorldGenRegion(worldGenRegion), serverLevel.getChunkSource().randomState(), chunkAccess);
		return CompletableFuture.completedFuture(chunkAccess);
	}

	static CompletableFuture<ChunkAccess> generateCarvers(
		WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess
	) {
		ServerLevel serverLevel = worldGenContext.level();
		WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, staticCache2D, chunkStep, chunkAccess);
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
				chunkAccess
			);
		return CompletableFuture.completedFuture(chunkAccess);
	}

	static CompletableFuture<ChunkAccess> generateFeatures(
		WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess
	) {
		ServerLevel serverLevel = worldGenContext.level();
		Heightmap.primeHeightmaps(
			chunkAccess,
			EnumSet.of(Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE)
		);
		WorldGenRegion worldGenRegion = new WorldGenRegion(serverLevel, staticCache2D, chunkStep, chunkAccess);
		worldGenContext.generator().applyBiomeDecoration(worldGenRegion, chunkAccess, serverLevel.structureManager().forWorldGenRegion(worldGenRegion));
		Blender.generateBorderTicks(worldGenRegion, chunkAccess);
		return CompletableFuture.completedFuture(chunkAccess);
	}

	static CompletableFuture<ChunkAccess> initializeLight(
		WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess
	) {
		ThreadedLevelLightEngine threadedLevelLightEngine = worldGenContext.lightEngine();
		chunkAccess.initializeLightSources();
		((ProtoChunk)chunkAccess).setLightEngine(threadedLevelLightEngine);
		boolean bl = isLighted(chunkAccess);
		return threadedLevelLightEngine.initializeLight(chunkAccess, bl);
	}

	static CompletableFuture<ChunkAccess> light(
		WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess
	) {
		boolean bl = isLighted(chunkAccess);
		return worldGenContext.lightEngine().lightChunk(chunkAccess, bl);
	}

	static CompletableFuture<ChunkAccess> generateSpawn(
		WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess
	) {
		if (!chunkAccess.isUpgrading()) {
			worldGenContext.generator().spawnOriginalMobs(new WorldGenRegion(worldGenContext.level(), staticCache2D, chunkStep, chunkAccess));
		}

		return CompletableFuture.completedFuture(chunkAccess);
	}

	static CompletableFuture<ChunkAccess> full(
		WorldGenContext worldGenContext, ChunkStep chunkStep, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess
	) {
		ChunkPos chunkPos = chunkAccess.getPos();
		GenerationChunkHolder generationChunkHolder = staticCache2D.get(chunkPos.x, chunkPos.z);
		return CompletableFuture.supplyAsync(() -> {
			ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
			ServerLevel serverLevel = worldGenContext.level();
			LevelChunk levelChunk;
			if (protoChunk instanceof ImposterProtoChunk imposterProtoChunk) {
				levelChunk = imposterProtoChunk.getWrapped();
			} else {
				levelChunk = new LevelChunk(serverLevel, protoChunk, levelChunkx -> postLoadProtoChunk(serverLevel, protoChunk.getEntities()));
				generationChunkHolder.replaceProtoChunk(new ImposterProtoChunk(levelChunk, false));
			}

			levelChunk.setFullStatus(generationChunkHolder::getFullStatus);
			levelChunk.runPostLoad();
			levelChunk.setLoaded(true);
			levelChunk.registerAllBlockEntitiesAfterLevelLoad();
			levelChunk.registerTickContainerInLevel(serverLevel);
			return levelChunk;
		}, worldGenContext.mainThreadExecutor());
	}

	private static void postLoadProtoChunk(ServerLevel serverLevel, List<CompoundTag> list) {
		if (!list.isEmpty()) {
			serverLevel.addWorldGenChunkEntities(EntityType.loadEntitiesRecursive(list, serverLevel, EntitySpawnReason.LOAD));
		}
	}
}
