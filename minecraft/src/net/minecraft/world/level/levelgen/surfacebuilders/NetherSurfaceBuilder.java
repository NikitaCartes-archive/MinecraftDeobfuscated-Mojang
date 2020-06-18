package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class NetherSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
	private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
	private static final BlockState SOUL_SAND = Blocks.SOUL_SAND.defaultBlockState();
	protected long seed;
	protected PerlinNoise decorationNoise;

	public NetherSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
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
		double e = 0.03125;
		boolean bl = this.decorationNoise.getValue((double)i * 0.03125, (double)j * 0.03125, 0.0) * 75.0 + random.nextDouble() > 0.0;
		boolean bl2 = this.decorationNoise.getValue((double)i * 0.03125, 109.0, (double)j * 0.03125) * 75.0 + random.nextDouble() > 0.0;
		int q = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int r = -1;
		BlockState blockState3 = surfaceBuilderBaseConfiguration.getTopMaterial();
		BlockState blockState4 = surfaceBuilderBaseConfiguration.getUnderMaterial();

		for (int s = 127; s >= 0; s--) {
			mutableBlockPos.set(o, s, p);
			BlockState blockState5 = chunkAccess.getBlockState(mutableBlockPos);
			if (blockState5.isAir()) {
				r = -1;
			} else if (blockState5.is(blockState.getBlock())) {
				if (r == -1) {
					boolean bl3 = false;
					if (q <= 0) {
						bl3 = true;
						blockState4 = surfaceBuilderBaseConfiguration.getUnderMaterial();
					} else if (s >= n - 4 && s <= n + 1) {
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

					if (s < n && bl3) {
						blockState3 = blockState2;
					}

					r = q;
					if (s >= n - 1) {
						chunkAccess.setBlockState(mutableBlockPos, blockState3, false);
					} else {
						chunkAccess.setBlockState(mutableBlockPos, blockState4, false);
					}
				} else if (r > 0) {
					r--;
					chunkAccess.setBlockState(mutableBlockPos, blockState4, false);
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
