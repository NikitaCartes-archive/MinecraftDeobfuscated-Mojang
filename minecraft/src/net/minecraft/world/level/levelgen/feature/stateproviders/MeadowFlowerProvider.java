package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MeadowFlowerProvider extends BlockStateProvider {
	public static final Codec<MeadowFlowerProvider> CODEC = Codec.unit((Supplier<MeadowFlowerProvider>)(() -> MeadowFlowerProvider.INSTANCE));
	private static final BlockState[] FLOWERS = new BlockState[]{
		Blocks.TALL_GRASS.defaultBlockState(),
		Blocks.ALLIUM.defaultBlockState(),
		Blocks.POPPY.defaultBlockState(),
		Blocks.AZURE_BLUET.defaultBlockState(),
		Blocks.DANDELION.defaultBlockState(),
		Blocks.CORNFLOWER.defaultBlockState(),
		Blocks.OXEYE_DAISY.defaultBlockState(),
		Blocks.GRASS.defaultBlockState()
	};
	public static final int MIN_VARIETY = 1;
	public static final int MAX_VARIETY = 3;
	public static final double SLOW_SCALE = 5.0E-4;
	public static final double FAST_SCALE = 0.1;
	public static final MeadowFlowerProvider INSTANCE = new MeadowFlowerProvider();

	@Override
	protected BlockStateProviderType<?> type() {
		return BlockStateProviderType.MEADOW_FLOWER_PROVIDER;
	}

	@Override
	public BlockState getState(Random random, BlockPos blockPos) {
		double d = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() * 5.0E-4, (double)blockPos.getZ() * 5.0E-4, false);
		int i = (int)Mth.map(d, -1.0, 1.0, 1.0, 4.0);
		List<BlockState> list = Lists.<BlockState>newArrayList();

		for (int j = 0; j < i; j++) {
			int k = this.getRandomArrayIndex(blockPos.offset(j * 234349, 0, 0), FLOWERS.length, 5.0E-4);
			list.add(FLOWERS[k]);
		}

		return (BlockState)list.get(this.getRandomArrayIndex(blockPos, list.size(), 0.1));
	}

	private int getRandomArrayIndex(BlockPos blockPos, int i, double d) {
		double e = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() * d, (double)blockPos.getZ() * d, false);
		double f = Mth.clamp((1.0 + e) / 2.0, 0.0, 0.9999);
		return (int)(f * (double)i);
	}
}
