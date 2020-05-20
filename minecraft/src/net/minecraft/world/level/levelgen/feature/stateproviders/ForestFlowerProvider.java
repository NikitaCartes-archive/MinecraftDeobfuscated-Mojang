package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ForestFlowerProvider extends BlockStateProvider {
	public static final Codec<ForestFlowerProvider> CODEC = Codec.unit((Supplier<ForestFlowerProvider>)(() -> ForestFlowerProvider.INSTANCE));
	private static final BlockState[] FLOWERS = new BlockState[]{
		Blocks.DANDELION.defaultBlockState(),
		Blocks.POPPY.defaultBlockState(),
		Blocks.ALLIUM.defaultBlockState(),
		Blocks.AZURE_BLUET.defaultBlockState(),
		Blocks.RED_TULIP.defaultBlockState(),
		Blocks.ORANGE_TULIP.defaultBlockState(),
		Blocks.WHITE_TULIP.defaultBlockState(),
		Blocks.PINK_TULIP.defaultBlockState(),
		Blocks.OXEYE_DAISY.defaultBlockState(),
		Blocks.CORNFLOWER.defaultBlockState(),
		Blocks.LILY_OF_THE_VALLEY.defaultBlockState()
	};
	public static final ForestFlowerProvider INSTANCE = new ForestFlowerProvider();

	@Override
	protected BlockStateProviderType<?> type() {
		return BlockStateProviderType.FOREST_FLOWER_PROVIDER;
	}

	@Override
	public BlockState getState(Random random, BlockPos blockPos) {
		double d = Mth.clamp((1.0 + Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() / 48.0, (double)blockPos.getZ() / 48.0, false)) / 2.0, 0.0, 0.9999);
		return FLOWERS[(int)(d * (double)FLOWERS.length)];
	}
}
