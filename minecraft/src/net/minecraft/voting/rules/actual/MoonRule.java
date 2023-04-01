package net.minecraft.voting.rules.actual;

import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.CounterRule;
import net.minecraft.voting.rules.RuleChange;

public class MoonRule extends CounterRule {
	public static final int MAX_LEVEL = 3;

	public MoonRule() {
		super(0);
	}

	public double moonScale() {
		return (double)this.currentCount() / 3.0;
	}

	public boolean isBiggerThanNormal() {
		return this.moonScale() != 0.0;
	}

	@Override
	protected Component description(int i, int j) {
		i = Math.min(i, 3);
		j = Math.min(j, 3);
		return j > i ? Component.translatable("rule.moon." + j) : Component.translatable("rule.moon." + i);
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		return this.currentCount() == 3 ? Stream.empty() : Stream.of(this.relative(1));
	}
}
