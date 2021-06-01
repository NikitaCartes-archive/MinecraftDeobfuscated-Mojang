package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class UnderwaterCaveWorldCarver extends CaveWorldCarver {
	public UnderwaterCaveWorldCarver(Codec<CaveCarverConfiguration> codec) {
		super(codec);
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
			Blocks.PACKED_ICE
		);
	}

	@Override
	protected boolean hasDisallowedLiquid(ChunkAccess chunkAccess, int i, int j, int k, int l, int m, int n) {
		return false;
	}

	protected boolean carveBlock(
		CarvingContext carvingContext,
		CaveCarverConfiguration caveCarverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Biome> function,
		BitSet bitSet,
		Random random,
		BlockPos.MutableBlockPos mutableBlockPos,
		BlockPos.MutableBlockPos mutableBlockPos2,
		Aquifer aquifer,
		MutableBoolean mutableBoolean
	) {
		return carveBlock(this, chunkAccess, random, mutableBlockPos, mutableBlockPos2, aquifer);
	}

	protected static boolean carveBlock(
		WorldCarver<?> worldCarver,
		ChunkAccess chunkAccess,
		Random random,
		BlockPos.MutableBlockPos mutableBlockPos,
		BlockPos.MutableBlockPos mutableBlockPos2,
		Aquifer aquifer
	) {
		if (aquifer.computeState(WorldCarver.STONE_SOURCE, mutableBlockPos.getX(), mutableBlockPos.getY(), mutableBlockPos.getZ(), Double.NEGATIVE_INFINITY).isAir()) {
			return false;
		} else {
			BlockState blockState = chunkAccess.getBlockState(mutableBlockPos);
			if (!worldCarver.canReplaceBlock(blockState)) {
				return false;
			} else if (mutableBlockPos.getY() == 10) {
				float f = random.nextFloat();
				if ((double)f < 0.25) {
					chunkAccess.setBlockState(mutableBlockPos, Blocks.MAGMA_BLOCK.defaultBlockState(), false);
					chunkAccess.getBlockTicks().scheduleTick(mutableBlockPos, Blocks.MAGMA_BLOCK, 0);
				} else {
					chunkAccess.setBlockState(mutableBlockPos, Blocks.OBSIDIAN.defaultBlockState(), false);
				}

				return true;
			} else if (mutableBlockPos.getY() < 10) {
				chunkAccess.setBlockState(mutableBlockPos, Blocks.LAVA.defaultBlockState(), false);
				return false;
			} else {
				chunkAccess.setBlockState(mutableBlockPos, WATER.createLegacyBlock(), false);
				int i = chunkAccess.getPos().x;
				int j = chunkAccess.getPos().z;

				for (Direction direction : LiquidBlock.POSSIBLE_FLOW_DIRECTIONS) {
					mutableBlockPos2.setWithOffset(mutableBlockPos, direction);
					if (SectionPos.blockToSectionCoord(mutableBlockPos2.getX()) != i
						|| SectionPos.blockToSectionCoord(mutableBlockPos2.getZ()) != j
						|| chunkAccess.getBlockState(mutableBlockPos2).isAir()) {
						chunkAccess.getLiquidTicks().scheduleTick(mutableBlockPos, WATER.getType(), 0);
						break;
					}
				}

				return true;
			}
		}
	}
}
