package net.minecraft.server.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record Filterable<T>(T raw, Optional<T> filtered) {
	public static <T> Codec<Filterable<T>> codec(Codec<T> codec) {
		Codec<Filterable<T>> codec2 = RecordCodecBuilder.create(
			instance -> instance.group(
						codec.fieldOf("text").forGetter(Filterable::raw), ExtraCodecs.strictOptionalField(codec, "filtered").forGetter(Filterable::filtered)
					)
					.apply(instance, Filterable::new)
		);
		Codec<Filterable<T>> codec3 = codec.xmap(Filterable::passThrough, Filterable::raw);
		return ExtraCodecs.withAlternative(codec2, codec3);
	}

	public static <B extends ByteBuf, T> StreamCodec<B, Filterable<T>> streamCodec(StreamCodec<B, T> streamCodec) {
		return StreamCodec.composite(streamCodec, Filterable::raw, streamCodec.apply(ByteBufCodecs::optional), Filterable::filtered, Filterable::new);
	}

	public static <T> Filterable<T> passThrough(T object) {
		return new Filterable<>(object, Optional.empty());
	}

	public static Filterable<String> from(FilteredText filteredText) {
		return new Filterable<>(filteredText.raw(), filteredText.isFiltered() ? Optional.of(filteredText.filteredOrEmpty()) : Optional.empty());
	}

	public T get(boolean bl) {
		return (T)(bl ? this.filtered.orElse(this.raw) : this.raw);
	}

	public <U> Filterable<U> map(Function<T, U> function) {
		return (Filterable<U>)(new Filterable<>(function.apply(this.raw), this.filtered.map(function)));
	}

	public <U> Optional<Filterable<U>> resolve(Function<T, Optional<U>> function) {
		Optional<U> optional = (Optional<U>)function.apply(this.raw);
		if (optional.isEmpty()) {
			return Optional.empty();
		} else if (this.filtered.isPresent()) {
			Optional<U> optional2 = (Optional<U>)function.apply(this.filtered.get());
			return optional2.isEmpty() ? Optional.empty() : Optional.of(new Filterable<>(optional.get(), optional2));
		} else {
			return Optional.of(new Filterable<>(optional.get(), Optional.empty()));
		}
	}
}
