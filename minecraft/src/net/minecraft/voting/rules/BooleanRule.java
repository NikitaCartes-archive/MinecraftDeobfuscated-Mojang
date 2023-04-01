package net.minecraft.voting.rules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;

public class BooleanRule implements Rule {
	final Component description;
	boolean value;
	private final BooleanRule.BooleanRuleChange change = new BooleanRule.BooleanRuleChange();
	private final Codec<RuleChange> codec = RecordCodecBuilder.create(instance -> instance.point(this.change));

	public BooleanRule(Component component) {
		this.description = component;
	}

	public boolean get() {
		return this.value;
	}

	@Override
	public Stream<RuleChange> approvedChanges() {
		return this.value ? Stream.of(this.change) : Stream.empty();
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		return !this.value && i > 0 ? Stream.of(this.change) : Stream.empty();
	}

	@Override
	public Codec<RuleChange> codec() {
		return this.codec;
	}

	class BooleanRuleChange implements RuleChange.Simple {
		@Override
		public Rule rule() {
			return BooleanRule.this;
		}

		@Override
		public Component description() {
			return BooleanRule.this.description;
		}

		@Override
		public void update(RuleAction ruleAction) {
			BooleanRule.this.value = ruleAction == RuleAction.APPROVE;
		}
	}
}
