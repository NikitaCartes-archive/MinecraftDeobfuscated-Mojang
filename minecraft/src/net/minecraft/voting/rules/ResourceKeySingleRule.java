package net.minecraft.voting.rules;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;

public abstract class ResourceKeySingleRule<T> implements Rule {
	private final ResourceKey<? extends Registry<T>> registry;
	final ResourceKey<T> defaultValue;
	ResourceKey<T> currentValue;
	private final Codec<RuleChange> codec;

	public ResourceKeySingleRule(ResourceKey<? extends Registry<T>> resourceKey, ResourceKey<T> resourceKey2) {
		this.registry = resourceKey;
		this.defaultValue = resourceKey2;
		this.currentValue = resourceKey2;
		this.codec = Rule.puntCodec(ResourceKey.codec(resourceKey).xmap(resourceKeyx -> new ResourceKeySingleRule.Change(resourceKeyx), change -> change.value));
	}

	@Override
	public Codec<RuleChange> codec() {
		return this.codec;
	}

	public ResourceKey<T> currentValue() {
		return this.currentValue;
	}

	public ResourceKey<T> defaultValue() {
		return this.defaultValue;
	}

	@Override
	public Stream<RuleChange> approvedChanges() {
		return this.currentValue.equals(this.defaultValue) ? Stream.empty() : Stream.of(new ResourceKeySingleRule.Change(this.currentValue));
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		Registry<T> registry = minecraftServer.registryAccess().registryOrThrow(this.registry);
		return Stream.generate(() -> registry.getRandom(randomSource))
			.flatMap(Optional::stream)
			.filter(reference -> !reference.is(this.defaultValue) && !reference.is(this.currentValue))
			.limit((long)i)
			.map(reference -> new ResourceKeySingleRule.Change(reference.key()));
	}

	protected abstract Component valueDescription(ResourceKey<T> resourceKey);

	class Change implements RuleChange.Simple {
		final ResourceKey<T> value;

		Change(ResourceKey<T> resourceKey) {
			this.value = resourceKey;
		}

		@Override
		public Rule rule() {
			return ResourceKeySingleRule.this;
		}

		@Override
		public void update(RuleAction ruleAction) {
			ResourceKeySingleRule.this.currentValue = switch (ruleAction) {
				case APPROVE -> this.value;
				case REPEAL -> ResourceKeySingleRule.this.defaultValue;
			};
		}

		@Override
		public Component description() {
			return ResourceKeySingleRule.this.valueDescription(this.value);
		}
	}
}
