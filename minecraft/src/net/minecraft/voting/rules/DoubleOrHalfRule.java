package net.minecraft.voting.rules;

import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;

public abstract class DoubleOrHalfRule extends CounterRule.Simple {
	private final int min;
	private final int max;

	public DoubleOrHalfRule(int i, int j, int k, Component component, Component component2) {
		super(i);
		this.min = j;
		this.max = k;
	}

	public int getInt() {
		return 1 << this.currentCount();
	}

	public float getFloat() {
		return (float)Math.pow(2.0, (double)this.currentCount());
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		Builder<RuleChange> builder = Stream.builder();
		int j = this.currentCount();
		if (j < this.max) {
			builder.add(this.relative(1));
		}

		if (j > this.min) {
			builder.add(this.relative(-1));
		}

		return builder.build();
	}
}
