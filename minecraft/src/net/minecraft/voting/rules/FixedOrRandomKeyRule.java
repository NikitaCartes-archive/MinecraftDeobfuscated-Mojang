package net.minecraft.voting.rules;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public abstract class FixedOrRandomKeyRule<T> implements Rule {
	private static final MapCodec<Long> SEED_CODEC = Codec.LONG.fieldOf("seed");
	final Component reshuffleLabel;
	final Component randomizeLabel;
	private final ResourceKey<? extends Registry<T>> registry;
	final Either<ResourceKey<T>, Long> defaultValue;
	Either<ResourceKey<T>, Long> valueOrSeed;
	private final Codec<FixedOrRandomKeyRule<T>.Change> codec;

	protected FixedOrRandomKeyRule(ResourceKey<? extends Registry<T>> resourceKey, Component component, Component component2, ResourceKey<T> resourceKey2) {
		this.reshuffleLabel = component;
		this.randomizeLabel = component2;
		this.defaultValue = Either.left(resourceKey2);
		this.valueOrSeed = this.defaultValue;
		this.registry = resourceKey;
		MapCodec<ResourceKey<T>> mapCodec = ResourceKey.codec(resourceKey).fieldOf("value");
		this.codec = Codec.mapEither(mapCodec, SEED_CODEC)
			.<FixedOrRandomKeyRule<T>.Change>xmap(either -> new FixedOrRandomKeyRule.Change(either), change -> change.valueOrSeed)
			.codec();
	}

	@Override
	public Codec<RuleChange> codec() {
		return Rule.puntCodec(this.codec);
	}

	@Override
	public Stream<RuleChange> approvedChanges() {
		return this.valueOrSeed.equals(this.defaultValue) ? Stream.empty() : Stream.of(new FixedOrRandomKeyRule.Change(this.valueOrSeed));
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		Registry<T> registry = minecraftServer.registryAccess().registryOrThrow(this.registry);
		List<RuleChange> list = new ArrayList();
		list.add(new FixedOrRandomKeyRule.Change(Either.right(randomSource.nextLong())));
		list.add(new FixedOrRandomKeyRule.Change(Either.right(randomSource.nextLong())));
		list.add(new FixedOrRandomKeyRule.Change(Either.right(randomSource.nextLong())));

		for (int j = 0; j < Math.max(10, i); j++) {
			registry.getRandom(randomSource).ifPresent(reference -> list.add(new FixedOrRandomKeyRule.Change(Either.left(reference.key()))));
		}

		Util.shuffle((List<T>)list, randomSource);
		return list.stream().limit((long)i);
	}

	public Optional<Holder.Reference<T>> getValueForEntity(Entity entity) {
		Registry<T> registry = entity.level.registryAccess().registryOrThrow(this.registry);
		return this.valueOrSeed.map(registry::getHolder, long_ -> {
			long l = (long)entity.getUUID().hashCode() ^ long_;
			return registry.getRandom(RandomSource.create(l));
		});
	}

	protected abstract Component valueDescription(ResourceKey<T> resourceKey);

	public class Change implements RuleChange.Simple {
		final Either<ResourceKey<T>, Long> valueOrSeed;

		public Change(Either<ResourceKey<T>, Long> either) {
			this.valueOrSeed = either;
		}

		@Override
		public Rule rule() {
			return FixedOrRandomKeyRule.this;
		}

		@Override
		public void update(RuleAction ruleAction) {
			FixedOrRandomKeyRule.this.valueOrSeed = switch (ruleAction) {
				case APPROVE -> this.valueOrSeed;
				case REPEAL -> FixedOrRandomKeyRule.this.defaultValue;
			};
		}

		@Override
		public Component description() {
			return this.valueOrSeed
				.map(
					FixedOrRandomKeyRule.this::valueDescription,
					long_ -> !FixedOrRandomKeyRule.this.valueOrSeed.equals(this.valueOrSeed)
							? FixedOrRandomKeyRule.this.reshuffleLabel
							: FixedOrRandomKeyRule.this.randomizeLabel
				);
		}
	}
}
