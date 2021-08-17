package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class StripedStoneSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	private static final float STRIPE_MATERIAL_THRESHOLD = 0.3F;
	private static final float STRIPE_THRESHOLD = 0.025F;
	private long seed;
	private NormalNoise stripeMaterialNoise;
	private NormalNoise stripeNoise;

	public StripedStoneSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
		super(codec);
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
		int m,
		long n,
		SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration
	) {
		double e = this.stripeNoise.getValue((double)i, (double)j, (double)k);
		SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration2;
		if (e > -0.025F && e < 0.025F) {
			double f = this.stripeMaterialNoise.getValue((double)i, (double)k, (double)j);
			surfaceBuilderBaseConfiguration2 = this.getStripeMaterial(f);
		} else {
			surfaceBuilderBaseConfiguration2 = SurfaceBuilder.CONFIG_STONE;
		}

		SurfaceBuilder.DEFAULT.apply(random, chunkAccess, biome, i, j, k, d, blockState, blockState2, l, m, n, surfaceBuilderBaseConfiguration2);
	}

	protected SurfaceBuilderBaseConfiguration getStripeMaterial(double d) {
		SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration;
		if (d < -0.3F) {
			surfaceBuilderBaseConfiguration = SurfaceBuilder.CONFIG_DIORITE;
		} else if (d < 0.0) {
			surfaceBuilderBaseConfiguration = SurfaceBuilder.CONFIG_ANDESITE;
		} else if (d < 0.3F) {
			surfaceBuilderBaseConfiguration = SurfaceBuilder.CONFIG_GRAVEL;
		} else {
			surfaceBuilderBaseConfiguration = SurfaceBuilder.CONFIG_GRANITE;
		}

		return surfaceBuilderBaseConfiguration;
	}

	@Override
	public void initNoise(long l) {
		if (this.seed != l) {
			WorldgenRandom worldgenRandom = new WorldgenRandom(l);
			this.stripeNoise = NormalNoise.create(worldgenRandom, -7, 1.0, 1.0, 0.0);
			this.stripeMaterialNoise = NormalNoise.create(worldgenRandom, -8, 1.0);
		}

		this.seed = l;
	}
}
