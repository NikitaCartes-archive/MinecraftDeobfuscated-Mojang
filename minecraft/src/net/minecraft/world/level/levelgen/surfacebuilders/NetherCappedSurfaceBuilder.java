package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Codec;
import java.util.Comparator;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public abstract class NetherCappedSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	private long seed;
	private ImmutableMap<BlockState, PerlinNoise> floorNoises = ImmutableMap.of();
	private ImmutableMap<BlockState, PerlinNoise> ceilingNoises = ImmutableMap.of();
	private PerlinNoise patchNoise;

	public NetherCappedSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
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
		int o = l + 1;
		int p = i & 15;
		int q = j & 15;
		int r = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		int s = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		double e = 0.03125;
		boolean bl = this.patchNoise.getValue((double)i * 0.03125, 109.0, (double)j * 0.03125) * 75.0 + random.nextDouble() > 0.0;
		BlockState blockState3 = (BlockState)((Entry)this.ceilingNoises
				.entrySet()
				.stream()
				.max(Comparator.comparing(entry -> ((PerlinNoise)entry.getValue()).getValue((double)i, (double)l, (double)j)))
				.get())
			.getKey();
		BlockState blockState4 = (BlockState)((Entry)this.floorNoises
				.entrySet()
				.stream()
				.max(Comparator.comparing(entry -> ((PerlinNoise)entry.getValue()).getValue((double)i, (double)l, (double)j)))
				.get())
			.getKey();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		BlockState blockState5 = chunkAccess.getBlockState(mutableBlockPos.set(p, 128, q));

		for (int t = 127; t >= m; t--) {
			mutableBlockPos.set(p, t, q);
			BlockState blockState6 = chunkAccess.getBlockState(mutableBlockPos);
			if (blockState5.is(blockState.getBlock()) && (blockState6.isAir() || blockState6 == blockState2)) {
				for (int u = 0; u < r; u++) {
					mutableBlockPos.move(Direction.UP);
					if (!chunkAccess.getBlockState(mutableBlockPos).is(blockState.getBlock())) {
						break;
					}

					chunkAccess.setBlockState(mutableBlockPos, blockState3, false);
				}

				mutableBlockPos.set(p, t, q);
			}

			if ((blockState5.isAir() || blockState5 == blockState2) && blockState6.is(blockState.getBlock())) {
				for (int u = 0; u < s && chunkAccess.getBlockState(mutableBlockPos).is(blockState.getBlock()); u++) {
					if (bl && t >= o - 4 && t <= o + 1) {
						chunkAccess.setBlockState(mutableBlockPos, this.getPatchBlockState(), false);
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
		if (this.seed != l || this.patchNoise == null || this.floorNoises.isEmpty() || this.ceilingNoises.isEmpty()) {
			this.floorNoises = initPerlinNoises(this.getFloorBlockStates(), l);
			this.ceilingNoises = initPerlinNoises(this.getCeilingBlockStates(), l + (long)this.floorNoises.size());
			this.patchNoise = new PerlinNoise(new WorldgenRandom(l + (long)this.floorNoises.size() + (long)this.ceilingNoises.size()), ImmutableList.of(0));
		}

		this.seed = l;
	}

	private static ImmutableMap<BlockState, PerlinNoise> initPerlinNoises(ImmutableList<BlockState> immutableList, long l) {
		Builder<BlockState, PerlinNoise> builder = new Builder<>();

		for (BlockState blockState : immutableList) {
			builder.put(blockState, new PerlinNoise(new WorldgenRandom(l), ImmutableList.of(-4)));
			l++;
		}

		return builder.build();
	}

	protected abstract ImmutableList<BlockState> getFloorBlockStates();

	protected abstract ImmutableList<BlockState> getCeilingBlockStates();

	protected abstract BlockState getPatchBlockState();
}
