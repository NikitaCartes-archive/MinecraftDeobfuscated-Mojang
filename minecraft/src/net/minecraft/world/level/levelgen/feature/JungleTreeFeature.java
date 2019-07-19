package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.world.level.block.state.BlockState;

public class JungleTreeFeature extends TreeFeature {
	public JungleTreeFeature(
		Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, boolean bl, int i, BlockState blockState, BlockState blockState2, boolean bl2
	) {
		super(function, bl, i, blockState, blockState2, bl2);
	}

	@Override
	protected int getTreeHeight(Random random) {
		return this.baseHeight + random.nextInt(7);
	}
}
