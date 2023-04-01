package net.minecraft.voting.rules.actual;

import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.CounterRule;
import net.minecraft.voting.rules.RuleChange;

public class ExplosionExtraPowerRule extends CounterRule.Simple {
	public ExplosionExtraPowerRule() {
		super(0);
	}

	@Override
	public Component setDescription(int i) {
		return Component.translatable("rule.explosion_power.set", i);
	}

	@Override
	public Component changeDescription(int i, int j) {
		return Component.translatable("rule.explosion_power.change", i, j);
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		int j = this.currentCount();
		Builder<RuleChange> builder = Stream.builder();
		builder.accept(this.relative(1));
		if (randomSource.nextFloat() < 0.01F) {
			builder.accept(this.relative(3));
		}

		if (j > 0) {
			builder.accept(this.relative(-1));
		}

		return builder.build();
	}
}
