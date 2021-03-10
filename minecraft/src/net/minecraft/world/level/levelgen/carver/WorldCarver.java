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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class WorldCarver<C extends CarverConfiguration> {
	public static final WorldCarver<CarverConfiguration> CAVE = register("cave", new CaveWorldCarver(CarverConfiguration.CODEC));
	public static final WorldCarver<CarverConfiguration> NETHER_CAVE = register("nether_cave", new NetherWorldCarver(CarverConfiguration.CODEC));
	public static final WorldCarver<CanyonCarverConfiguration> CANYON = register("canyon", new CanyonWorldCarver(CanyonCarverConfiguration.CODEC));
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
		Blocks.PACKED_ICE,
		Blocks.DEEPSLATE
	);
	protected Set<Fluid> liquids = ImmutableSet.of(Fluids.WATER);
	private final Codec<ConfiguredWorldCarver<C>> configuredCodec;

	private static <C extends CarverConfiguration, F extends WorldCarver<C>> F register(String string, F worldCarver) {
		return Registry.register(Registry.CARVER, string, worldCarver);
	}

	public WorldCarver(Codec<C> codec) {
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

	protected boolean carveEllipsoid(
		CarvingContext carvingContext,
		C carverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Biome> function,
		long l,
		int i,
		double d,
		double e,
		double f,
		double g,
		double h,
		BitSet bitSet,
		WorldCarver.CarveSkipChecker carveSkipChecker
	) {
		ChunkPos chunkPos = chunkAccess.getPos();
		int j = chunkPos.x;
		int k = chunkPos.z;
		Random random = new Random(l + (long)j + (long)k);
		double m = (double)chunkPos.getMiddleBlockX();
		double n = (double)chunkPos.getMiddleBlockZ();
		double o = 16.0 + g * 2.0;
		if (!(Math.abs(d - m) > o) && !(Math.abs(f - n) > o)) {
			int p = chunkPos.getMinBlockX();
			int q = chunkPos.getMinBlockZ();
			int r = Math.max(Mth.floor(d - g) - p - 1, 0);
			int s = Math.min(Mth.floor(d + g) - p, 15);
			int t = Math.max(Mth.floor(e - h) - 1, carvingContext.getMinGenY() + 1);
			int u = Math.min(Mth.floor(e + h) + 1, carvingContext.getMinGenY() + carvingContext.getGenDepth() - 8);
			int v = Math.max(Mth.floor(f - g) - q - 1, 0);
			int w = Math.min(Mth.floor(f + g) - q, 15);
			if (this.hasDisallowedLiquid(chunkAccess, r, s, t, u, v, w)) {
				return false;
			} else {
				boolean bl = false;
				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
				BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();

				for (int x = r; x <= s; x++) {
					int y = chunkPos.getBlockX(x);
					double z = ((double)y + 0.5 - d) / g;

					for (int aa = v; aa <= w; aa++) {
						int ab = chunkPos.getBlockZ(aa);
						double ac = ((double)ab + 0.5 - f) / g;
						if (!(z * z + ac * ac >= 1.0)) {
							MutableBoolean mutableBoolean = new MutableBoolean(false);

							for (int ad = u; ad > t; ad--) {
								double ae = ((double)ad - 0.5 - e) / h;
								if (!carveSkipChecker.shouldSkip(carvingContext, z, ae, ac, ad)) {
									int af = ad - carvingContext.getMinGenY();
									int ag = x | aa << 4 | af << 8;
									if (!bitSet.get(ag) || isDebugEnabled(carverConfiguration)) {
										bitSet.set(ag);
										mutableBlockPos.set(y, ad, ab);
										bl |= this.carveBlock(
											carvingContext, carverConfiguration, chunkAccess, function, bitSet, random, mutableBlockPos, mutableBlockPos2, i, mutableBoolean
										);
									}
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
		CarvingContext carvingContext,
		C carverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Biome> function,
		BitSet bitSet,
		Random random,
		BlockPos.MutableBlockPos mutableBlockPos,
		BlockPos.MutableBlockPos mutableBlockPos2,
		int i,
		MutableBoolean mutableBoolean
	) {
		BlockState blockState = chunkAccess.getBlockState(mutableBlockPos);
		BlockState blockState2 = chunkAccess.getBlockState(mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.UP));
		if (blockState.is(Blocks.GRASS_BLOCK) || blockState.is(Blocks.MYCELIUM)) {
			mutableBoolean.setTrue();
		}

		if (!this.canReplaceBlock(blockState, blockState2) && !isDebugEnabled(carverConfiguration)) {
			return false;
		} else {
			if (mutableBlockPos.getY() < carvingContext.getMinGenY() + 9 && !isDebugEnabled(carverConfiguration)) {
				chunkAccess.setBlockState(mutableBlockPos, LAVA.createLegacyBlock(), false);
			} else {
				chunkAccess.setBlockState(mutableBlockPos, getCaveAirState(carverConfiguration), false);
				if (mutableBoolean.isTrue()) {
					mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.DOWN);
					if (chunkAccess.getBlockState(mutableBlockPos2).is(Blocks.DIRT)) {
						chunkAccess.setBlockState(
							mutableBlockPos2, ((Biome)function.apply(mutableBlockPos)).getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial(), false
						);
					}
				}
			}

			return true;
		}
	}

	private static BlockState getCaveAirState(CarverConfiguration carverConfiguration) {
		return isDebugEnabled(carverConfiguration) ? carverConfiguration.getDebugSettings().getAirState() : CAVE_AIR;
	}

	public abstract boolean carve(
		CarvingContext carvingContext,
		C carverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Biome> function,
		Random random,
		int i,
		ChunkPos chunkPos,
		BitSet bitSet
	);

	public abstract boolean isStartChunk(C carverConfiguration, Random random);

	protected boolean canReplaceBlock(BlockState blockState) {
		return this.replaceableBlocks.contains(blockState.getBlock());
	}

	protected boolean canReplaceBlock(BlockState blockState, BlockState blockState2) {
		return this.canReplaceBlock(blockState) || (blockState.is(Blocks.SAND) || blockState.is(Blocks.GRAVEL)) && !blockState2.getFluidState().is(FluidTags.WATER);
	}

	protected boolean hasDisallowedLiquid(ChunkAccess chunkAccess, int i, int j, int k, int l, int m, int n) {
		ChunkPos chunkPos = chunkAccess.getPos();
		int o = chunkPos.getMinBlockX();
		int p = chunkPos.getMinBlockZ();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int q = i; q <= j; q++) {
			for (int r = m; r <= n; r++) {
				for (int s = k - 1; s <= l + 1; s++) {
					mutableBlockPos.set(o + q, s, p + r);
					if (this.liquids.contains(chunkAccess.getFluidState(mutableBlockPos).getType())) {
						return true;
					}

					if (s != l + 1 && !isEdge(q, r, i, j, m, n)) {
						s = l;
					}
				}
			}
		}

		return false;
	}

	private static boolean isEdge(int i, int j, int k, int l, int m, int n) {
		return i == k || i == l || j == m || j == n;
	}

	protected static boolean canReach(ChunkPos chunkPos, double d, double e, int i, int j, float f) {
		double g = (double)chunkPos.getMiddleBlockX();
		double h = (double)chunkPos.getMiddleBlockZ();
		double k = d - g;
		double l = e - h;
		double m = (double)(j - i);
		double n = (double)(f + 2.0F + 16.0F);
		return k * k + l * l - m * m <= n * n;
	}

	private static boolean isDebugEnabled(CarverConfiguration carverConfiguration) {
		return carverConfiguration.getDebugSettings().isDebugMode();
	}

	public interface CarveSkipChecker {
		boolean shouldSkip(CarvingContext carvingContext, double d, double e, double f, int i);
	}
}
