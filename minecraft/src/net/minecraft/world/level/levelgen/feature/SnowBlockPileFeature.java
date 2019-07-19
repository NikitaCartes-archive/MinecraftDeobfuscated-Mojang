package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SnowBlockPileFeature extends BlockPileFeature {
	public SnowBlockPileFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	@Override
	protected BlockState getBlockState(LevelAccessor levelAccessor) {
		return Blocks.SNOW_BLOCK.defaultBlockState();
	}
}
