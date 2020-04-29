package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.Dynamic;
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

public class NetherWorldCarver extends CaveWorldCarver {
	public NetherWorldCarver(Function<Dynamic<?>, ? extends ProbabilityFeatureConfiguration> function) {
		super(function, 128);
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
	protected float getThickness(Random random) {
		return (random.nextFloat() * 2.0F + random.nextFloat()) * 2.0F;
	}

	@Override
	protected double getYScale() {
		return 5.0;
	}

	@Override
	protected int getCaveY(Random random) {
		return random.nextInt(this.genHeight);
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
		AtomicBoolean atomicBoolean
	) {
		int q = n | p << 4 | o << 8;
		if (bitSet.get(q)) {
			return false;
		} else {
			bitSet.set(q);
			mutableBlockPos.set(l, o, m);
			if (this.canReplaceBlock(chunkAccess.getBlockState(mutableBlockPos))) {
				BlockState blockState;
				if (o <= 31) {
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
}
