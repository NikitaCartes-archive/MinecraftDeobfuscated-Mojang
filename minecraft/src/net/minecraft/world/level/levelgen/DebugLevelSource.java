package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class DebugLevelSource extends ChunkGenerator {
	public static final Codec<DebugLevelSource> CODEC = RegistryLookupCodec.create(Registry.BIOME_REGISTRY)
		.<DebugLevelSource>xmap(DebugLevelSource::new, DebugLevelSource::biomes)
		.stable()
		.codec();
	private static final int BLOCK_MARGIN = 2;
	private static final List<BlockState> ALL_BLOCKS = (List<BlockState>)StreamSupport.stream(Registry.BLOCK.spliterator(), false)
		.flatMap(block -> block.getStateDefinition().getPossibleStates().stream())
		.collect(Collectors.toList());
	private static final int GRID_WIDTH = Mth.ceil(Mth.sqrt((float)ALL_BLOCKS.size()));
	private static final int GRID_HEIGHT = Mth.ceil((float)ALL_BLOCKS.size() / (float)GRID_WIDTH);
	protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
	protected static final BlockState BARRIER = Blocks.BARRIER.defaultBlockState();
	public static final int HEIGHT = 70;
	public static final int BARRIER_HEIGHT = 60;
	private final Registry<Biome> biomes;

	public DebugLevelSource(Registry<Biome> registry) {
		super(new FixedBiomeSource(registry.getOrThrow(Biomes.PLAINS)), new StructureSettings(false));
		this.biomes = registry;
	}

	public Registry<Biome> biomes() {
		return this.biomes;
	}

	@Override
	protected Codec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	@Override
	public ChunkGenerator withSeed(long l) {
		return this;
	}

	@Override
	public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
	}

	@Override
	public void applyCarvers(long l, BiomeManager biomeManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
	}

	@Override
	public void applyBiomeDecoration(WorldGenRegion worldGenRegion, StructureFeatureManager structureFeatureManager) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		ChunkPos chunkPos = worldGenRegion.getCenter();

		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				int k = SectionPos.sectionToBlockCoord(chunkPos.x, i);
				int l = SectionPos.sectionToBlockCoord(chunkPos.z, j);
				worldGenRegion.setBlock(mutableBlockPos.set(k, 60, l), BARRIER, 2);
				BlockState blockState = getBlockStateFor(k, l);
				if (blockState != null) {
					worldGenRegion.setBlock(mutableBlockPos.set(k, 70, l), blockState, 2);
				}
			}
		}
	}

	@Override
	public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
		return CompletableFuture.completedFuture(chunkAccess);
	}

	@Override
	public int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
		return 0;
	}

	@Override
	public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor) {
		return new NoiseColumn(0, new BlockState[0]);
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
}
