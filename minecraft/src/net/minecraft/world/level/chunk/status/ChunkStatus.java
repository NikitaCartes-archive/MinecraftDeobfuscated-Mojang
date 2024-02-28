package net.minecraft.world.level.chunk.status;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public class ChunkStatus {
	public static final int MAX_STRUCTURE_DISTANCE = 8;
	private static final EnumSet<Heightmap.Types> PRE_FEATURES = EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG);
	public static final EnumSet<Heightmap.Types> POST_FEATURES = EnumSet.of(
		Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE, Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES
	);
	public static final ChunkStatus EMPTY = register(
		"empty", null, -1, false, PRE_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateEmpty, ChunkStatusTasks::loadPassThrough
	);
	public static final ChunkStatus STRUCTURE_STARTS = register(
		"structure_starts", EMPTY, 0, false, PRE_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateStructureStarts, ChunkStatusTasks::loadStructureStarts
	);
	public static final ChunkStatus STRUCTURE_REFERENCES = register(
		"structure_references",
		STRUCTURE_STARTS,
		8,
		false,
		PRE_FEATURES,
		ChunkType.PROTOCHUNK,
		ChunkStatusTasks::generateStructureReferences,
		ChunkStatusTasks::loadPassThrough
	);
	public static final ChunkStatus BIOMES = register(
		"biomes", STRUCTURE_REFERENCES, 8, false, PRE_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateBiomes, ChunkStatusTasks::loadPassThrough
	);
	public static final ChunkStatus NOISE = register(
		"noise", BIOMES, 8, false, PRE_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateNoise, ChunkStatusTasks::loadPassThrough
	);
	public static final ChunkStatus SURFACE = register(
		"surface", NOISE, 8, false, PRE_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateSurface, ChunkStatusTasks::loadPassThrough
	);
	public static final ChunkStatus CARVERS = register(
		"carvers", SURFACE, 8, false, POST_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateCarvers, ChunkStatusTasks::loadPassThrough
	);
	public static final ChunkStatus FEATURES = register(
		"features", CARVERS, 8, false, POST_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateFeatures, ChunkStatusTasks::loadPassThrough
	);
	public static final ChunkStatus INITIALIZE_LIGHT = register(
		"initialize_light", FEATURES, 0, false, POST_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateInitializeLight, ChunkStatusTasks::loadInitializeLight
	);
	public static final ChunkStatus LIGHT = register(
		"light", INITIALIZE_LIGHT, 1, true, POST_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateLight, ChunkStatusTasks::loadLight
	);
	public static final ChunkStatus SPAWN = register(
		"spawn", LIGHT, 1, false, POST_FEATURES, ChunkType.PROTOCHUNK, ChunkStatusTasks::generateSpawn, ChunkStatusTasks::loadPassThrough
	);
	public static final ChunkStatus FULL = register(
		"full", SPAWN, 0, false, POST_FEATURES, ChunkType.LEVELCHUNK, ChunkStatusTasks::generateFull, ChunkStatusTasks::loadFull
	);
	private static final List<ChunkStatus> STATUS_BY_RANGE = ImmutableList.of(
		FULL,
		INITIALIZE_LIGHT,
		CARVERS,
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
	private final int index;
	private final ChunkStatus parent;
	private final ChunkStatus.GenerationTask generationTask;
	private final ChunkStatus.LoadingTask loadingTask;
	private final int range;
	private final boolean hasLoadDependencies;
	private final ChunkType chunkType;
	private final EnumSet<Heightmap.Types> heightmapsAfter;

	private static ChunkStatus register(
		String string,
		@Nullable ChunkStatus chunkStatus,
		int i,
		boolean bl,
		EnumSet<Heightmap.Types> enumSet,
		ChunkType chunkType,
		ChunkStatus.GenerationTask generationTask,
		ChunkStatus.LoadingTask loadingTask
	) {
		return Registry.register(BuiltInRegistries.CHUNK_STATUS, string, new ChunkStatus(chunkStatus, i, bl, enumSet, chunkType, generationTask, loadingTask));
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
		@Nullable ChunkStatus chunkStatus,
		int i,
		boolean bl,
		EnumSet<Heightmap.Types> enumSet,
		ChunkType chunkType,
		ChunkStatus.GenerationTask generationTask,
		ChunkStatus.LoadingTask loadingTask
	) {
		this.parent = chunkStatus == null ? this : chunkStatus;
		this.generationTask = generationTask;
		this.loadingTask = loadingTask;
		this.range = i;
		this.hasLoadDependencies = bl;
		this.chunkType = chunkType;
		this.heightmapsAfter = enumSet;
		this.index = chunkStatus == null ? 0 : chunkStatus.getIndex() + 1;
	}

	public int getIndex() {
		return this.index;
	}

	public ChunkStatus getParent() {
		return this.parent;
	}

	public CompletableFuture<ChunkAccess> generate(WorldGenContext worldGenContext, Executor executor, ToFullChunk toFullChunk, List<ChunkAccess> list) {
		ChunkAccess chunkAccess = (ChunkAccess)list.get(list.size() / 2);
		ProfiledDuration profiledDuration = JvmProfiler.INSTANCE.onChunkGenerate(chunkAccess.getPos(), worldGenContext.level().dimension(), this.toString());
		return this.generationTask.doWork(worldGenContext, this, executor, toFullChunk, list, chunkAccess).thenApply(chunkAccessx -> {
			if (chunkAccessx instanceof ProtoChunk protoChunk && !protoChunk.getStatus().isOrAfter(this)) {
				protoChunk.setStatus(this);
			}

			if (profiledDuration != null) {
				profiledDuration.finish();
			}

			return chunkAccessx;
		});
	}

	public CompletableFuture<ChunkAccess> load(WorldGenContext worldGenContext, ToFullChunk toFullChunk, ChunkAccess chunkAccess) {
		return this.loadingTask.doWork(worldGenContext, this, toFullChunk, chunkAccess);
	}

	public int getRange() {
		return this.range;
	}

	public boolean hasLoadDependencies() {
		return this.hasLoadDependencies;
	}

	public ChunkType getChunkType() {
		return this.chunkType;
	}

	public static ChunkStatus byName(String string) {
		return BuiltInRegistries.CHUNK_STATUS.get(ResourceLocation.tryParse(string));
	}

	public EnumSet<Heightmap.Types> heightmapsAfter() {
		return this.heightmapsAfter;
	}

	public boolean isOrAfter(ChunkStatus chunkStatus) {
		return this.getIndex() >= chunkStatus.getIndex();
	}

	public String toString() {
		return BuiltInRegistries.CHUNK_STATUS.getKey(this).toString();
	}

	@FunctionalInterface
	protected interface GenerationTask {
		CompletableFuture<ChunkAccess> doWork(
			WorldGenContext worldGenContext, ChunkStatus chunkStatus, Executor executor, ToFullChunk toFullChunk, List<ChunkAccess> list, ChunkAccess chunkAccess
		);
	}

	@FunctionalInterface
	protected interface LoadingTask {
		CompletableFuture<ChunkAccess> doWork(WorldGenContext worldGenContext, ChunkStatus chunkStatus, ToFullChunk toFullChunk, ChunkAccess chunkAccess);
	}
}
