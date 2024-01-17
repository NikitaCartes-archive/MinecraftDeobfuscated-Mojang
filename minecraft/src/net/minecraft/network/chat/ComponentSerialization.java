package net.minecraft.network.chat;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

public class ComponentSerialization {
	public static final Codec<Component> CODEC = ExtraCodecs.recursive("Component", ComponentSerialization::createCodec);
	public static final StreamCodec<ByteBuf, Component> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
	public static final Codec<Component> FLAT_CODEC = ExtraCodecs.FLAT_JSON
		.flatXmap(jsonElement -> CODEC.parse(JsonOps.INSTANCE, jsonElement), component -> CODEC.encodeStart(JsonOps.INSTANCE, component));

	private static MutableComponent createFromList(List<Component> list) {
		MutableComponent mutableComponent = ((Component)list.get(0)).copy();

		for (int i = 1; i < list.size(); i++) {
			mutableComponent.append((Component)list.get(i));
		}

		return mutableComponent;
	}

	public static <T extends StringRepresentable, E> MapCodec<E> createLegacyComponentMatcher(
		T[] stringRepresentables, Function<T, MapCodec<? extends E>> function, Function<E, T> function2, String string
	) {
		MapCodec<E> mapCodec = new ComponentSerialization.FuzzyCodec(
			Stream.of(stringRepresentables).map(function).toList(), object -> (MapEncoder)function.apply((StringRepresentable)function2.apply(object))
		);
		Codec<T> codec = StringRepresentable.fromValues(() -> stringRepresentables);
		MapCodec<E> mapCodec2 = codec.dispatchMap(string, function2, stringRepresentable -> ((MapCodec)function.apply(stringRepresentable)).codec());
		MapCodec<E> mapCodec3 = new ComponentSerialization.StrictEither<>(string, mapCodec2, mapCodec);
		return ExtraCodecs.orCompressed(mapCodec3, mapCodec2);
	}

	private static Codec<Component> createCodec(Codec<Component> codec) {
		ComponentContents.Type<?>[] types = new ComponentContents.Type[]{
			PlainTextContents.TYPE, TranslatableContents.TYPE, KeybindContents.TYPE, ScoreContents.TYPE, SelectorContents.TYPE, NbtContents.TYPE
		};
		MapCodec<ComponentContents> mapCodec = createLegacyComponentMatcher(types, ComponentContents.Type::codec, ComponentContents::type, "type");
		Codec<Component> codec2 = RecordCodecBuilder.create(
			instance -> instance.group(
						mapCodec.forGetter(Component::getContents),
						ExtraCodecs.strictOptionalField(ExtraCodecs.nonEmptyList(codec.listOf()), "extra", List.of()).forGetter(Component::getSiblings),
						Style.Serializer.MAP_CODEC.forGetter(Component::getStyle)
					)
					.apply(instance, MutableComponent::new)
		);
		return Codec.either(Codec.either(Codec.STRING, ExtraCodecs.nonEmptyList(codec.listOf())), codec2)
			.xmap(either -> either.map(eitherx -> eitherx.map(Component::literal, ComponentSerialization::createFromList), component -> component), component -> {
				String string = component.tryCollapseToString();
				return string != null ? Either.left(Either.left(string)) : Either.right(component);
			});
	}

	static class FuzzyCodec<T> extends MapCodec<T> {
		private final List<MapCodec<? extends T>> codecs;
		private final Function<T, MapEncoder<? extends T>> encoderGetter;

		public FuzzyCodec(List<MapCodec<? extends T>> list, Function<T, MapEncoder<? extends T>> function) {
			this.codecs = list;
			this.encoderGetter = function;
		}

		@Override
		public <S> DataResult<T> decode(DynamicOps<S> dynamicOps, MapLike<S> mapLike) {
			for (MapDecoder<? extends T> mapDecoder : this.codecs) {
				DataResult<? extends T> dataResult = mapDecoder.decode(dynamicOps, mapLike);
				if (dataResult.result().isPresent()) {
					return (DataResult<T>)dataResult;
				}
			}

			return DataResult.error(() -> "No matching codec found");
		}

		@Override
		public <S> RecordBuilder<S> encode(T object, DynamicOps<S> dynamicOps, RecordBuilder<S> recordBuilder) {
			MapEncoder<T> mapEncoder = (MapEncoder<T>)this.encoderGetter.apply(object);
			return mapEncoder.encode(object, dynamicOps, recordBuilder);
		}

		@Override
		public <S> Stream<S> keys(DynamicOps<S> dynamicOps) {
			return this.codecs.stream().flatMap(mapCodec -> mapCodec.keys(dynamicOps)).distinct();
		}

		public String toString() {
			return "FuzzyCodec[" + this.codecs + "]";
		}
	}

	static class StrictEither<T> extends MapCodec<T> {
		private final String typeFieldName;
		private final MapCodec<T> typed;
		private final MapCodec<T> fuzzy;

		public StrictEither(String string, MapCodec<T> mapCodec, MapCodec<T> mapCodec2) {
			this.typeFieldName = string;
			this.typed = mapCodec;
			this.fuzzy = mapCodec2;
		}

		@Override
		public <O> DataResult<T> decode(DynamicOps<O> dynamicOps, MapLike<O> mapLike) {
			return mapLike.get(this.typeFieldName) != null ? this.typed.decode(dynamicOps, mapLike) : this.fuzzy.decode(dynamicOps, mapLike);
		}

		@Override
		public <O> RecordBuilder<O> encode(T object, DynamicOps<O> dynamicOps, RecordBuilder<O> recordBuilder) {
			return this.fuzzy.encode(object, dynamicOps, recordBuilder);
		}

		@Override
		public <T1> Stream<T1> keys(DynamicOps<T1> dynamicOps) {
			return Stream.concat(this.typed.keys(dynamicOps), this.fuzzy.keys(dynamicOps)).distinct();
		}
	}
}
