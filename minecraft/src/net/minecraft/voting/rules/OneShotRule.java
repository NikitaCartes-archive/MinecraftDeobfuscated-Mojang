package net.minecraft.voting.rules;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;

public abstract class OneShotRule implements Rule {
	static final Component WHAT = Component.literal("???");

	@Override
	public Stream<RuleChange> approvedChanges() {
		return Stream.empty();
	}

	protected abstract class OneShotRuleChange implements RuleChange {
		@Override
		public Rule rule() {
			return OneShotRule.this;
		}

		protected abstract Component description();

		@Override
		public Component description(RuleAction ruleAction) {
			return switch (ruleAction) {
				case REPEAL -> OneShotRule.WHAT;
				case APPROVE -> this.description();
			};
		}

		public abstract void run(MinecraftServer minecraftServer);

		@Override
		public void apply(RuleAction ruleAction, MinecraftServer minecraftServer) {
			RuleChange.super.apply(ruleAction, minecraftServer);
			if (ruleAction == RuleAction.APPROVE) {
				this.run(minecraftServer);
			}
		}

		@Override
		public void update(RuleAction ruleAction) {
		}
	}

	public abstract static class Resettable extends OneShotRule.Simple {
		protected abstract Optional<RuleChange> resetChange(MinecraftServer minecraftServer, RandomSource randomSource);

		@Override
		public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
			Optional<RuleChange> optional = this.resetChange(minecraftServer, randomSource);
			Stream<RuleChange> stream = Stream.generate(() -> this.randomApprovableChange(minecraftServer, randomSource)).flatMap(Optional::stream);
			return randomSource.nextBoolean() ? optional.stream().limit((long)i) : stream.limit((long)i);
		}
	}

	public abstract static class Simple extends OneShotRule {
		protected abstract Optional<RuleChange> randomApprovableChange(MinecraftServer minecraftServer, RandomSource randomSource);

		@Override
		public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
			return IntStream.range(0, i * 3).mapToObj(ix -> this.randomApprovableChange(minecraftServer, randomSource)).flatMap(Optional::stream).limit((long)i);
		}
	}
}
