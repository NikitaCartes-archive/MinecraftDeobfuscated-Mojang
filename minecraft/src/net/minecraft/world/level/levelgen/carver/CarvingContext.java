package net.minecraft.world.level.levelgen.carver;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class CarvingContext extends WorldGenerationContext {
	private final NoiseBasedChunkGenerator generator;
	private final RegistryAccess registryAccess;
	private final NoiseChunk noiseChunk;

	public CarvingContext(
		NoiseBasedChunkGenerator noiseBasedChunkGenerator, RegistryAccess registryAccess, LevelHeightAccessor levelHeightAccessor, NoiseChunk noiseChunk
	) {
		super(noiseBasedChunkGenerator, levelHeightAccessor);
		this.generator = noiseBasedChunkGenerator;
		this.registryAccess = registryAccess;
		this.noiseChunk = noiseChunk;
	}

	@Deprecated
	public Optional<BlockState> topMaterial(Function<BlockPos, Holder<Biome>> function, ChunkAccess chunkAccess, BlockPos blockPos, boolean bl) {
		return this.generator.topMaterial(this, function, chunkAccess, this.noiseChunk, blockPos, bl);
	}

	@Deprecated
	public RegistryAccess registryAccess() {
		return this.registryAccess;
	}
}
