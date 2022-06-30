package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.chat.contents.BlockDataSource;
import net.minecraft.network.chat.contents.DataSource;
import net.minecraft.network.chat.contents.EntityDataSource;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.StorageDataSource;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;

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
			List<Component> list2 = component.toFlatList();
			return Collections.indexOfSubList(list, list2) != -1;
		}
	}

	static Component nullToEmpty(@Nullable String string) {
		return (Component)(string != null ? literal(string) : CommonComponents.EMPTY);
	}

	static MutableComponent literal(String string) {
		return MutableComponent.create(new LiteralContents(string));
	}

	static MutableComponent translatable(String string) {
		return MutableComponent.create(new TranslatableContents(string));
	}

	static MutableComponent translatable(String string, Object... objects) {
		return MutableComponent.create(new TranslatableContents(string, objects));
	}

	static MutableComponent empty() {
		return MutableComponent.create(ComponentContents.EMPTY);
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

	public static class Serializer implements JsonDeserializer<MutableComponent>, JsonSerializer<Component> {
		private static final Gson GSON = Util.make(() -> {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.disableHtmlEscaping();
			gsonBuilder.registerTypeHierarchyAdapter(Component.class, new Component.Serializer());
			gsonBuilder.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
			gsonBuilder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
			return gsonBuilder.create();
		});
		private static final Field JSON_READER_POS = Util.make(() -> {
			try {
				new JsonReader(new StringReader(""));
				Field field = JsonReader.class.getDeclaredField("pos");
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException var1) {
				throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", var1);
			}
		});
		private static final Field JSON_READER_LINESTART = Util.make(() -> {
			try {
				new JsonReader(new StringReader(""));
				Field field = JsonReader.class.getDeclaredField("lineStart");
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException var1) {
				throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", var1);
			}
		});

		public MutableComponent deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			if (jsonElement.isJsonPrimitive()) {
				return Component.literal(jsonElement.getAsString());
			} else if (!jsonElement.isJsonObject()) {
				if (jsonElement.isJsonArray()) {
					JsonArray jsonArray3 = jsonElement.getAsJsonArray();
					MutableComponent mutableComponent = null;

					for (JsonElement jsonElement2 : jsonArray3) {
						MutableComponent mutableComponent2 = this.deserialize(jsonElement2, jsonElement2.getClass(), jsonDeserializationContext);
						if (mutableComponent == null) {
							mutableComponent = mutableComponent2;
						} else {
							mutableComponent.append(mutableComponent2);
						}
					}

					return mutableComponent;
				} else {
					throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
				}
			} else {
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				MutableComponent mutableComponent;
				if (jsonObject.has("text")) {
					String string = GsonHelper.getAsString(jsonObject, "text");
					mutableComponent = string.isEmpty() ? Component.empty() : Component.literal(string);
				} else if (jsonObject.has("translate")) {
					String string = GsonHelper.getAsString(jsonObject, "translate");
					if (jsonObject.has("with")) {
						JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "with");
						Object[] objects = new Object[jsonArray.size()];

						for (int i = 0; i < objects.length; i++) {
							objects[i] = unwrapTextArgument(this.deserialize(jsonArray.get(i), type, jsonDeserializationContext));
						}

						mutableComponent = Component.translatable(string, objects);
					} else {
						mutableComponent = Component.translatable(string);
					}
				} else if (jsonObject.has("score")) {
					JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "score");
					if (!jsonObject2.has("name") || !jsonObject2.has("objective")) {
						throw new JsonParseException("A score component needs a least a name and an objective");
					}

					mutableComponent = Component.score(GsonHelper.getAsString(jsonObject2, "name"), GsonHelper.getAsString(jsonObject2, "objective"));
				} else if (jsonObject.has("selector")) {
					Optional<Component> optional = this.parseSeparator(type, jsonDeserializationContext, jsonObject);
					mutableComponent = Component.selector(GsonHelper.getAsString(jsonObject, "selector"), optional);
				} else if (jsonObject.has("keybind")) {
					mutableComponent = Component.keybind(GsonHelper.getAsString(jsonObject, "keybind"));
				} else {
					if (!jsonObject.has("nbt")) {
						throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
					}

					String string = GsonHelper.getAsString(jsonObject, "nbt");
					Optional<Component> optional2 = this.parseSeparator(type, jsonDeserializationContext, jsonObject);
					boolean bl = GsonHelper.getAsBoolean(jsonObject, "interpret", false);
					DataSource dataSource;
					if (jsonObject.has("block")) {
						dataSource = new BlockDataSource(GsonHelper.getAsString(jsonObject, "block"));
					} else if (jsonObject.has("entity")) {
						dataSource = new EntityDataSource(GsonHelper.getAsString(jsonObject, "entity"));
					} else {
						if (!jsonObject.has("storage")) {
							throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
						}

						dataSource = new StorageDataSource(new ResourceLocation(GsonHelper.getAsString(jsonObject, "storage")));
					}

					mutableComponent = Component.nbt(string, bl, optional2, dataSource);
				}

				if (jsonObject.has("extra")) {
					JsonArray jsonArray2 = GsonHelper.getAsJsonArray(jsonObject, "extra");
					if (jsonArray2.size() <= 0) {
						throw new JsonParseException("Unexpected empty array of components");
					}

					for (int j = 0; j < jsonArray2.size(); j++) {
						mutableComponent.append(this.deserialize(jsonArray2.get(j), type, jsonDeserializationContext));
					}
				}

				mutableComponent.setStyle(jsonDeserializationContext.deserialize(jsonElement, Style.class));
				return mutableComponent;
			}
		}

		private static Object unwrapTextArgument(Object object) {
			if (object instanceof Component component
				&& component.getStyle().isEmpty()
				&& component.getSiblings().isEmpty()
				&& component.getContents() instanceof LiteralContents literalContents) {
				return literalContents.text();
			}

			return object;
		}

		private Optional<Component> parseSeparator(Type type, JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
			return jsonObject.has("separator") ? Optional.of(this.deserialize(jsonObject.get("separator"), type, jsonDeserializationContext)) : Optional.empty();
		}

		private void serializeStyle(Style style, JsonObject jsonObject, JsonSerializationContext jsonSerializationContext) {
			JsonElement jsonElement = jsonSerializationContext.serialize(style);
			if (jsonElement.isJsonObject()) {
				JsonObject jsonObject2 = (JsonObject)jsonElement;

				for (Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
					jsonObject.add((String)entry.getKey(), (JsonElement)entry.getValue());
				}
			}
		}

		public JsonElement serialize(Component component, Type type, JsonSerializationContext jsonSerializationContext) {
			JsonObject jsonObject = new JsonObject();
			if (!component.getStyle().isEmpty()) {
				this.serializeStyle(component.getStyle(), jsonObject, jsonSerializationContext);
			}

			if (!component.getSiblings().isEmpty()) {
				JsonArray jsonArray = new JsonArray();

				for (Component component2 : component.getSiblings()) {
					jsonArray.add(this.serialize(component2, Component.class, jsonSerializationContext));
				}

				jsonObject.add("extra", jsonArray);
			}

			ComponentContents componentContents = component.getContents();
			if (componentContents == ComponentContents.EMPTY) {
				jsonObject.addProperty("text", "");
			} else if (componentContents instanceof LiteralContents literalContents) {
				jsonObject.addProperty("text", literalContents.text());
			} else if (componentContents instanceof TranslatableContents translatableContents) {
				jsonObject.addProperty("translate", translatableContents.getKey());
				if (translatableContents.getArgs().length > 0) {
					JsonArray jsonArray2 = new JsonArray();

					for (Object object : translatableContents.getArgs()) {
						if (object instanceof Component) {
							jsonArray2.add(this.serialize((Component)object, object.getClass(), jsonSerializationContext));
						} else {
							jsonArray2.add(new JsonPrimitive(String.valueOf(object)));
						}
					}

					jsonObject.add("with", jsonArray2);
				}
			} else if (componentContents instanceof ScoreContents scoreContents) {
				JsonObject jsonObject2 = new JsonObject();
				jsonObject2.addProperty("name", scoreContents.getName());
				jsonObject2.addProperty("objective", scoreContents.getObjective());
				jsonObject.add("score", jsonObject2);
			} else if (componentContents instanceof SelectorContents selectorContents) {
				jsonObject.addProperty("selector", selectorContents.getPattern());
				this.serializeSeparator(jsonSerializationContext, jsonObject, selectorContents.getSeparator());
			} else if (componentContents instanceof KeybindContents keybindContents) {
				jsonObject.addProperty("keybind", keybindContents.getName());
			} else {
				if (!(componentContents instanceof NbtContents nbtContents)) {
					throw new IllegalArgumentException("Don't know how to serialize " + componentContents + " as a Component");
				}

				jsonObject.addProperty("nbt", nbtContents.getNbtPath());
				jsonObject.addProperty("interpret", nbtContents.isInterpreting());
				this.serializeSeparator(jsonSerializationContext, jsonObject, nbtContents.getSeparator());
				DataSource dataSource = nbtContents.getDataSource();
				if (dataSource instanceof BlockDataSource blockDataSource) {
					jsonObject.addProperty("block", blockDataSource.posPattern());
				} else if (dataSource instanceof EntityDataSource entityDataSource) {
					jsonObject.addProperty("entity", entityDataSource.selectorPattern());
				} else {
					if (!(dataSource instanceof StorageDataSource storageDataSource)) {
						throw new IllegalArgumentException("Don't know how to serialize " + componentContents + " as a Component");
					}

					jsonObject.addProperty("storage", storageDataSource.id().toString());
				}
			}

			return jsonObject;
		}

		private void serializeSeparator(JsonSerializationContext jsonSerializationContext, JsonObject jsonObject, Optional<Component> optional) {
			optional.ifPresent(component -> jsonObject.add("separator", this.serialize(component, component.getClass(), jsonSerializationContext)));
		}

		public static String toJson(Component component) {
			return GSON.toJson(component);
		}

		public static String toStableJson(Component component) {
			return GsonHelper.toStableString(toJsonTree(component));
		}

		public static JsonElement toJsonTree(Component component) {
			return GSON.toJsonTree(component);
		}

		@Nullable
		public static MutableComponent fromJson(String string) {
			return GsonHelper.fromJson(GSON, string, MutableComponent.class, false);
		}

		@Nullable
		public static MutableComponent fromJson(JsonElement jsonElement) {
			return GSON.fromJson(jsonElement, MutableComponent.class);
		}

		@Nullable
		public static MutableComponent fromJsonLenient(String string) {
			return GsonHelper.fromJson(GSON, string, MutableComponent.class, true);
		}

		public static MutableComponent fromJson(com.mojang.brigadier.StringReader stringReader) {
			try {
				JsonReader jsonReader = new JsonReader(new StringReader(stringReader.getRemaining()));
				jsonReader.setLenient(false);
				MutableComponent mutableComponent = GSON.<MutableComponent>getAdapter(MutableComponent.class).read(jsonReader);
				stringReader.setCursor(stringReader.getCursor() + getPos(jsonReader));
				return mutableComponent;
			} catch (StackOverflowError | IOException var3) {
				throw new JsonParseException(var3);
			}
		}

		private static int getPos(JsonReader jsonReader) {
			try {
				return JSON_READER_POS.getInt(jsonReader) - JSON_READER_LINESTART.getInt(jsonReader) + 1;
			} catch (IllegalAccessException var2) {
				throw new IllegalStateException("Couldn't read position of JsonReader", var2);
			}
		}
	}
}
