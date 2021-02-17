package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.BiasedRangeDecoratorConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractBiasedRangeDecorator extends VerticalDecorator<BiasedRangeDecoratorConfiguration> {
	private static final Logger LOGGER = LogManager.getLogger();

	public AbstractBiasedRangeDecorator(Codec<BiasedRangeDecoratorConfiguration> codec) {
		super(codec);
	}

	protected int y(DecorationContext decorationContext, Random random, BiasedRangeDecoratorConfiguration biasedRangeDecoratorConfiguration, int i) {
		int j = biasedRangeDecoratorConfiguration.bottomInclusive().resolveY(decorationContext);
		int k = biasedRangeDecoratorConfiguration.topInclusive().resolveY(decorationContext);
		if (j >= k) {
			LOGGER.warn("Empty range decorator: {} [{}-{}]", this, j, k);
			return j;
		} else {
			return this.y(random, j, k, biasedRangeDecoratorConfiguration.cutoff());
		}
	}

	protected abstract int y(Random random, int i, int j, int k);
}
