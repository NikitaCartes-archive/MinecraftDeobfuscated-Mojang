package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PumpkinBlockPileFeature extends BlockPileFeature {
	public PumpkinBlockPileFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	@Override
	protected BlockState getBlockState(LevelAccessor levelAccessor) {
		return levelAccessor.getRandom().nextFloat() < 0.95F ? Blocks.PUMPKIN.defaultBlockState() : Blocks.JACK_O_LANTERN.defaultBlockState();
	}
}
