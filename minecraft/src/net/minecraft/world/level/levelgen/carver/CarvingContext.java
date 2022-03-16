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
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class CarvingContext extends WorldGenerationContext {
	private final RegistryAccess registryAccess;
	private final NoiseChunk noiseChunk;
	private final RandomState randomState;
	private final SurfaceRules.RuleSource surfaceRule;

	public CarvingContext(
		NoiseBasedChunkGenerator noiseBasedChunkGenerator,
		RegistryAccess registryAccess,
		LevelHeightAccessor levelHeightAccessor,
		NoiseChunk noiseChunk,
		RandomState randomState,
		SurfaceRules.RuleSource ruleSource
	) {
		super(noiseBasedChunkGenerator, levelHeightAccessor);
		this.registryAccess = registryAccess;
		this.noiseChunk = noiseChunk;
		this.randomState = randomState;
		this.surfaceRule = ruleSource;
	}

	@Deprecated
	public Optional<BlockState> topMaterial(Function<BlockPos, Holder<Biome>> function, ChunkAccess chunkAccess, BlockPos blockPos, boolean bl) {
		return this.randomState.surfaceSystem().topMaterial(this.surfaceRule, this, function, chunkAccess, this.noiseChunk, blockPos, bl);
	}

	@Deprecated
	public RegistryAccess registryAccess() {
		return this.registryAccess;
	}

	public RandomState randomState() {
		return this.randomState;
	}
}
