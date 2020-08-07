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
		long m,
		SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration
	) {
		int n = l + 1;
		int o = i & 15;
		int p = j & 15;
		int q = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		int r = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
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
		BlockState blockState5 = chunkAccess.getBlockState(mutableBlockPos.set(o, 128, p));

		for (int s = 127; s >= 0; s--) {
			mutableBlockPos.set(o, s, p);
			BlockState blockState6 = chunkAccess.getBlockState(mutableBlockPos);
			if (blockState5.is(blockState.getBlock()) && (blockState6.isAir() || blockState6 == blockState2)) {
				for (int t = 0; t < q; t++) {
					mutableBlockPos.move(Direction.UP);
					if (!chunkAccess.getBlockState(mutableBlockPos).is(blockState.getBlock())) {
						break;
					}

					chunkAccess.setBlockState(mutableBlockPos, blockState3, false);
				}

				mutableBlockPos.set(o, s, p);
			}

			if ((blockState5.isAir() || blockState5 == blockState2) && blockState6.is(blockState.getBlock())) {
				for (int t = 0; t < r && chunkAccess.getBlockState(mutableBlockPos).is(blockState.getBlock()); t++) {
					if (bl && s >= n - 4 && s <= n + 1) {
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
