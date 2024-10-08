package net.minecraft.world.level.storage.loot.providers.number;

import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;

public record BinomialDistributionGenerator(NumberProvider n, NumberProvider p) implements NumberProvider {
	public static final MapCodec<BinomialDistributionGenerator> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					NumberProviders.CODEC.fieldOf("n").forGetter(BinomialDistributionGenerator::n),
					NumberProviders.CODEC.fieldOf("p").forGetter(BinomialDistributionGenerator::p)
				)
				.apply(instance, BinomialDistributionGenerator::new)
	);

	@Override
	public LootNumberProviderType getType() {
		return NumberProviders.BINOMIAL;
	}

	@Override
	public int getInt(LootContext lootContext) {
		int i = this.n.getInt(lootContext);
		float f = this.p.getFloat(lootContext);
		RandomSource randomSource = lootContext.getRandom();
		int j = 0;

		for (int k = 0; k < i; k++) {
			if (randomSource.nextFloat() < f) {
				j++;
			}
		}

		return j;
	}

	@Override
	public float getFloat(LootContext lootContext) {
		return (float)this.getInt(lootContext);
	}

	public static BinomialDistributionGenerator binomial(int i, float f) {
		return new BinomialDistributionGenerator(ConstantValue.exactly((float)i), ConstantValue.exactly(f));
	}

	@Override
	public Set<ContextKey<?>> getReferencedContextParams() {
		return Sets.<ContextKey<?>>union(this.n.getReferencedContextParams(), this.p.getReferencedContextParams());
	}
}
