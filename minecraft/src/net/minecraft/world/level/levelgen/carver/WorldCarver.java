package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.BaseStoneSource;
import net.minecraft.world.level.levelgen.SingleBaseStoneSource;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class WorldCarver<C extends CarverConfiguration> {
	public static final WorldCarver<CaveCarverConfiguration> CAVE = register("cave", new CaveWorldCarver(CaveCarverConfiguration.CODEC));
	public static final WorldCarver<CaveCarverConfiguration> NETHER_CAVE = register("nether_cave", new NetherWorldCarver(CaveCarverConfiguration.CODEC));
	public static final WorldCarver<CanyonCarverConfiguration> CANYON = register("canyon", new CanyonWorldCarver(CanyonCarverConfiguration.CODEC));
	public static final WorldCarver<CanyonCarverConfiguration> UNDERWATER_CANYON = register(
		"underwater_canyon", new UnderwaterCanyonWorldCarver(CanyonCarverConfiguration.CODEC)
	);
	public static final WorldCarver<CaveCarverConfiguration> UNDERWATER_CAVE = register(
		"underwater_cave", new UnderwaterCaveWorldCarver(CaveCarverConfiguration.CODEC)
	);
	protected static final BaseStoneSource STONE_SOURCE = new SingleBaseStoneSource(Blocks.STONE.defaultBlockState());
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
		Blocks.DEEPSLATE,
		Blocks.TUFF,
		Blocks.GRANITE,
		Blocks.IRON_ORE,
		Blocks.DEEPSLATE_IRON_ORE,
		Blocks.RAW_IRON_BLOCK,
		Blocks.COPPER_ORE,
		Blocks.DEEPSLATE_COPPER_ORE,
		Blocks.RAW_COPPER_BLOCK
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
		Aquifer aquifer,
		double d,
		double e,
		double f,
		double g,
		double h,
		BitSet bitSet,
		WorldCarver.CarveSkipChecker carveSkipChecker
	) {
		ChunkPos chunkPos = chunkAccess.getPos();
		int i = chunkPos.x;
		int j = chunkPos.z;
		Random random = new Random(l + (long)i + (long)j);
		double k = (double)chunkPos.getMiddleBlockX();
		double m = (double)chunkPos.getMiddleBlockZ();
		double n = 16.0 + g * 2.0;
		if (!(Math.abs(d - k) > n) && !(Math.abs(f - m) > n)) {
			int o = chunkPos.getMinBlockX();
			int p = chunkPos.getMinBlockZ();
			int q = Math.max(Mth.floor(d - g) - o - 1, 0);
			int r = Math.min(Mth.floor(d + g) - o, 15);
			int s = Math.max(Mth.floor(e - h) - 1, carvingContext.getMinGenY() + 1);
			int t = Math.min(Mth.floor(e + h) + 1, carvingContext.getMinGenY() + carvingContext.getGenDepth() - 8);
			int u = Math.max(Mth.floor(f - g) - p - 1, 0);
			int v = Math.min(Mth.floor(f + g) - p, 15);
			if (!carverConfiguration.aquifersEnabled && this.hasDisallowedLiquid(chunkAccess, q, r, s, t, u, v)) {
				return false;
			} else {
				boolean bl = false;
				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
				BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();

				for (int w = q; w <= r; w++) {
					int x = chunkPos.getBlockX(w);
					double y = ((double)x + 0.5 - d) / g;

					for (int z = u; z <= v; z++) {
						int aa = chunkPos.getBlockZ(z);
						double ab = ((double)aa + 0.5 - f) / g;
						if (!(y * y + ab * ab >= 1.0)) {
							MutableBoolean mutableBoolean = new MutableBoolean(false);

							for (int ac = t; ac > s; ac--) {
								double ad = ((double)ac - 0.5 - e) / h;
								if (!carveSkipChecker.shouldSkip(carvingContext, y, ad, ab, ac)) {
									int ae = ac - carvingContext.getMinGenY();
									int af = w | z << 4 | ae << 8;
									if (!bitSet.get(af) || isDebugEnabled(carverConfiguration)) {
										bitSet.set(af);
										mutableBlockPos.set(x, ac, aa);
										bl |= this.carveBlock(
											carvingContext, carverConfiguration, chunkAccess, function, bitSet, random, mutableBlockPos, mutableBlockPos2, aquifer, mutableBoolean
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
		Aquifer aquifer,
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
			BlockState blockState3 = this.getCarveState(carvingContext, carverConfiguration, mutableBlockPos, aquifer);
			if (blockState3 == null) {
				return false;
			} else {
				chunkAccess.setBlockState(mutableBlockPos, blockState3, false);
				if (aquifer.shouldScheduleFluidUpdate() && !blockState3.getFluidState().isEmpty()) {
					chunkAccess.getLiquidTicks().scheduleTick(mutableBlockPos, blockState3.getFluidState().getType(), 0);
				}

				if (mutableBoolean.isTrue()) {
					mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.DOWN);
					if (chunkAccess.getBlockState(mutableBlockPos2).is(Blocks.DIRT)) {
						chunkAccess.setBlockState(
							mutableBlockPos2, ((Biome)function.apply(mutableBlockPos)).getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial(), false
						);
					}
				}

				return true;
			}
		}
	}

	@Nullable
	private BlockState getCarveState(CarvingContext carvingContext, C carverConfiguration, BlockPos blockPos, Aquifer aquifer) {
		if (blockPos.getY() <= carverConfiguration.lavaLevel.resolveY(carvingContext)) {
			return LAVA.createLegacyBlock();
		} else if (!carverConfiguration.aquifersEnabled) {
			return isDebugEnabled(carverConfiguration) ? getDebugState(carverConfiguration, AIR) : AIR;
		} else {
			BlockState blockState = aquifer.computeState(STONE_SOURCE, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0.0);
			if (blockState == Blocks.STONE.defaultBlockState()) {
				return isDebugEnabled(carverConfiguration) ? carverConfiguration.debugSettings.getBarrierState() : null;
			} else {
				return isDebugEnabled(carverConfiguration) ? getDebugState(carverConfiguration, blockState) : blockState;
			}
		}
	}

	private static BlockState getDebugState(CarverConfiguration carverConfiguration, BlockState blockState) {
		if (blockState.is(Blocks.AIR)) {
			return carverConfiguration.debugSettings.getAirState();
		} else if (blockState.is(Blocks.WATER)) {
			BlockState blockState2 = carverConfiguration.debugSettings.getWaterState();
			return blockState2.hasProperty(BlockStateProperties.WATERLOGGED)
				? blockState2.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true))
				: blockState2;
		} else {
			return blockState.is(Blocks.LAVA) ? carverConfiguration.debugSettings.getLavaState() : blockState;
		}
	}

	public abstract boolean carve(
		CarvingContext carvingContext,
		C carverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Biome> function,
		Random random,
		Aquifer aquifer,
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
		return carverConfiguration.debugSettings.isDebugMode();
	}

	public interface CarveSkipChecker {
		boolean shouldSkip(CarvingContext carvingContext, double d, double e, double f, int i);
	}
}
