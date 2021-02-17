package net.minecraft.world.level.levelgen.placement;

import java.util.BitSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;

public class DecorationContext {
	private final WorldGenLevel level;
	private final ChunkGenerator generator;

	public DecorationContext(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator) {
		this.level = worldGenLevel;
		this.generator = chunkGenerator;
	}

	public int getHeight(Heightmap.Types types, int i, int j) {
		return this.level.getHeight(types, i, j);
	}

	public int getMinGenY() {
		return this.generator.getMinY();
	}

	public int getGenDepth() {
		return this.generator.getGenDepth();
	}

	public BitSet getCarvingMask(ChunkPos chunkPos, GenerationStep.Carving carving) {
		return ((ProtoChunk)this.level.getChunk(chunkPos.x, chunkPos.z)).getOrCreateCarvingMask(carving);
	}

	public BlockState getBlockState(BlockPos blockPos) {
		return this.level.getBlockState(blockPos);
	}

	public int getMinBuildHeight() {
		return this.level.getMinBuildHeight();
	}
}
