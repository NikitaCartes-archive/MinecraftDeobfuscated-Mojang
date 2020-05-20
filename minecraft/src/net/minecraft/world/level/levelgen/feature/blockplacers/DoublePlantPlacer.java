package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;

public class DoublePlantPlacer extends BlockPlacer {
	public static final Codec<DoublePlantPlacer> CODEC = Codec.unit((Supplier<DoublePlantPlacer>)(() -> DoublePlantPlacer.INSTANCE));
	public static final DoublePlantPlacer INSTANCE = new DoublePlantPlacer();

	@Override
	protected BlockPlacerType<?> type() {
		return BlockPlacerType.DOUBLE_PLANT_PLACER;
	}

	@Override
	public void place(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Random random) {
		((DoublePlantBlock)blockState.getBlock()).placeAt(levelAccessor, blockPos, 2);
	}
}
