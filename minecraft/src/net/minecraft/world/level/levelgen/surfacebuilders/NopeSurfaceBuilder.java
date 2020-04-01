package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class NopeSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	public NopeSurfaceBuilder(
		Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration> function, Function<Random, ? extends SurfaceBuilderBaseConfiguration> function2
	) {
		super(function, function2);
	}

	public void apply(
		Random random,
		ChunkAccess chunkAccess,
		Biome biome,
		int i,
		int j,
		int k,
		double d,
		BlockState blockState,
		BlockState blockState2,
		int l,
		long m,
		SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration
	) {
	}
}
