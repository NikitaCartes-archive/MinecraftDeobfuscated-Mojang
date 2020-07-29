package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class WorldCarver<C extends CarverConfiguration> {
	public static final WorldCarver<ProbabilityFeatureConfiguration> CAVE = register("cave", new CaveWorldCarver(ProbabilityFeatureConfiguration.CODEC, 256));
	public static final WorldCarver<ProbabilityFeatureConfiguration> NETHER_CAVE = register(
		"nether_cave", new NetherWorldCarver(ProbabilityFeatureConfiguration.CODEC)
	);
	public static final WorldCarver<ProbabilityFeatureConfiguration> CANYON = register("canyon", new CanyonWorldCarver(ProbabilityFeatureConfiguration.CODEC));
	public static final WorldCarver<ProbabilityFeatureConfiguration> UNDERWATER_CANYON = register(
		"underwater_canyon", new UnderwaterCanyonWorldCarver(ProbabilityFeatureConfiguration.CODEC)
	);
	public static final WorldCarver<ProbabilityFeatureConfiguration> UNDERWATER_CAVE = register(
		"underwater_cave", new UnderwaterCaveWorldCarver(ProbabilityFeatureConfiguration.CODEC)
	);
	protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
	protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
	protected static final FluidState WATER = Fluids.WATER.defaultFluidState();
	protected static final FluidState LAVA = Fluids.LAVA.defaultFluidState();
	protected Set<Block> replaceableBlocks = ImmutableSet.of(
		Blocks.STONE,
		Blocks.GRANITE,
		Blocks.DIORITE,
		Blocks.ANDESITE,
		Blocks.DIRT,
		Blocks.COARSE_DIRT,
		Blocks.PODZOL,
		Blocks.GRASS_BLOCK,
		Blocks.TERRACOTTA,
		Blocks.WHITE_TERRACOTTA,
		Blocks.ORANGE_TERRACOTTA,
		Blocks.MAGENTA_TERRACOTTA,
		Blocks.LIGHT_BLUE_TERRACOTTA,
		Blocks.YELLOW_TERRACOTTA,
		Blocks.LIME_TERRACOTTA,
		Blocks.PINK_TERRACOTTA,
		Blocks.GRAY_TERRACOTTA,
		Blocks.LIGHT_GRAY_TERRACOTTA,
		Blocks.CYAN_TERRACOTTA,
		Blocks.PURPLE_TERRACOTTA,
		Blocks.BLUE_TERRACOTTA,
		Blocks.BROWN_TERRACOTTA,
		Blocks.GREEN_TERRACOTTA,
		Blocks.RED_TERRACOTTA,
		Blocks.BLACK_TERRACOTTA,
		Blocks.SANDSTONE,
		Blocks.RED_SANDSTONE,
		Blocks.MYCELIUM,
		Blocks.SNOW,
		Blocks.PACKED_ICE
	);
	protected Set<Fluid> liquids = ImmutableSet.of(Fluids.WATER);
	private final Codec<ConfiguredWorldCarver<C>> configuredCodec;
	protected final int genHeight;

	private static <C extends CarverConfiguration, F extends WorldCarver<C>> F register(String string, F worldCarver) {
		return Registry.register(Registry.CARVER, string, worldCarver);
	}

	public WorldCarver(Codec<C> codec, int i) {
		this.genHeight = i;
		this.configuredCodec = codec.fieldOf("config").<ConfiguredWorldCarver<C>>xmap(this::configured, ConfiguredWorldCarver::config).codec();
	}

	public ConfiguredWorldCarver<C> configured(C carverConfiguration) {
		return new ConfiguredWorldCarver<>(this, carverConfiguration);
	}

	public Codec<ConfiguredWorldCarver<C>> configuredCodec() {
		return this.configuredCodec;
	}

	public int getRange() {
		return 4;
	}

	protected boolean carveSphere(
		ChunkAccess chunkAccess, Function<BlockPos, Biome> function, long l, int i, int j, int k, double d, double e, double f, double g, double h, BitSet bitSet
	) {
		Random random = new Random(l + (long)j + (long)k);
		double m = (double)(j * 16 + 8);
		double n = (double)(k * 16 + 8);
		if (!(d < m - 16.0 - g * 2.0) && !(f < n - 16.0 - g * 2.0) && !(d > m + 16.0 + g * 2.0) && !(f > n + 16.0 + g * 2.0)) {
			int o = Math.max(Mth.floor(d - g) - j * 16 - 1, 0);
			int p = Math.min(Mth.floor(d + g) - j * 16 + 1, 16);
			int q = Math.max(Mth.floor(e - h) - 1, 1);
			int r = Math.min(Mth.floor(e + h) + 1, this.genHeight - 8);
			int s = Math.max(Mth.floor(f - g) - k * 16 - 1, 0);
			int t = Math.min(Mth.floor(f + g) - k * 16 + 1, 16);
			if (this.hasWater(chunkAccess, j, k, o, p, q, r, s, t)) {
				return false;
			} else {
				boolean bl = false;
				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
				BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
				BlockPos.MutableBlockPos mutableBlockPos3 = new BlockPos.MutableBlockPos();

				for (int u = o; u < p; u++) {
					int v = u + j * 16;
					double w = ((double)v + 0.5 - d) / g;

					for (int x = s; x < t; x++) {
						int y = x + k * 16;
						double z = ((double)y + 0.5 - f) / g;
						if (!(w * w + z * z >= 1.0)) {
							MutableBoolean mutableBoolean = new MutableBoolean(false);

							for (int aa = r; aa > q; aa--) {
								double ab = ((double)aa - 0.5 - e) / h;
								if (!this.skip(w, ab, z, aa)) {
									bl |= this.carveBlock(
										chunkAccess, function, bitSet, random, mutableBlockPos, mutableBlockPos2, mutableBlockPos3, i, j, k, v, y, u, aa, x, mutableBoolean
									);
								}
							}
						}
					}
				}

				return bl;
			}
		} else {
			return false;
		}
	}

	protected boolean carveBlock(
		ChunkAccess chunkAccess,
		Function<BlockPos, Biome> function,
		BitSet bitSet,
		Random random,
		BlockPos.MutableBlockPos mutableBlockPos,
		BlockPos.MutableBlockPos mutableBlockPos2,
		BlockPos.MutableBlockPos mutableBlockPos3,
		int i,
		int j,
		int k,
		int l,
		int m,
		int n,
		int o,
		int p,
		MutableBoolean mutableBoolean
	) {
		int q = n | p << 4 | o << 8;
		if (bitSet.get(q)) {
			return false;
		} else {
			bitSet.set(q);
			mutableBlockPos.set(l, o, m);
			BlockState blockState = chunkAccess.getBlockState(mutableBlockPos);
			BlockState blockState2 = chunkAccess.getBlockState(mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.UP));
			if (blockState.is(Blocks.GRASS_BLOCK) || blockState.is(Blocks.MYCELIUM)) {
				mutableBoolean.setTrue();
			}

			if (!this.canReplaceBlock(blockState, blockState2)) {
				return false;
			} else {
				if (o < 11) {
					chunkAccess.setBlockState(mutableBlockPos, LAVA.createLegacyBlock(), false);
				} else {
					chunkAccess.setBlockState(mutableBlockPos, CAVE_AIR, false);
					if (mutableBoolean.isTrue()) {
						mutableBlockPos3.setWithOffset(mutableBlockPos, Direction.DOWN);
						if (chunkAccess.getBlockState(mutableBlockPos3).is(Blocks.DIRT)) {
							chunkAccess.setBlockState(
								mutableBlockPos3, ((Biome)function.apply(mutableBlockPos)).getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial(), false
							);
						}
					}
				}

				return true;
			}
		}
	}

	public abstract boolean carve(
		ChunkAccess chunkAccess, Function<BlockPos, Biome> function, Random random, int i, int j, int k, int l, int m, BitSet bitSet, C carverConfiguration
	);

	public abstract boolean isStartChunk(Random random, int i, int j, C carverConfiguration);

	protected boolean canReplaceBlock(BlockState blockState) {
		return this.replaceableBlocks.contains(blockState.getBlock());
	}

	protected boolean canReplaceBlock(BlockState blockState, BlockState blockState2) {
		return this.canReplaceBlock(blockState) || (blockState.is(Blocks.SAND) || blockState.is(Blocks.GRAVEL)) && !blockState2.getFluidState().is(FluidTags.WATER);
	}

	protected boolean hasWater(ChunkAccess chunkAccess, int i, int j, int k, int l, int m, int n, int o, int p) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int q = k; q < l; q++) {
			for (int r = o; r < p; r++) {
				for (int s = m - 1; s <= n + 1; s++) {
					if (this.liquids.contains(chunkAccess.getFluidState(mutableBlockPos.set(q + i * 16, s, r + j * 16)).getType())) {
						return true;
					}

					if (s != n + 1 && !this.isEdge(k, l, o, p, q, r)) {
						s = n;
					}
				}
			}
		}

		return false;
	}

	private boolean isEdge(int i, int j, int k, int l, int m, int n) {
		return m == i || m == j - 1 || n == k || n == l - 1;
	}

	protected boolean canReach(int i, int j, double d, double e, int k, int l, float f) {
		double g = (double)(i * 16 + 8);
		double h = (double)(j * 16 + 8);
		double m = d - g;
		double n = e - h;
		double o = (double)(l - k);
		double p = (double)(f + 2.0F + 16.0F);
		return m * m + n * n - o * o <= p * p;
	}

	protected abstract boolean skip(double d, double e, double f, int i);
}
