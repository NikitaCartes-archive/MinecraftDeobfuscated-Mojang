package net.minecraft.world.level.levelgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blending.Blender;

public class DebugLevelSource extends ChunkGenerator {
	public static final MapCodec<DebugLevelSource> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(RegistryOps.retrieveElement(Biomes.PLAINS)).apply(instance, instance.stable(DebugLevelSource::new))
	);
	private static final int BLOCK_MARGIN = 2;
	private static final List<BlockState> ALL_BLOCKS = (List<BlockState>)StreamSupport.stream(BuiltInRegistries.BLOCK.spliterator(), false)
		.flatMap(block -> block.getStateDefinition().getPossibleStates().stream())
		.collect(Collectors.toList());
	private static final int GRID_WIDTH = Mth.ceil(Mth.sqrt((float)ALL_BLOCKS.size()));
	private static final int GRID_HEIGHT = Mth.ceil((float)ALL_BLOCKS.size() / (float)GRID_WIDTH);
	protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
	protected static final BlockState BARRIER = Blocks.BARRIER.defaultBlockState();
	public static final int HEIGHT = 70;
	public static final int BARRIER_HEIGHT = 60;

	public DebugLevelSource(Holder.Reference<Biome> reference) {
		super(new FixedBiomeSource(reference));
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	@Override
	public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) {
	}

	@Override
	public void applyBiomeDecoration(WorldGenLevel worldGenLevel, ChunkAccess chunkAccess, StructureManager structureManager) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		ChunkPos chunkPos = chunkAccess.getPos();
		int i = chunkPos.x;
		int j = chunkPos.z;

		for (int k = 0; k < 16; k++) {
			for (int l = 0; l < 16; l++) {
				int m = SectionPos.sectionToBlockCoord(i, k);
				int n = SectionPos.sectionToBlockCoord(j, l);
				worldGenLevel.setBlock(mutableBlockPos.set(m, 60, n), BARRIER, 2);
				BlockState blockState = getBlockStateFor(m, n);
				worldGenLevel.setBlock(mutableBlockPos.set(m, 70, n), blockState, 2);
			}
		}
	}

	@Override
	public CompletableFuture<ChunkAccess> fillFromNoise(
		Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess
	) {
		return CompletableFuture.completedFuture(chunkAccess);
	}

	@Override
	public int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
		return 0;
	}

	@Override
	public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
		return new NoiseColumn(0, new BlockState[0]);
	}

	@Override
	public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {
	}

	public static BlockState getBlockStateFor(int i, int j) {
		BlockState blockState = AIR;
		if (i > 0 && j > 0 && i % 2 != 0 && j % 2 != 0) {
			i /= 2;
			j /= 2;
			if (i <= GRID_WIDTH && j <= GRID_HEIGHT) {
				int k = Mth.abs(i * GRID_WIDTH + j);
				if (k < ALL_BLOCKS.size()) {
					blockState = (BlockState)ALL_BLOCKS.get(k);
				}
			}
		}

		return blockState;
	}

	@Override
	public void applyCarvers(
		WorldGenRegion worldGenRegion,
		long l,
		RandomState randomState,
		BiomeManager biomeManager,
		StructureManager structureManager,
		ChunkAccess chunkAccess,
		GenerationStep.Carving carving
	) {
	}

	@Override
	public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
	}

	@Override
	public int getMinY() {
		return 0;
	}

	@Override
	public int getGenDepth() {
		return 384;
	}

	@Override
	public int getSeaLevel() {
		return 63;
	}
}
