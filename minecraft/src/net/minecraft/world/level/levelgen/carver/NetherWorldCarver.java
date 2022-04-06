package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NetherWorldCarver extends CaveWorldCarver {
	public NetherWorldCarver(Codec<CaveCarverConfiguration> codec) {
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
			Blocks.NETHERRACK,
			Blocks.SOUL_SAND,
			Blocks.SOUL_SOIL,
			Blocks.CRIMSON_NYLIUM,
			Blocks.WARPED_NYLIUM,
			Blocks.NETHER_WART_BLOCK,
			Blocks.WARPED_WART_BLOCK,
			Blocks.BASALT,
			Blocks.BLACKSTONE
		);
		this.liquids = ImmutableSet.of(Fluids.LAVA, Fluids.WATER);
	}

	@Override
	protected int getCaveBound() {
		return 10;
	}

	@Override
	protected float getThickness(RandomSource randomSource) {
		return (randomSource.nextFloat() * 2.0F + randomSource.nextFloat()) * 2.0F;
	}

	@Override
	protected double getYScale() {
		return 5.0;
	}

	protected boolean carveBlock(
		CarvingContext carvingContext,
		CaveCarverConfiguration caveCarverConfiguration,
		ChunkAccess chunkAccess,
		Function<BlockPos, Holder<Biome>> function,
		CarvingMask carvingMask,
		BlockPos.MutableBlockPos mutableBlockPos,
		BlockPos.MutableBlockPos mutableBlockPos2,
		Aquifer aquifer,
		MutableBoolean mutableBoolean
	) {
		if (this.canReplaceBlock(chunkAccess.getBlockState(mutableBlockPos))) {
			BlockState blockState;
			if (mutableBlockPos.getY() <= carvingContext.getMinGenY() + 31) {
				blockState = LAVA.createLegacyBlock();
			} else {
				blockState = CAVE_AIR;
			}

			chunkAccess.setBlockState(mutableBlockPos, blockState, false);
			return true;
		} else {
			return false;
		}
	}
}
