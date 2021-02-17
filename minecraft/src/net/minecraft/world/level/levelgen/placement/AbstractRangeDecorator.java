package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractRangeDecorator extends VerticalDecorator<RangeDecoratorConfiguration> {
	private static final Logger LOGGER = LogManager.getLogger();

	public AbstractRangeDecorator(Codec<RangeDecoratorConfiguration> codec) {
		super(codec);
	}

	protected int y(DecorationContext decorationContext, Random random, RangeDecoratorConfiguration rangeDecoratorConfiguration, int i) {
		int j = rangeDecoratorConfiguration.bottomInclusive().resolveY(decorationContext);
		int k = rangeDecoratorConfiguration.topInclusive().resolveY(decorationContext);
		if (j >= k) {
			LOGGER.warn("Empty range decorator: {} [{}-{}]", this, j, k);
			return j;
		} else {
			return this.y(random, j, k);
		}
	}

	protected abstract int y(Random random, int i, int j);
}
