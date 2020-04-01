package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class SoulSandValleySurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
	private static final BlockState SOUL_SAND = Blocks.SOUL_SAND.defaultBlockState();
	private static final BlockState SOUL_SOIL = Blocks.SOUL_SOIL.defaultBlockState();
	private long seed;
	private PerlinNoise soulSandNoiseFloor;
	private PerlinNoise soulSoilNoiseFloor;
	private PerlinNoise soulSandNoiseCeiling;
	private PerlinNoise soulSoilNoiseCeiling;
	private PerlinNoise gravelNoise;

	public SoulSandValleySurfaceBuilder(
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
		int n = l + 1;
		int o = i & 15;
		int p = j & 15;
		int q = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		int r = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		double e = 0.03125;
		boolean bl = this.gravelNoise.getValue((double)i * 0.03125, 109.0, (double)j * 0.03125) * 75.0 + random.nextDouble() > 0.0;
		double f = this.soulSoilNoiseFloor.getValue((double)i, (double)l, (double)j);
		double g = this.soulSandNoiseFloor.getValue((double)i, (double)l, (double)j);
		double h = this.soulSoilNoiseCeiling.getValue((double)i, (double)l, (double)j);
		double s = this.soulSandNoiseCeiling.getValue((double)i, (double)l, (double)j);
		BlockState blockState3 = h > s ? SOUL_SOIL : SOUL_SAND;
		BlockState blockState4 = f > g ? SOUL_SOIL : SOUL_SAND;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		BlockState blockState5 = chunkAccess.getBlockState(mutableBlockPos.set(o, 128, p));

		for (int t = 127; t >= 0; t--) {
			mutableBlockPos.set(o, t, p);
			BlockState blockState6 = chunkAccess.getBlockState(mutableBlockPos);
			if (blockState5.getBlock() == blockState.getBlock() && (blockState6.isAir() || blockState6 == blockState2)) {
				for (int u = 0; u < q; u++) {
					mutableBlockPos.move(Direction.UP);
					if (chunkAccess.getBlockState(mutableBlockPos).getBlock() != blockState.getBlock()) {
						break;
					}

					chunkAccess.setBlockState(mutableBlockPos, blockState3, false);
				}

				mutableBlockPos.set(o, t, p);
			}

			if ((blockState5.isAir() || blockState5 == blockState2) && blockState6.getBlock() == blockState.getBlock()) {
				for (int u = 0; u < r && chunkAccess.getBlockState(mutableBlockPos).getBlock() == blockState.getBlock(); u++) {
					if (bl && t >= n - 4 && t <= n + 1) {
						chunkAccess.setBlockState(mutableBlockPos, GRAVEL, false);
					} else {
						chunkAccess.setBlockState(mutableBlockPos, blockState4, false);
					}

					mutableBlockPos.move(Direction.DOWN);
				}
			}

			blockState5 = blockState6;
		}
	}

	@Override
	public void initNoise(long l) {
		if (this.seed != l
			|| this.soulSandNoiseFloor == null
			|| this.soulSoilNoiseFloor == null
			|| this.soulSandNoiseCeiling == null
			|| this.soulSoilNoiseCeiling == null
			|| this.gravelNoise == null) {
			this.soulSandNoiseFloor = new PerlinNoise(new WorldgenRandom(l), ImmutableList.of(-4));
			this.soulSoilNoiseFloor = new PerlinNoise(new WorldgenRandom(l + 1L), ImmutableList.of(-4));
			this.soulSandNoiseCeiling = new PerlinNoise(new WorldgenRandom(l + 2L), ImmutableList.of(-4));
			this.soulSoilNoiseCeiling = new PerlinNoise(new WorldgenRandom(l + 3L), ImmutableList.of(-4));
			this.gravelNoise = new PerlinNoise(new WorldgenRandom(l + 4L), ImmutableList.of(0));
		}

		this.seed = l;
	}
}
