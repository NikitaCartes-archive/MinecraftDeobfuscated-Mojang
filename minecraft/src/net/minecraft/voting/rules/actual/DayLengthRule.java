package net.minecraft.voting.rules.actual;

import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.voting.rules.CounterRule;
import net.minecraft.voting.rules.RuleChange;

public class DayLengthRule extends CounterRule.Simple {
	private static final int MINIMUM = 100;

	public DayLengthRule() {
		super(24000);
	}

	public int delta(RandomSource randomSource) {
		float f = 24000.0F / (float)this.currentCount();
		int i = Mth.floor(f);
		if (randomSource.nextFloat() < Mth.frac(f)) {
			i++;
		}

		return i;
	}

	@Override
	public Component setDescription(int i) {
		return Component.translatable("rule.day_length.set", StringUtil.formatTickDuration((long)i));
	}

	@Override
	public Component changeDescription(int i, int j) {
		return Component.translatable("rule.day_length.change", StringUtil.formatTickDuration((long)i), StringUtil.formatTickDuration((long)j));
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		int j = this.currentCount();
		Builder<RuleChange> builder = Stream.builder();
		if (j > 100 && randomSource.nextBoolean()) {
			builder.accept(this.relative(-j / 2));
		}

		builder.accept(this.relative(j));
		return builder.build();
	}
}
