package net.minecraft.world.level.levelgen.placement;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class PlacementContext extends WorldGenerationContext {
	private final WorldGenLevel level;
	private final ChunkGenerator generator;
	private final Optional<PlacedFeature> topFeature;

	public PlacementContext(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Optional<PlacedFeature> optional) {
		super(chunkGenerator, worldGenLevel);
		this.level = worldGenLevel;
		this.generator = chunkGenerator;
		this.topFeature = optional;
	}

	public int getHeight(Heightmap.Types types, int i, int j) {
		return this.level.getHeight(types, i, j);
	}

	public CarvingMask getCarvingMask(ChunkPos chunkPos) {
		return ((ProtoChunk)this.level.getChunk(chunkPos.x, chunkPos.z)).getOrCreateCarvingMask();
	}

	public BlockState getBlockState(BlockPos blockPos) {
		return this.level.getBlockState(blockPos);
	}

	public int getMinY() {
		return this.level.getMinY();
	}

	public WorldGenLevel getLevel() {
		return this.level;
	}

	public Optional<PlacedFeature> topFeature() {
		return this.topFeature;
	}

	public ChunkGenerator generator() {
		return this.generator;
	}
}
