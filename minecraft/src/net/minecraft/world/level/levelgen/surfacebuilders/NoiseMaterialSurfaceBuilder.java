package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public abstract class NoiseMaterialSurfaceBuilder extends DefaultSurfaceBuilder {
	private long seed;
	protected NormalNoise surfaceNoise;

	public NoiseMaterialSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
		super(codec);
	}

	@Override
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
		BlockState blockState3;
		BlockState blockState4;
		if (this.getSteepMaterial() != null && this.isSteepTerrain(chunkAccess, i, j, this.getSteepMaterial())) {
			blockState3 = this.getSteepMaterial().getState();
			blockState4 = this.getSteepMaterial().getState();
		} else {
			blockState3 = this.getTopMaterial(surfaceBuilderBaseConfiguration, i, j);
			blockState4 = this.getMidMaterial(surfaceBuilderBaseConfiguration, i, j);
		}

		apply(random, chunkAccess, biome, i, j, k, d, blockState, blockState2, blockState3, blockState4, surfaceBuilderBaseConfiguration.getUnderwaterMaterial(), m);
	}

	protected BlockState getMaterial(double d, int i, int j, BlockState blockState, BlockState blockState2, double e, double f) {
		double g = this.surfaceNoise.getValue((double)i * d, 100.0, (double)j * d);
		BlockState blockState3;
		if (g >= e && g <= f) {
			blockState3 = blockState2;
		} else {
			blockState3 = blockState;
		}

		return blockState3;
	}

	@Override
	public void initNoise(long l) {
		if (this.seed != l) {
			WorldgenRandom worldgenRandom = new WorldgenRandom(l);
			this.surfaceNoise = NormalNoise.create(worldgenRandom, -3, 1.0, 1.0, 1.0, 1.0);
		}

		this.seed = l;
	}

	public boolean isSteepTerrain(ChunkAccess chunkAccess, int i, int j, NoiseMaterialSurfaceBuilder.SteepMaterial steepMaterial) {
		int k = 1;
		int l = 3;
		int m = i & 15;
		int n = j & 15;
		if (steepMaterial.hasNorthSlopes() || steepMaterial.hasSouthSlopes()) {
			int o = Math.max(n - 1, 0);
			int p = Math.min(n + 1, 15);
			int q = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, m, o);
			int r = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, m, p);
			int s = q - r;
			if (steepMaterial.hasSouthSlopes() && s > 3) {
				return true;
			}

			if (steepMaterial.hasNorthSlopes() && -s > 3) {
				return true;
			}
		}

		if (!steepMaterial.hasEastSlopes() && !steepMaterial.hasWestSlopes()) {
			return false;
		} else {
			int ox = Math.max(m - 1, 0);
			int px = Math.min(m + 1, 15);
			int qx = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, ox, n);
			int rx = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, px, n);
			int sx = qx - rx;
			return steepMaterial.hasEastSlopes() && sx > 3 ? true : steepMaterial.hasWestSlopes() && -sx > 3;
		}
	}

	@Nullable
	protected abstract NoiseMaterialSurfaceBuilder.SteepMaterial getSteepMaterial();

	protected abstract BlockState getTopMaterial(SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration, int i, int j);

	protected abstract BlockState getMidMaterial(SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration, int i, int j);

	public static class SteepMaterial {
		private final BlockState state;
		private final boolean northSlopes;
		private final boolean southSlopes;
		private final boolean westSlopes;
		private final boolean eastSlopes;

		public SteepMaterial(BlockState blockState, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
			this.state = blockState;
			this.northSlopes = bl;
			this.southSlopes = bl2;
			this.westSlopes = bl3;
			this.eastSlopes = bl4;
		}

		public BlockState getState() {
			return this.state;
		}

		public boolean hasNorthSlopes() {
			return this.northSlopes;
		}

		public boolean hasSouthSlopes() {
			return this.southSlopes;
		}

		public boolean hasWestSlopes() {
			return this.westSlopes;
		}

		public boolean hasEastSlopes() {
			return this.eastSlopes;
		}
	}
}
