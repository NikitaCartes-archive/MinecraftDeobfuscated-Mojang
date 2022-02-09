package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class WorldCarver<C extends CarverConfiguration> {
	public static final WorldCarver<CaveCarverConfiguration> CAVE = register("cave", new CaveWorldCarver(CaveCarverConfiguration.CODEC));
	public static final WorldCarver<CaveCarverConfiguration> NETHER_CAVE = register("nether_cave", new NetherWorldCarver(CaveCarverConfiguration.CODEC));
	public static final WorldCarver<CanyonCarverConfiguration> CANYON = register("canyon", new CanyonWorldCarver(CanyonCarverConfiguration.CODEC));
	protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
	protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
	protected static final FluidState WATER = Fluids.WATER.defaultFluidState();
	protected static final FluidState LAVA = Fluids.LAVA.defaultFluidState();
	protected Set<Block> replaceableBlocks = ImmutableSet.of(
		Blocks.WATER,
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
		Blocks.CALCITE,
		Blocks.SAND,
		Blocks.RED_SAND,
		Blocks.GRAVEL,
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
		Function<BlockPos, Holder<Biome>> function,
		Aquifer aquifer,
		double d,
		double e,
		double f,
		double g,
		double h,
		CarvingMask carvingMask,
		WorldCarver.CarveSkipChecker carveSkipChecker
	) {
		ChunkPos chunkPos = chunkAccess.getPos();
		double i = (double)chunkPos.getMiddleBlockX();
		double j = (double)chunkPos.getMiddleBlockZ();
		double k = 16.0 + g * 2.0;
		if (!(Math.abs(d - i) > k) && !(Math.abs(f - j) > k)) {
			int l = chunkPos.getMinBlockX();
			int m = chunkPos.getMinBlockZ();
			int n = Math.max(Mth.floor(d - g) - l - 1, 0);
			int o = Math.min(Mth.floor(d + g) - l, 15);
			int p = Math.max(Mth.floor(e - h) - 1, carvingContext.getMinGenY() + 1);
			int q = chunkAccess.isUpgrading() ? 0 : 7;
			int r = Math.min(Mth.floor(e + h) + 1, carvingContext.getMinGenY() + carvingContext.getGenDepth() - 1 - q);
			int s = Math.max(Mth.floor(f - g) - m - 1, 0);
			int t = Math.min(Mth.floor(f + g) - m, 15);
			boolean bl = false;
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();

			for (int u = n; u <= o; u++) {
				int v = chunkPos.getBlockX(u);
				double w = ((double)v + 0.5 - d) / g;

				for (int x = s; x <= t; x++) {
					int y = chunkPos.getBlockZ(x);
					double z = ((double)y + 0.5 - f) / g;
					if (!(w * w + z * z >= 1.0)) {
						MutableBoolean mutableBoolean = new MutableBoolean(false);

						for (int aa = r; aa > p; aa--) {
							double ab = ((double)aa - 0.5 - e) / h;
							if (!carveSkipChecker.shouldSkip(carvingContext, w, ab, z, aa) && (!carvingMask.get(u, aa, x) || isDebugEnabled(carverConfiguration))) {
								carvingMask.set(u, aa, x);
								mutableBlockPos.set(v, aa, y);
								bl |= this.carveBlock(
									carvingContext, carverConfiguration, chunkAccess, function, carvingMask, mutableBlockPos, mutableBlockPos2, aquifer, mutableBoolean
								);
							}
						}
					}
				}
			}

			return bl;
		} else {
			return false;
		}
	}

	protected boolean carveBlock(
		CarvingContext carvingContext,
		C carverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Holder<Biome>> function,
		CarvingMask carvingMask,
		BlockPos.MutableBlockPos mutableBlockPos,
		BlockPos.MutableBlockPos mutableBlockPos2,
		Aquifer aquifer,
		MutableBoolean mutableBoolean
	) {
		BlockState blockState = chunkAccess.getBlockState(mutableBlockPos);
		if (blockState.is(Blocks.GRASS_BLOCK) || blockState.is(Blocks.MYCELIUM)) {
			mutableBoolean.setTrue();
		}

		if (!this.canReplaceBlock(blockState) && !isDebugEnabled(carverConfiguration)) {
			return false;
		} else {
			BlockState blockState2 = this.getCarveState(carvingContext, carverConfiguration, mutableBlockPos, aquifer);
			if (blockState2 == null) {
				return false;
			} else {
				chunkAccess.setBlockState(mutableBlockPos, blockState2, false);
				if (aquifer.shouldScheduleFluidUpdate() && !blockState2.getFluidState().isEmpty()) {
					chunkAccess.markPosForPostprocessing(mutableBlockPos);
				}

				if (mutableBoolean.isTrue()) {
					mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.DOWN);
					if (chunkAccess.getBlockState(mutableBlockPos2).is(Blocks.DIRT)) {
						carvingContext.topMaterial(function, chunkAccess, mutableBlockPos2, !blockState2.getFluidState().isEmpty()).ifPresent(blockStatex -> {
							chunkAccess.setBlockState(mutableBlockPos2, blockStatex, false);
							if (!blockStatex.getFluidState().isEmpty()) {
								chunkAccess.markPosForPostprocessing(mutableBlockPos2);
							}
						});
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
		} else {
			BlockState blockState = aquifer.computeSubstance(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0.0, 0.0);
			if (blockState == null) {
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
		Function<BlockPos, Holder<Biome>> function,
		Random random,
		Aquifer aquifer,
		ChunkPos chunkPos,
		CarvingMask carvingMask
	);

	public abstract boolean isStartChunk(C carverConfiguration, Random random);

	protected boolean canReplaceBlock(BlockState blockState) {
		return this.replaceableBlocks.contains(blockState.getBlock());
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
