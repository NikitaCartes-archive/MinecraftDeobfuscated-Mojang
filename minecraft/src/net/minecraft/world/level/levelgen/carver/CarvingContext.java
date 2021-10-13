package net.minecraft.world.level.levelgen.carver;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class CarvingContext extends WorldGenerationContext {
	private final NoiseBasedChunkGenerator generator;
	private final RegistryAccess registryAccess;

	public CarvingContext(NoiseBasedChunkGenerator noiseBasedChunkGenerator, RegistryAccess registryAccess, LevelHeightAccessor levelHeightAccessor) {
		super(noiseBasedChunkGenerator, levelHeightAccessor);
		this.generator = noiseBasedChunkGenerator;
		this.registryAccess = registryAccess;
	}

	@Deprecated
	public Optional<BlockState> topMaterial(Biome biome, ChunkAccess chunkAccess, BlockPos blockPos, boolean bl) {
		return this.generator.topMaterial(this, biome, chunkAccess, blockPos, bl);
	}

	@Deprecated
	public RegistryAccess registryAccess() {
		return this.registryAccess;
	}
}
