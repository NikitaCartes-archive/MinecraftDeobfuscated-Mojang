package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

public interface MinMaxBounds<T extends Number> {
	SimpleCommandExceptionType ERROR_EMPTY = new SimpleCommandExceptionType(Component.translatable("argument.range.empty"));
	SimpleCommandExceptionType ERROR_SWAPPED = new SimpleCommandExceptionType(Component.translatable("argument.range.swapped"));

	Optional<T> min();

	Optional<T> max();

	default boolean isAny() {
		return this.min().isEmpty() && this.max().isEmpty();
	}

	default Optional<T> unwrapPoint() {
		Optional<T> optional = this.min();
		Optional<T> optional2 = this.max();
		return optional.equals(optional2) ? optional : Optional.empty();
	}

	static <T extends Number, R extends MinMaxBounds<T>> Codec<R> createCodec(Codec<T> codec, MinMaxBounds.BoundsFactory<T, R> boundsFactory) {
		Codec<R> codec2 = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(codec, "min").forGetter(MinMaxBounds::min), ExtraCodecs.strictOptionalField(codec, "max").forGetter(MinMaxBounds::max)
					)
					.apply(instance, boundsFactory::create)
		);
		return Codec.either(codec2, codec)
			.xmap(either -> either.map(minMaxBounds -> minMaxBounds, number -> boundsFactory.create(Optional.of(number), Optional.of(number))), minMaxBounds -> {
				Optional<T> optional = minMaxBounds.unwrapPoint();
				return optional.isPresent() ? Either.right((Number)optional.get()) : Either.left(minMaxBounds);
			});
	}

	static <T extends Number, R extends MinMaxBounds<T>> R fromReader(
		StringReader stringReader,
		MinMaxBounds.BoundsFromReaderFactory<T, R> boundsFromReaderFactory,
		Function<String, T> function,
		Supplier<DynamicCommandExceptionType> supplier,
		Function<T, T> function2
	) throws CommandSyntaxException {
		if (!stringReader.canRead()) {
			throw ERROR_EMPTY.createWithContext(stringReader);
		} else {
			int i = stringReader.getCursor();

			try {
				Optional<T> optional = readNumber(stringReader, function, supplier).map(function2);
				Optional<T> optional2;
				if (stringReader.canRead(2) && stringReader.peek() == '.' && stringReader.peek(1) == '.') {
					stringReader.skip();
					stringReader.skip();
					optional2 = readNumber(stringReader, function, supplier).map(function2);
					if (optional.isEmpty() && optional2.isEmpty()) {
						throw ERROR_EMPTY.createWithContext(stringReader);
					}
				} else {
					optional2 = optional;
				}

				if (optional.isEmpty() && optional2.isEmpty()) {
					throw ERROR_EMPTY.createWithContext(stringReader);
				} else {
					return boundsFromReaderFactory.create(stringReader, optional, optional2);
				}
			} catch (CommandSyntaxException var8) {
				stringReader.setCursor(i);
				throw new CommandSyntaxException(var8.getType(), var8.getRawMessage(), var8.getInput(), i);
			}
		}
	}

	private static <T extends Number> Optional<T> readNumber(
		StringReader stringReader, Function<String, T> function, Supplier<DynamicCommandExceptionType> supplier
	) throws CommandSyntaxException {
		int i = stringReader.getCursor();

		while (stringReader.canRead() && isAllowedInputChat(stringReader)) {
			stringReader.skip();
		}

		String string = stringReader.getString().substring(i, stringReader.getCursor());
		if (string.isEmpty()) {
			return Optional.empty();
		} else {
			try {
				return Optional.of((Number)function.apply(string));
			} catch (NumberFormatException var6) {
				throw ((DynamicCommandExceptionType)supplier.get()).createWithContext(stringReader, string);
			}
		}
	}

	private static boolean isAllowedInputChat(StringReader stringReader) {
		char c = stringReader.peek();
		if ((c < '0' || c > '9') && c != '-') {
			return c != '.' ? false : !stringReader.canRead(2) || stringReader.peek(1) != '.';
		} else {
			return true;
		}
	}

	@FunctionalInterface
	public interface BoundsFactory<T extends Number, R extends MinMaxBounds<T>> {
		R create(Optional<T> optional, Optional<T> optional2);
	}

	@FunctionalInterface
	public interface BoundsFromReaderFactory<T extends Number, R extends MinMaxBounds<T>> {
		R create(StringReader stringReader, Optional<T> optional, Optional<T> optional2) throws CommandSyntaxException;
	}

	public static record Doubles(Optional<Double> min, Optional<Double> max, Optional<Double> minSq, Optional<Double> maxSq) implements MinMaxBounds<Double> {
		public static final MinMaxBounds.Doubles ANY = new MinMaxBounds.Doubles(Optional.empty(), Optional.empty());
		public static final Codec<MinMaxBounds.Doubles> CODEC = MinMaxBounds.createCodec((Codec<T>)Codec.DOUBLE, MinMaxBounds.Doubles::new);

		private Doubles(Optional<Double> optional, Optional<Double> optional2) {
			this(optional, optional2, squareOpt(optional), squareOpt(optional2));
		}

		private static MinMaxBounds.Doubles create(StringReader stringReader, Optional<Double> optional, Optional<Double> optional2) throws CommandSyntaxException {
			if (optional.isPresent() && optional2.isPresent() && (Double)optional.get() > (Double)optional2.get()) {
				throw ERROR_SWAPPED.createWithContext(stringReader);
			} else {
				return new MinMaxBounds.Doubles(optional, optional2);
			}
		}

		private static Optional<Double> squareOpt(Optional<Double> optional) {
			return optional.map(double_ -> double_ * double_);
		}

		public static MinMaxBounds.Doubles exactly(double d) {
			return new MinMaxBounds.Doubles(Optional.of(d), Optional.of(d));
		}

		public static MinMaxBounds.Doubles between(double d, double e) {
			return new MinMaxBounds.Doubles(Optional.of(d), Optional.of(e));
		}

		public static MinMaxBounds.Doubles atLeast(double d) {
			return new MinMaxBounds.Doubles(Optional.of(d), Optional.empty());
		}

		public static MinMaxBounds.Doubles atMost(double d) {
			return new MinMaxBounds.Doubles(Optional.empty(), Optional.of(d));
		}

		public boolean matches(double d) {
			return this.min.isPresent() && this.min.get() > d ? false : this.max.isEmpty() || !((Double)this.max.get() < d);
		}

		public boolean matchesSqr(double d) {
			return this.minSq.isPresent() && this.minSq.get() > d ? false : this.maxSq.isEmpty() || !((Double)this.maxSq.get() < d);
		}

		public static MinMaxBounds.Doubles fromJson(@Nullable JsonElement jsonElement) {
			return jsonElement != null && !jsonElement.isJsonNull() ? Util.getOrThrow(CODEC.parse(JsonOps.INSTANCE, jsonElement), JsonParseException::new) : ANY;
		}

		public JsonElement serializeToJson() {
			return (JsonElement)(this.isAny() ? JsonNull.INSTANCE : Util.getOrThrow(CODEC.encodeStart(JsonOps.INSTANCE, this), IllegalStateException::new));
		}

		public static MinMaxBounds.Doubles fromReader(StringReader stringReader) throws CommandSyntaxException {
			return fromReader(stringReader, double_ -> double_);
		}

		public static MinMaxBounds.Doubles fromReader(StringReader stringReader, Function<Double, Double> function) throws CommandSyntaxException {
			return MinMaxBounds.fromReader(
				stringReader, MinMaxBounds.Doubles::create, Double::parseDouble, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidDouble, (Function<T, T>)function
			);
		}
	}

	public static record Ints(Optional<Integer> min, Optional<Integer> max, Optional<Long> minSq, Optional<Long> maxSq) implements MinMaxBounds<Integer> {
		public static final MinMaxBounds.Ints ANY = new MinMaxBounds.Ints(Optional.empty(), Optional.empty());
		public static final Codec<MinMaxBounds.Ints> CODEC = MinMaxBounds.createCodec((Codec<T>)Codec.INT, MinMaxBounds.Ints::new);

		private Ints(Optional<Integer> optional, Optional<Integer> optional2) {
			this(optional, optional2, optional.map(integer -> integer.longValue() * integer.longValue()), squareOpt(optional2));
		}

		private static MinMaxBounds.Ints create(StringReader stringReader, Optional<Integer> optional, Optional<Integer> optional2) throws CommandSyntaxException {
			if (optional.isPresent() && optional2.isPresent() && (Integer)optional.get() > (Integer)optional2.get()) {
				throw ERROR_SWAPPED.createWithContext(stringReader);
			} else {
				return new MinMaxBounds.Ints(optional, optional2);
			}
		}

		private static Optional<Long> squareOpt(Optional<Integer> optional) {
			return optional.map(integer -> integer.longValue() * integer.longValue());
		}

		public static MinMaxBounds.Ints exactly(int i) {
			return new MinMaxBounds.Ints(Optional.of(i), Optional.of(i));
		}

		public static MinMaxBounds.Ints between(int i, int j) {
			return new MinMaxBounds.Ints(Optional.of(i), Optional.of(j));
		}

		public static MinMaxBounds.Ints atLeast(int i) {
			return new MinMaxBounds.Ints(Optional.of(i), Optional.empty());
		}

		public static MinMaxBounds.Ints atMost(int i) {
			return new MinMaxBounds.Ints(Optional.empty(), Optional.of(i));
		}

		public boolean matches(int i) {
			return this.min.isPresent() && this.min.get() > i ? false : this.max.isEmpty() || (Integer)this.max.get() >= i;
		}

		public boolean matchesSqr(long l) {
			return this.minSq.isPresent() && this.minSq.get() > l ? false : this.maxSq.isEmpty() || (Long)this.maxSq.get() >= l;
		}

		public static MinMaxBounds.Ints fromJson(@Nullable JsonElement jsonElement) {
			return jsonElement != null && !jsonElement.isJsonNull() ? Util.getOrThrow(CODEC.parse(JsonOps.INSTANCE, jsonElement), JsonParseException::new) : ANY;
		}

		public JsonElement serializeToJson() {
			return (JsonElement)(this.isAny() ? JsonNull.INSTANCE : Util.getOrThrow(CODEC.encodeStart(JsonOps.INSTANCE, this), IllegalStateException::new));
		}

		public static MinMaxBounds.Ints fromReader(StringReader stringReader) throws CommandSyntaxException {
			return fromReader(stringReader, integer -> integer);
		}

		public static MinMaxBounds.Ints fromReader(StringReader stringReader, Function<Integer, Integer> function) throws CommandSyntaxException {
			return MinMaxBounds.fromReader(
				stringReader, MinMaxBounds.Ints::create, Integer::parseInt, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidInt, (Function<T, T>)function
			);
		}
	}
}
