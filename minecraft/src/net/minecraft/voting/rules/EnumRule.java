package net.minecraft.voting.rules;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;

public abstract class EnumRule<T> implements Rule {
	private final List<T> values;
	final T defaultValue;
	T value;
	private final Codec<RuleChange> codec;

	public EnumRule(T[] objects, T object, Codec<T> codec) {
		this(Arrays.asList(objects), object, codec);
	}

	public EnumRule(List<T> list, T object, Codec<T> codec) {
		this.values = list.stream().filter(object2 -> !object.equals(object2)).toList();
		this.defaultValue = object;
		this.value = object;
		this.codec = Rule.puntCodec(codec.xmap(objectx -> new EnumRule.EnumRuleChange(objectx), enumRuleChange -> enumRuleChange.targetValue));
	}

	public T get() {
		return this.value;
	}

	@Override
	public Stream<RuleChange> approvedChanges() {
		return !Objects.equals(this.value, this.defaultValue) ? Stream.of(new EnumRule.EnumRuleChange(this.value)) : Stream.empty();
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		ObjectArrayList<T> objectArrayList = new ObjectArrayList<>(this.values);
		Util.shuffle(objectArrayList, randomSource);
		return objectArrayList.stream()
			.filter(object -> !Objects.equals(object, this.defaultValue))
			.limit((long)i)
			.map(object -> new EnumRule.EnumRuleChange(object));
	}

	protected abstract Component valueDescription(T object);

	@Override
	public Codec<RuleChange> codec() {
		return this.codec;
	}

	class EnumRuleChange implements RuleChange.Simple {
		final T targetValue;
		private final Component description;

		EnumRuleChange(T object) {
			this.targetValue = object;
			this.description = EnumRule.this.valueDescription(object);
		}

		@Override
		public Rule rule() {
			return EnumRule.this;
		}

		@Override
		public Component description() {
			return this.description;
		}

		@Override
		public void update(RuleAction ruleAction) {
			EnumRule.this.value = (T)(switch (ruleAction) {
				case APPROVE -> this.targetValue;
				case REPEAL -> EnumRule.this.defaultValue;
			});
		}
	}
}
