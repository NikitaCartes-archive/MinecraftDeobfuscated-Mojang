package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class LakeLavaPlacementDecorator extends FeatureDecorator<ChanceDecoratorConfiguration> {
	public LakeLavaPlacementDecorator(Codec<ChanceDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(
		DecorationContext decorationContext, Random random, ChanceDecoratorConfiguration chanceDecoratorConfiguration, BlockPos blockPos
	) {
		if (random.nextInt(chanceDecoratorConfiguration.chance / 10) == 0) {
			int i = random.nextInt(16) + blockPos.getX();
			int j = random.nextInt(16) + blockPos.getZ();
			int k = random.nextInt(random.nextInt(decorationContext.getGenDepth() - 8) + 8);
			if (k < decorationContext.getSeaLevel() || random.nextInt(chanceDecoratorConfiguration.chance / 8) == 0) {
				return Stream.of(new BlockPos(i, k, j));
			}
		}

		return Stream.empty();
	}
}
