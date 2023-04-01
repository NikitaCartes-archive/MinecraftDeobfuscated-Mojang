package net.minecraft.voting.rules;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;

public abstract class CounterRule implements Rule {
	final int defaultCount;
	int currentCount;
	private final Codec<RuleChange> codec;

	protected CounterRule(int i) {
		this.defaultCount = i;
		this.currentCount = i;
		MapCodec<CounterRule.CounterRuleChange> mapCodec = Codec.mapEither(Codec.INT.fieldOf("value"), Codec.INT.fieldOf("delta"))
			.xmap(
				either -> either.map(this::absolute, this::relative),
				counterRuleChange -> counterRuleChange.relative ? Either.right(counterRuleChange.value) : Either.left(counterRuleChange.value)
			);
		this.codec = Rule.puntCodec(mapCodec.codec());
	}

	public int currentCount() {
		return this.currentCount;
	}

	@Override
	public Codec<RuleChange> codec() {
		return Rule.puntCodec(this.codec);
	}

	protected abstract Component description(int i, int j);

	@Override
	public Stream<RuleChange> approvedChanges() {
		return this.currentCount != this.defaultCount ? Stream.of(this.absolute(this.currentCount)) : Stream.empty();
	}

	protected CounterRule.CounterRuleChange relative(int i) {
		return new CounterRule.CounterRuleChange(i, true);
	}

	protected CounterRule.CounterRuleChange absolute(int i) {
		return new CounterRule.CounterRuleChange(i, false);
	}

	protected class CounterRuleChange implements RuleChange {
		final int value;
		final boolean relative;

		public CounterRuleChange(int i, boolean bl) {
			this.value = i;
			this.relative = bl;
		}

		@Override
		public Rule rule() {
			return CounterRule.this;
		}

		private int next(RuleAction ruleAction) {
			if (this.relative) {
				return switch (ruleAction) {
					case APPROVE -> CounterRule.this.currentCount + this.value;
					case REPEAL -> CounterRule.this.currentCount - this.value;
				};
			} else {
				return switch (ruleAction) {
					case APPROVE -> CounterRule.this.defaultCount + this.value;
					case REPEAL -> CounterRule.this.defaultCount;
				};
			}
		}

		@Override
		public Component description(RuleAction ruleAction) {
			return CounterRule.this.description(CounterRule.this.currentCount, this.next(ruleAction));
		}

		@Override
		public void update(RuleAction ruleAction) {
			CounterRule.this.currentCount = this.next(ruleAction);
		}
	}

	public abstract static class Simple extends CounterRule {
		protected Simple(int i) {
			super(i);
		}

		@Override
		protected final Component description(int i, int j) {
			return i == j ? this.setDescription(i) : this.changeDescription(i, j);
		}

		public abstract Component setDescription(int i);

		public abstract Component changeDescription(int i, int j);
	}
}
