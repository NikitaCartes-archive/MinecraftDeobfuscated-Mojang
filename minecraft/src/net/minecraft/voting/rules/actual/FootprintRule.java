package net.minecraft.voting.rules.actual;

import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.CounterRule;
import net.minecraft.voting.rules.RuleChange;

public class FootprintRule extends CounterRule {
	public static final int MAX_LEVEL = 11;

	public FootprintRule() {
		super(0);
	}

	public boolean footprintsEnabled() {
		return this.currentCount() >= 11;
	}

	@Override
	protected Component description(int i, int j) {
		i = Math.min(i, 11);
		j = Math.min(j, 11);
		return i == j
			? Component.translatable("rule.footprint." + i)
			: Component.translatable("rule.footprint", Component.translatable("rule.footprint." + i), Component.translatable("rule.footprint." + j));
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		return this.currentCount() == 11 ? Stream.empty() : Stream.of(this.relative(1));
	}
}
