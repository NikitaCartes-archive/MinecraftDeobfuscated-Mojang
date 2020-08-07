package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class NetherForestSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
	protected long seed;
	private PerlinNoise decorationNoise;

	public NetherForestSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
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
		long m,
		SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration
	) {
		int n = l;
		int o = i & 15;
		int p = j & 15;
		double e = this.decorationNoise.getValue((double)i * 0.1, (double)l, (double)j * 0.1);
		boolean bl = e > 0.15 + random.nextDouble() * 0.35;
		double f = this.decorationNoise.getValue((double)i * 0.1, 109.0, (double)j * 0.1);
		boolean bl2 = f > 0.25 + random.nextDouble() * 0.9;
		int q = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int r = -1;
		BlockState blockState3 = surfaceBuilderBaseConfiguration.getUnderMaterial();

		for (int s = 127; s >= 0; s--) {
			mutableBlockPos.set(o, s, p);
			BlockState blockState4 = surfaceBuilderBaseConfiguration.getTopMaterial();
			BlockState blockState5 = chunkAccess.getBlockState(mutableBlockPos);
			if (blockState5.isAir()) {
				r = -1;
			} else if (blockState5.is(blockState.getBlock())) {
				if (r == -1) {
					boolean bl3 = false;
					if (q <= 0) {
						bl3 = true;
						blockState3 = surfaceBuilderBaseConfiguration.getUnderMaterial();
					}

					if (bl) {
						blockState4 = surfaceBuilderBaseConfiguration.getUnderMaterial();
					} else if (bl2) {
						blockState4 = surfaceBuilderBaseConfiguration.getUnderwaterMaterial();
					}

					if (s < n && bl3) {
						blockState4 = blockState2;
					}

					r = q;
					if (s >= n - 1) {
						chunkAccess.setBlockState(mutableBlockPos, blockState4, false);
					} else {
						chunkAccess.setBlockState(mutableBlockPos, blockState3, false);
					}
				} else if (r > 0) {
					r--;
					chunkAccess.setBlockState(mutableBlockPos, blockState3, false);
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
