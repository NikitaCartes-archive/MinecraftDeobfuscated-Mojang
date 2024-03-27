package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import com.mojang.serialization.JsonOps;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.contents.DataSource;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.ChunkPos;

public interface Component extends Message, FormattedText {
	Style getStyle();

	ComponentContents getContents();

	@Override
	default String getString() {
		return FormattedText.super.getString();
	}

	default String getString(int i) {
		StringBuilder stringBuilder = new StringBuilder();
		this.visit(string -> {
			int j = i - stringBuilder.length();
			if (j <= 0) {
				return STOP_ITERATION;
			} else {
				stringBuilder.append(string.length() <= j ? string : string.substring(0, j));
				return Optional.empty();
			}
		});
		return stringBuilder.toString();
	}

	List<Component> getSiblings();

	@Nullable
	default String tryCollapseToString() {
		if (this.getContents() instanceof PlainTextContents plainTextContents && this.getSiblings().isEmpty() && this.getStyle().isEmpty()) {
			return plainTextContents.text();
		}

		return null;
	}

	default MutableComponent plainCopy() {
		return MutableComponent.create(this.getContents());
	}

	default MutableComponent copy() {
		return new MutableComponent(this.getContents(), new ArrayList(this.getSiblings()), this.getStyle());
	}

	FormattedCharSequence getVisualOrderText();

	@Override
	default <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
		Style style2 = this.getStyle().applyTo(style);
		Optional<T> optional = this.getContents().visit(styledContentConsumer, style2);
		if (optional.isPresent()) {
			return optional;
		} else {
			for (Component component : this.getSiblings()) {
				Optional<T> optional2 = component.visit(styledContentConsumer, style2);
				if (optional2.isPresent()) {
					return optional2;
				}
			}

			return Optional.empty();
		}
	}

	@Override
	default <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
		Optional<T> optional = this.getContents().visit(contentConsumer);
		if (optional.isPresent()) {
			return optional;
		} else {
			for (Component component : this.getSiblings()) {
				Optional<T> optional2 = component.visit(contentConsumer);
				if (optional2.isPresent()) {
					return optional2;
				}
			}

			return Optional.empty();
		}
	}

	default List<Component> toFlatList() {
		return this.toFlatList(Style.EMPTY);
	}

	default List<Component> toFlatList(Style style) {
		List<Component> list = Lists.<Component>newArrayList();
		this.visit((stylex, string) -> {
			if (!string.isEmpty()) {
				list.add(literal(string).withStyle(stylex));
			}

			return Optional.empty();
		}, style);
		return list;
	}

	default boolean contains(Component component) {
		if (this.equals(component)) {
			return true;
		} else {
			List<Component> list = this.toFlatList();
			List<Component> list2 = component.toFlatList(this.getStyle());
			return Collections.indexOfSubList(list, list2) != -1;
		}
	}

	static Component nullToEmpty(@Nullable String string) {
		return (Component)(string != null ? literal(string) : CommonComponents.EMPTY);
	}

	static MutableComponent literal(String string) {
		return MutableComponent.create(PlainTextContents.create(string));
	}

	static MutableComponent translatable(String string) {
		return MutableComponent.create(new TranslatableContents(string, null, TranslatableContents.NO_ARGS));
	}

	static MutableComponent translatable(String string, Object... objects) {
		return MutableComponent.create(new TranslatableContents(string, null, objects));
	}

	static MutableComponent translatableEscape(String string, Object... objects) {
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			if (!TranslatableContents.isAllowedPrimitiveArgument(object) && !(object instanceof Component)) {
				objects[i] = String.valueOf(object);
			}
		}

		return translatable(string, objects);
	}

	static MutableComponent translatableWithFallback(String string, @Nullable String string2) {
		return MutableComponent.create(new TranslatableContents(string, string2, TranslatableContents.NO_ARGS));
	}

	static MutableComponent translatableWithFallback(String string, @Nullable String string2, Object... objects) {
		return MutableComponent.create(new TranslatableContents(string, string2, objects));
	}

	static MutableComponent empty() {
		return MutableComponent.create(PlainTextContents.EMPTY);
	}

	static MutableComponent keybind(String string) {
		return MutableComponent.create(new KeybindContents(string));
	}

	static MutableComponent nbt(String string, boolean bl, Optional<Component> optional, DataSource dataSource) {
		return MutableComponent.create(new NbtContents(string, bl, optional, dataSource));
	}

	static MutableComponent score(String string, String string2) {
		return MutableComponent.create(new ScoreContents(string, string2));
	}

	static MutableComponent selector(String string, Optional<Component> optional) {
		return MutableComponent.create(new SelectorContents(string, optional));
	}

	static Component translationArg(Date date) {
		return literal(date.toString());
	}

	static Component translationArg(Message message) {
		return (Component)(message instanceof Component component ? component : literal(message.getString()));
	}

	static Component translationArg(UUID uUID) {
		return literal(uUID.toString());
	}

	static Component translationArg(ResourceLocation resourceLocation) {
		return literal(resourceLocation.toString());
	}

	static Component translationArg(ChunkPos chunkPos) {
		return literal(chunkPos.toString());
	}

	public static class Serializer {
		private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

		private Serializer() {
		}

		static MutableComponent deserialize(JsonElement jsonElement, HolderLookup.Provider provider) {
			return (MutableComponent)ComponentSerialization.CODEC
				.parse(provider.createSerializationContext(JsonOps.INSTANCE), jsonElement)
				.getOrThrow(JsonParseException::new);
		}

		static JsonElement serialize(Component component, HolderLookup.Provider provider) {
			return ComponentSerialization.CODEC.encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), component).getOrThrow(JsonParseException::new);
		}

		public static String toJson(Component component, HolderLookup.Provider provider) {
			return GSON.toJson(serialize(component, provider));
		}

		@Nullable
		public static MutableComponent fromJson(String string, HolderLookup.Provider provider) {
			JsonElement jsonElement = JsonParser.parseString(string);
			return jsonElement == null ? null : deserialize(jsonElement, provider);
		}

		@Nullable
		public static MutableComponent fromJson(@Nullable JsonElement jsonElement, HolderLookup.Provider provider) {
			return jsonElement == null ? null : deserialize(jsonElement, provider);
		}

		@Nullable
		public static MutableComponent fromJsonLenient(String string, HolderLookup.Provider provider) {
			JsonReader jsonReader = new JsonReader(new StringReader(string));
			jsonReader.setLenient(true);
			JsonElement jsonElement = JsonParser.parseReader(jsonReader);
			return jsonElement == null ? null : deserialize(jsonElement, provider);
		}
	}

	public static class SerializerAdapter implements JsonDeserializer<MutableComponent>, JsonSerializer<Component> {
		private final HolderLookup.Provider registries;

		public SerializerAdapter(HolderLookup.Provider provider) {
			this.registries = provider;
		}

		public MutableComponent deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			return Component.Serializer.deserialize(jsonElement, this.registries);
		}

		public JsonElement serialize(Component component, Type type, JsonSerializationContext jsonSerializationContext) {
			return Component.Serializer.serialize(component, this.registries);
		}
	}
}
