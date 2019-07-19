package net.minecraft.world.level.levelgen;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class DebugLevelSource extends ChunkGenerator<DebugGeneratorSettings> {
	private static final List<BlockState> ALL_BLOCKS = (List<BlockState>)StreamSupport.stream(Registry.BLOCK.spliterator(), false)
		.flatMap(block -> block.getStateDefinition().getPossibleStates().stream())
		.collect(Collectors.toList());
	private static final int GRID_WIDTH = Mth.ceil(Mth.sqrt((float)ALL_BLOCKS.size()));
	private static final int GRID_HEIGHT = Mth.ceil((float)ALL_BLOCKS.size() / (float)GRID_WIDTH);
	protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
	protected static final BlockState BARRIER = Blocks.BARRIER.defaultBlockState();

	public DebugLevelSource(LevelAccessor levelAccessor, BiomeSource biomeSource, DebugGeneratorSettings debugGeneratorSettings) {
		super(levelAccessor, biomeSource, debugGeneratorSettings);
	}

	@Override
	public void buildSurfaceAndBedrock(ChunkAccess chunkAccess) {
	}

	@Override
	public void applyCarvers(ChunkAccess chunkAccess, GenerationStep.Carving carving) {
	}

	@Override
	public int getSpawnHeight() {
		return this.level.getSeaLevel() + 1;
	}

	@Override
	public void applyBiomeDecoration(WorldGenRegion worldGenRegion) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int i = worldGenRegion.getCenterX();
		int j = worldGenRegion.getCenterZ();

		for (int k = 0; k < 16; k++) {
			for (int l = 0; l < 16; l++) {
				int m = (i << 4) + k;
				int n = (j << 4) + l;
				worldGenRegion.setBlock(mutableBlockPos.set(m, 60, n), BARRIER, 2);
				BlockState blockState = getBlockStateFor(m, n);
				if (blockState != null) {
					worldGenRegion.setBlock(mutableBlockPos.set(m, 70, n), blockState, 2);
				}
			}
		}
	}

	@Override
	public void fillFromNoise(LevelAccessor levelAccessor, ChunkAccess chunkAccess) {
	}

	@Override
	public int getBaseHeight(int i, int j, Heightmap.Types types) {
		return 0;
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
