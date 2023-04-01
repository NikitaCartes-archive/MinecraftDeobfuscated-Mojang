package net.minecraft.voting.rules;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public abstract class SetRule<T> implements Rule {
	private final Set<T> values = new HashSet();

	protected abstract Codec<T> elementCodec();

	protected abstract Component description(T object);

	@Override
	public Stream<RuleChange> approvedChanges() {
		return this.values.stream().map(object -> new SetRule.SetRuleChange(object));
	}

	@Override
	public Codec<RuleChange> codec() {
		return Rule.puntCodec(this.elementCodec().xmap(object -> new SetRule.SetRuleChange(object), setRuleChange -> setRuleChange.target));
	}

	public boolean contains(T object) {
		return this.values.contains(object);
	}

	protected boolean remove(T object) {
		return this.values.remove(object);
	}

	protected boolean add(T object) {
		return this.values.add(object);
	}

	public Collection<T> values() {
		return Collections.unmodifiableCollection(this.values);
	}

	protected void applyEffect(RuleAction ruleAction, MinecraftServer minecraftServer) {
	}

	protected class SetRuleChange implements RuleChange.Simple {
		final T target;

		public SetRuleChange(T object) {
			this.target = object;
		}

		@Override
		public Rule rule() {
			return SetRule.this;
		}

		@Override
		public Component description() {
			return SetRule.this.description(this.target);
		}

		@Override
		public void update(RuleAction ruleAction) {
			T object = this.target;
			if (ruleAction == RuleAction.APPROVE) {
				SetRule.this.add(object);
			} else {
				SetRule.this.remove(object);
			}
		}

		@Override
		public void apply(RuleAction ruleAction, MinecraftServer minecraftServer) {
			RuleChange.Simple.super.apply(ruleAction, minecraftServer);
			SetRule.this.applyEffect(ruleAction, minecraftServer);
		}
	}
}
