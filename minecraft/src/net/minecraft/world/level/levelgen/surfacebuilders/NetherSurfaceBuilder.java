package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class NetherSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
	private static final BlockState SOUL_SAND = Blocks.SOUL_SAND.defaultBlockState();
	protected long seed;
	protected PerlinNoise decorationNoise;

	public NetherSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
		super(codec);
	}

	public void apply(
		Random random,
		BlockColumn blockColumn,
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
		int o = l;
		double e = 0.03125;
		boolean bl = this.decorationNoise.getValue((double)i * 0.03125, (double)j * 0.03125, 0.0) * 75.0 + random.nextDouble() > 0.0;
		boolean bl2 = this.decorationNoise.getValue((double)i * 0.03125, 109.0, (double)j * 0.03125) * 75.0 + random.nextDouble() > 0.0;
		int p = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		int q = -1;
		BlockState blockState3 = surfaceBuilderBaseConfiguration.getTopMaterial();
		BlockState blockState4 = surfaceBuilderBaseConfiguration.getUnderMaterial();

		for (int r = 127; r >= m; r--) {
			BlockState blockState5 = blockColumn.getBlock(r);
			if (blockState5.isAir()) {
				q = -1;
			} else if (blockState5.is(blockState.getBlock())) {
				if (q == -1) {
					boolean bl3 = false;
					if (p <= 0) {
						bl3 = true;
						blockState4 = surfaceBuilderBaseConfiguration.getUnderMaterial();
					} else if (r >= o - 4 && r <= o + 1) {
						blockState3 = surfaceBuilderBaseConfiguration.getTopMaterial();
						blockState4 = surfaceBuilderBaseConfiguration.getUnderMaterial();
						if (bl2) {
							blockState3 = GRAVEL;
							blockState4 = surfaceBuilderBaseConfiguration.getUnderMaterial();
						}

						if (bl) {
							blockState3 = SOUL_SAND;
							blockState4 = SOUL_SAND;
						}
					}

					if (r < o && bl3) {
						blockState3 = blockState2;
					}

					q = p;
					if (r >= o - 1) {
						blockColumn.setBlock(r, blockState3);
					} else {
						blockColumn.setBlock(r, blockState4);
					}
				} else if (q > 0) {
					q--;
					blockColumn.setBlock(r, blockState4);
				}
			}
		}
	}

	@Override
	public void initNoise(long l) {
		if (this.seed != l || this.decorationNoise == null) {
			this.decorationNoise = new PerlinNoise(new WorldgenRandom(l), IntStream.rangeClosed(-3, 0));
		}

		this.seed = l;
	}
}
