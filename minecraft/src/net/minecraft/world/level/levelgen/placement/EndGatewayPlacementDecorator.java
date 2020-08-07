package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class EndGatewayPlacementDecorator extends FeatureDecorator<NoneDecoratorConfiguration> {
	public EndGatewayPlacementDecorator(Codec<NoneDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(
		DecorationContext decorationContext, Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos
	) {
		if (random.nextInt(700) == 0) {
			int i = random.nextInt(16) + blockPos.getX();
			int j = random.nextInt(16) + blockPos.getZ();
			int k = decorationContext.getHeight(Heightmap.Types.MOTION_BLOCKING, i, j);
			if (k > 0) {
				int l = k + 3 + random.nextInt(7);
				return Stream.of(new BlockPos(i, l, j));
			}
		}

		return Stream.empty();
	}
}
