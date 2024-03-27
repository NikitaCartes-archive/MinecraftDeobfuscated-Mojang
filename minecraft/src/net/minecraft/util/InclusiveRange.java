package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public record InclusiveRange<T extends Comparable<T>>(T minInclusive, T maxInclusive) {
	public static final Codec<InclusiveRange<Integer>> INT = codec((Codec<T>)Codec.INT);

	public InclusiveRange(T minInclusive, T maxInclusive) {
		if (minInclusive.compareTo(maxInclusive) > 0) {
			throw new IllegalArgumentException("min_inclusive must be less than or equal to max_inclusive");
		} else {
			this.minInclusive = minInclusive;
			this.maxInclusive = maxInclusive;
		}
	}

	public InclusiveRange(T comparable) {
		this(comparable, comparable);
	}

	public static <T extends Comparable<T>> Codec<InclusiveRange<T>> codec(Codec<T> codec) {
		return ExtraCodecs.intervalCodec(codec, "min_inclusive", "max_inclusive", InclusiveRange::create, InclusiveRange::minInclusive, InclusiveRange::maxInclusive);
	}

	public static <T extends Comparable<T>> Codec<InclusiveRange<T>> codec(Codec<T> codec, T comparable, T comparable2) {
		return codec(codec)
			.validate(
				inclusiveRange -> {
					if (inclusiveRange.minInclusive().compareTo(comparable) < 0) {
						return DataResult.error(
							() -> "Range limit too low, expected at least " + comparable + " [" + inclusiveRange.minInclusive() + "-" + inclusiveRange.maxInclusive() + "]"
						);
					} else {
						return inclusiveRange.maxInclusive().compareTo(comparable2) > 0
							? DataResult.error(
								() -> "Range limit too high, expected at most " + comparable2 + " [" + inclusiveRange.minInclusive() + "-" + inclusiveRange.maxInclusive() + "]"
							)
							: DataResult.success(inclusiveRange);
					}
				}
			);
	}

	public static <T extends Comparable<T>> DataResult<InclusiveRange<T>> create(T comparable, T comparable2) {
		return comparable.compareTo(comparable2) <= 0
			? DataResult.success(new InclusiveRange<>(comparable, comparable2))
			: DataResult.error(() -> "min_inclusive must be less than or equal to max_inclusive");
	}

	public boolean isValueInRange(T comparable) {
		return comparable.compareTo(this.minInclusive) >= 0 && comparable.compareTo(this.maxInclusive) <= 0;
	}

	public boolean contains(InclusiveRange<T> inclusiveRange) {
		return inclusiveRange.minInclusive().compareTo(this.minInclusive) >= 0 && inclusiveRange.maxInclusive.compareTo(this.maxInclusive) <= 0;
	}

	public String toString() {
		return "[" + this.minInclusive + ", " + this.maxInclusive + "]";
	}
}
