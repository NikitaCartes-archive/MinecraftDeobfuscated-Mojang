package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class EndGatewayPlacementDecorator extends VerticalDecorator<NoneDecoratorConfiguration> {
	public EndGatewayPlacementDecorator(Codec<NoneDecoratorConfiguration> codec) {
		super(codec);
	}

	protected int y(DecorationContext decorationContext, Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, int i) {
		return i + 3 + random.nextInt(7);
	}
}
