package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DefaultFlowerFeature extends FlowerFeature {
	public DefaultFlowerFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	@Override
	public BlockState getRandomFlower(Random random, BlockPos blockPos) {
		return random.nextFloat() > 0.6666667F ? Blocks.DANDELION.defaultBlockState() : Blocks.POPPY.defaultBlockState();
	}
}
