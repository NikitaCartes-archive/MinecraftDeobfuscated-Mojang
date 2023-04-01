package net.minecraft.voting.rules;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;

public abstract class UniformIntRule implements Rule {
	private final IntProvider minValue;
	private final IntProvider span;
	final UniformInt defaultValue;
	UniformInt currentValue;
	private final Codec<RuleChange> codec;

	protected UniformIntRule(IntProvider intProvider, IntProvider intProvider2, UniformInt uniformInt) {
		this.defaultValue = uniformInt;
		this.currentValue = uniformInt;
		this.minValue = intProvider;
		this.span = intProvider2;
		this.codec = Rule.puntCodec(UniformInt.CODEC.xmap(uniformIntx -> new UniformIntRule.Change(uniformIntx), change -> change.newValue));
	}

	public IntProvider get() {
		return this.currentValue;
	}

	@Override
	public Codec<RuleChange> codec() {
		return this.codec;
	}

	@Override
	public Stream<RuleChange> approvedChanges() {
		return this.defaultValue.equals(this.currentValue) ? Stream.empty() : Stream.of(new UniformIntRule.Change(this.currentValue));
	}

	protected abstract Component valueDescription(UniformInt uniformInt);

	protected static Component rangeToComponent(UniformInt uniformInt, IntFunction<String> intFunction) {
		return Component.literal("[" + (String)intFunction.apply(uniformInt.getMinValue()) + "-" + (String)intFunction.apply(uniformInt.getMaxValue()) + "]");
	}

	protected static Component rangeToComponent(UniformInt uniformInt) {
		return rangeToComponent(uniformInt, String::valueOf);
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		return Stream.generate(() -> {
			int ix = this.minValue.sample(randomSource);
			int j = Math.max(0, this.span.sample(randomSource));
			int k = ix + j;
			return UniformInt.of(ix, k);
		}).limit((long)i).map(uniformInt -> new UniformIntRule.Change(uniformInt));
	}

	class Change implements RuleChange.Simple {
		private final Component description;
		final UniformInt newValue;

		Change(UniformInt uniformInt) {
			this.newValue = uniformInt;
			this.description = UniformIntRule.this.valueDescription(uniformInt);
		}

		@Override
		public Rule rule() {
			return UniformIntRule.this;
		}

		@Override
		public void update(RuleAction ruleAction) {
			UniformIntRule.this.currentValue = switch (ruleAction) {
				case APPROVE -> this.newValue;
				case REPEAL -> UniformIntRule.this.defaultValue;
			};
		}

		@Override
		public Component description() {
			return this.description;
		}
	}
}
