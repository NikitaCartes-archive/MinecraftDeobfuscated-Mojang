package net.minecraft.voting.rules;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;

public abstract class RandomNumberRule<T extends Number> implements Rule {
	final T defaultValue;
	private final Function<RandomSource, T> provider;
	T value;
	private final Codec<RuleChange> codec;

	public RandomNumberRule(T number, Function<RandomSource, T> function, Codec<T> codec) {
		this.provider = function;
		this.defaultValue = number;
		this.value = number;
		this.codec = Rule.puntCodec(
			codec.xmap(numberx -> new RandomNumberRule.RandomNumberRuleChange(numberx), randomNumberRuleChange -> randomNumberRuleChange.targetValue)
		);
	}

	public T get() {
		return this.value;
	}

	@Override
	public Stream<RuleChange> approvedChanges() {
		return !Objects.equals(this.value, this.defaultValue) ? Stream.of(new RandomNumberRule.RandomNumberRuleChange(this.value)) : Stream.empty();
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		return Stream.generate(() -> (Number)this.provider.apply(randomSource))
			.filter(number -> !Objects.equals(number, this.defaultValue))
			.limit((long)i)
			.distinct()
			.map(number -> new RandomNumberRule.RandomNumberRuleChange(number));
	}

	protected abstract Component valueDescription(T number);

	@Override
	public Codec<RuleChange> codec() {
		return this.codec;
	}

	public abstract static class RandomFloat extends RandomNumberRule<Float> {
		public RandomFloat(float f, FloatProvider floatProvider) {
			super(f, floatProvider::sample, Codec.FLOAT);
		}
	}

	public abstract static class RandomInt extends RandomNumberRule<Integer> {
		public RandomInt(int i, IntProvider intProvider) {
			super(i, intProvider::sample, Codec.INT);
		}
	}

	class RandomNumberRuleChange implements RuleChange.Simple {
		final T targetValue;
		private final Component description;

		RandomNumberRuleChange(T number) {
			this.targetValue = number;
			this.description = RandomNumberRule.this.valueDescription(number);
		}

		@Override
		public Rule rule() {
			return RandomNumberRule.this;
		}

		@Override
		public Component description() {
			return this.description;
		}

		@Override
		public void update(RuleAction ruleAction) {
			RandomNumberRule.this.value = (T)(switch (ruleAction) {
				case APPROVE -> this.targetValue;
				case REPEAL -> RandomNumberRule.this.defaultValue;
			});
		}
	}
}
