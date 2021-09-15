package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class NetherForestSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	protected long seed;
	private PerlinNoise decorationNoise;

	public NetherForestSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
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
		double e = this.decorationNoise.getValue((double)i * 0.1, (double)l, (double)j * 0.1);
		boolean bl = e > 0.15 + random.nextDouble() * 0.35;
		double f = this.decorationNoise.getValue((double)i * 0.1, 109.0, (double)j * 0.1);
		boolean bl2 = f > 0.25 + random.nextDouble() * 0.9;
		int o = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		int p = -1;
		BlockState blockState3 = surfaceBuilderBaseConfiguration.getUnderMaterial();

		for (int q = 127; q >= m; q--) {
			BlockState blockState4 = surfaceBuilderBaseConfiguration.getTopMaterial();
			BlockState blockState5 = blockColumn.getBlock(q);
			if (blockState5.isAir()) {
				p = -1;
			} else if (blockState5.is(blockState.getBlock())) {
				if (p == -1) {
					boolean bl3 = false;
					if (o <= 0) {
						bl3 = true;
						blockState3 = surfaceBuilderBaseConfiguration.getUnderMaterial();
					}

					if (bl) {
						blockState4 = surfaceBuilderBaseConfiguration.getUnderMaterial();
					} else if (bl2) {
						blockState4 = surfaceBuilderBaseConfiguration.getUnderwaterMaterial();
					}

					if (q < l && bl3) {
						blockState4 = blockState2;
					}

					p = o;
					if (q >= l - 1) {
						blockColumn.setBlock(q, blockState4);
					} else {
						blockColumn.setBlock(q, blockState3);
					}
				} else if (p > 0) {
					p--;
					blockColumn.setBlock(q, blockState3);
				}
			}
		}
	}

	@Override
	public void initNoise(long l) {
		if (this.seed != l || this.decorationNoise == null) {
			this.decorationNoise = new PerlinNoise(new WorldgenRandom(l), ImmutableList.of(0));
		}

		this.seed = l;
	}
}
