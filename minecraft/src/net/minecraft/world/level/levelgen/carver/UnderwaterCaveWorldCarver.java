package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class UnderwaterCaveWorldCarver extends CaveWorldCarver {
	public UnderwaterCaveWorldCarver(Codec<ProbabilityFeatureConfiguration> codec) {
		super(codec, 256);
		this.replaceableBlocks = ImmutableSet.of(
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
			Blocks.SAND,
			Blocks.GRAVEL,
			Blocks.WATER,
			Blocks.LAVA,
			Blocks.OBSIDIAN,
			Blocks.AIR,
			Blocks.CAVE_AIR,
			Blocks.PACKED_ICE
		);
	}

	@Override
	protected boolean hasWater(ChunkAccess chunkAccess, int i, int j, int k, int l, int m, int n, int o, int p) {
		return false;
	}

	@Override
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
		return carveBlock(this, chunkAccess, bitSet, random, mutableBlockPos, i, j, k, l, m, n, o, p);
	}

	protected static boolean carveBlock(
		WorldCarver<?> worldCarver,
		ChunkAccess chunkAccess,
		BitSet bitSet,
		Random random,
		BlockPos.MutableBlockPos mutableBlockPos,
		int i,
		int j,
		int k,
		int l,
		int m,
		int n,
		int o,
		int p
	) {
		if (o >= i) {
			return false;
		} else {
			int q = n | p << 4 | o << 8;
			if (bitSet.get(q)) {
				return false;
			} else {
				bitSet.set(q);
				mutableBlockPos.set(l, o, m);
				BlockState blockState = chunkAccess.getBlockState(mutableBlockPos);
				if (!worldCarver.canReplaceBlock(blockState)) {
					return false;
				} else if (o == 10) {
					float f = random.nextFloat();
					if ((double)f < 0.25) {
						chunkAccess.setBlockState(mutableBlockPos, Blocks.MAGMA_BLOCK.defaultBlockState(), false);
						chunkAccess.getBlockTicks().scheduleTick(mutableBlockPos, Blocks.MAGMA_BLOCK, 0);
					} else {
						chunkAccess.setBlockState(mutableBlockPos, Blocks.OBSIDIAN.defaultBlockState(), false);
					}

					return true;
				} else if (o < 10) {
					chunkAccess.setBlockState(mutableBlockPos, Blocks.LAVA.defaultBlockState(), false);
					return false;
				} else {
					boolean bl = false;

					for (Direction direction : Direction.Plane.HORIZONTAL) {
						int r = l + direction.getStepX();
						int s = m + direction.getStepZ();
						if (r >> 4 != j || s >> 4 != k || chunkAccess.getBlockState(mutableBlockPos.set(r, o, s)).isAir()) {
							chunkAccess.setBlockState(mutableBlockPos, WATER.createLegacyBlock(), false);
							chunkAccess.getLiquidTicks().scheduleTick(mutableBlockPos, WATER.getType(), 0);
							bl = true;
							break;
						}
					}

					mutableBlockPos.set(l, o, m);
					if (!bl) {
						chunkAccess.setBlockState(mutableBlockPos, WATER.createLegacyBlock(), false);
						return true;
					} else {
						return true;
					}
				}
			}
		}
	}
}
