package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SwampFlowerFeature extends FlowerFeature {
	public SwampFlowerFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	@Override
	public BlockState getRandomFlower(Random random, BlockPos blockPos) {
		return Blocks.BLUE_ORCHID.defaultBlockState();
	}
}
