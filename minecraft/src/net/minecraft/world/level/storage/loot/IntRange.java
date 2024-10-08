package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class IntRange {
	private static final Codec<IntRange> RECORD_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					NumberProviders.CODEC.optionalFieldOf("min").forGetter(intRange -> Optional.ofNullable(intRange.min)),
					NumberProviders.CODEC.optionalFieldOf("max").forGetter(intRange -> Optional.ofNullable(intRange.max))
				)
				.apply(instance, IntRange::new)
	);
	public static final Codec<IntRange> CODEC = Codec.either(Codec.INT, RECORD_CODEC)
		.xmap(either -> either.map(IntRange::exact, Function.identity()), intRange -> {
			OptionalInt optionalInt = intRange.unpackExact();
			return optionalInt.isPresent() ? Either.left(optionalInt.getAsInt()) : Either.right(intRange);
		});
	@Nullable
	private final NumberProvider min;
	@Nullable
	private final NumberProvider max;
	private final IntRange.IntLimiter limiter;
	private final IntRange.IntChecker predicate;

	public Set<ContextKey<?>> getReferencedContextParams() {
		Builder<ContextKey<?>> builder = ImmutableSet.builder();
		if (this.min != null) {
			builder.addAll(this.min.getReferencedContextParams());
		}

		if (this.max != null) {
			builder.addAll(this.max.getReferencedContextParams());
		}

		return builder.build();
	}

	private IntRange(Optional<NumberProvider> optional, Optional<NumberProvider> optional2) {
		this((NumberProvider)optional.orElse(null), (NumberProvider)optional2.orElse(null));
	}

	private IntRange(@Nullable NumberProvider numberProvider, @Nullable NumberProvider numberProvider2) {
		this.min = numberProvider;
		this.max = numberProvider2;
		if (numberProvider == null) {
			if (numberProvider2 == null) {
				this.limiter = (lootContext, i) -> i;
				this.predicate = (lootContext, i) -> true;
			} else {
				this.limiter = (lootContext, i) -> Math.min(numberProvider2.getInt(lootContext), i);
				this.predicate = (lootContext, i) -> i <= numberProvider2.getInt(lootContext);
			}
		} else if (numberProvider2 == null) {
			this.limiter = (lootContext, i) -> Math.max(numberProvider.getInt(lootContext), i);
			this.predicate = (lootContext, i) -> i >= numberProvider.getInt(lootContext);
		} else {
			this.limiter = (lootContext, i) -> Mth.clamp(i, numberProvider.getInt(lootContext), numberProvider2.getInt(lootContext));
			this.predicate = (lootContext, i) -> i >= numberProvider.getInt(lootContext) && i <= numberProvider2.getInt(lootContext);
		}
	}

	public static IntRange exact(int i) {
		ConstantValue constantValue = ConstantValue.exactly((float)i);
		return new IntRange(Optional.of(constantValue), Optional.of(constantValue));
	}

	public static IntRange range(int i, int j) {
		return new IntRange(Optional.of(ConstantValue.exactly((float)i)), Optional.of(ConstantValue.exactly((float)j)));
	}

	public static IntRange lowerBound(int i) {
		return new IntRange(Optional.of(ConstantValue.exactly((float)i)), Optional.empty());
	}

	public static IntRange upperBound(int i) {
		return new IntRange(Optional.empty(), Optional.of(ConstantValue.exactly((float)i)));
	}

	public int clamp(LootContext lootContext, int i) {
		return this.limiter.apply(lootContext, i);
	}

	public boolean test(LootContext lootContext, int i) {
		return this.predicate.test(lootContext, i);
	}

	private OptionalInt unpackExact() {
		return Objects.equals(this.min, this.max)
				&& this.min instanceof ConstantValue constantValue
				&& Math.floor((double)constantValue.value()) == (double)constantValue.value()
			? OptionalInt.of((int)constantValue.value())
			: OptionalInt.empty();
	}

	@FunctionalInterface
	interface IntChecker {
		boolean test(LootContext lootContext, int i);
	}

	@FunctionalInterface
	interface IntLimiter {
		int apply(LootContext lootContext, int i);
	}
}
