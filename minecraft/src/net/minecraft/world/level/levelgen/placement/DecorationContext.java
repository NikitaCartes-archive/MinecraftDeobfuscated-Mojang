package net.minecraft.world.level.levelgen.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class DecorationContext extends WorldGenerationContext {
	private final WorldGenLevel level;

	public DecorationContext(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator) {
		super(chunkGenerator, worldGenLevel);
		this.level = worldGenLevel;
	}

	public int getHeight(Heightmap.Types types, int i, int j) {
		return this.level.getHeight(types, i, j);
	}

	public CarvingMask getCarvingMask(ChunkPos chunkPos, GenerationStep.Carving carving) {
		return ((ProtoChunk)this.level.getChunk(chunkPos.x, chunkPos.z)).getOrCreateCarvingMask(carving);
	}

	public BlockState getBlockState(BlockPos blockPos) {
		return this.level.getBlockState(blockPos);
	}

	public int getMinBuildHeight() {
		return this.level.getMinBuildHeight();
	}

	public WorldGenLevel getLevel() {
		return this.level;
	}
}
