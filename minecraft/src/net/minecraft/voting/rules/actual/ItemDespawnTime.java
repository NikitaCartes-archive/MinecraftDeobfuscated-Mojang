package net.minecraft.voting.rules.actual;

import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.voting.rules.CounterRule;
import net.minecraft.voting.rules.RuleChange;

public class ItemDespawnTime extends CounterRule.Simple {
	public ItemDespawnTime() {
		super(6000);
	}

	@Override
	public Component setDescription(int i) {
		return Component.translatable("rule.item_despawn_time.set", StringUtil.formatTickDuration((long)i));
	}

	@Override
	public Component changeDescription(int i, int j) {
		return Component.translatable("rule.item_despawn_time.change", StringUtil.formatTickDuration((long)i), StringUtil.formatTickDuration((long)j));
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		int j = this.currentCount();
		Builder<RuleChange> builder = Stream.builder();
		if (j > 1200) {
			builder.accept(this.relative(-1200));
		}

		builder.accept(this.relative(1200));
		return builder.build();
	}
}
