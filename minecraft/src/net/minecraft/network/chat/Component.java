package net.minecraft.network.chat;

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
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;
import net.minecraft.util.Unit;

public interface Component extends Message {
	Optional<Unit> STOP_ITERATION = Optional.of(Unit.INSTANCE);

	Style getStyle();

	String getContents();

	@Override
	default String getString() {
		StringBuilder stringBuilder = new StringBuilder();
		this.visit(string -> {
			stringBuilder.append(string);
			return Optional.empty();
		});
		return stringBuilder.toString();
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

	MutableComponent toMutable();

	MutableComponent mutableCopy();

	@Environment(EnvType.CLIENT)
	default <T> Optional<T> visit(Component.StyledContentConsumer<T> styledContentConsumer, Style style) {
		Style style2 = this.getStyle().applyTo(style);
		Optional<T> optional = this.visitSelf(styledContentConsumer, style2);
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

	default <T> Optional<T> visit(Component.ContentConsumer<T> contentConsumer) {
		Optional<T> optional = this.visitSelf(contentConsumer);
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

	@Environment(EnvType.CLIENT)
	default <T> Optional<T> visitSelf(Component.StyledContentConsumer<T> styledContentConsumer, Style style) {
		return styledContentConsumer.accept(style, this.getContents());
	}

	default <T> Optional<T> visitSelf(Component.ContentConsumer<T> contentConsumer) {
		return contentConsumer.accept(this.getContents());
	}

	public interface ContentConsumer<T> {
		Optional<T> accept(String string);
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
				return new TextComponent(jsonElement.getAsString());
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
					mutableComponent = new TextComponent(GsonHelper.getAsString(jsonObject, "text"));
				} else if (jsonObject.has("translate")) {
					String string = GsonHelper.getAsString(jsonObject, "translate");
					if (jsonObject.has("with")) {
						JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "with");
						Object[] objects = new Object[jsonArray.size()];

						for (int i = 0; i < objects.length; i++) {
							objects[i] = this.deserialize(jsonArray.get(i), type, jsonDeserializationContext);
							if (objects[i] instanceof TextComponent) {
								TextComponent textComponent = (TextComponent)objects[i];
								if (textComponent.getStyle().isEmpty() && textComponent.getSiblings().isEmpty()) {
									objects[i] = textComponent.getText();
								}
							}
						}

						mutableComponent = new TranslatableComponent(string, objects);
					} else {
						mutableComponent = new TranslatableComponent(string);
					}
				} else if (jsonObject.has("score")) {
					JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "score");
					if (!jsonObject2.has("name") || !jsonObject2.has("objective")) {
						throw new JsonParseException("A score component needs a least a name and an objective");
					}

					mutableComponent = new ScoreComponent(GsonHelper.getAsString(jsonObject2, "name"), GsonHelper.getAsString(jsonObject2, "objective"));
				} else if (jsonObject.has("selector")) {
					mutableComponent = new SelectorComponent(GsonHelper.getAsString(jsonObject, "selector"));
				} else if (jsonObject.has("keybind")) {
					mutableComponent = new KeybindComponent(GsonHelper.getAsString(jsonObject, "keybind"));
				} else {
					if (!jsonObject.has("nbt")) {
						throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
					}

					String string = GsonHelper.getAsString(jsonObject, "nbt");
					boolean bl = GsonHelper.getAsBoolean(jsonObject, "interpret", false);
					if (jsonObject.has("block")) {
						mutableComponent = new NbtComponent.BlockNbtComponent(string, bl, GsonHelper.getAsString(jsonObject, "block"));
					} else if (jsonObject.has("entity")) {
						mutableComponent = new NbtComponent.EntityNbtComponent(string, bl, GsonHelper.getAsString(jsonObject, "entity"));
					} else {
						if (!jsonObject.has("storage")) {
							throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
						}

						mutableComponent = new NbtComponent.StorageNbtComponent(string, bl, new ResourceLocation(GsonHelper.getAsString(jsonObject, "storage")));
					}
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
					jsonArray.add(this.serialize(component2, component2.getClass(), jsonSerializationContext));
				}

				jsonObject.add("extra", jsonArray);
			}

			if (component instanceof TextComponent) {
				jsonObject.addProperty("text", ((TextComponent)component).getText());
			} else if (component instanceof TranslatableComponent) {
				TranslatableComponent translatableComponent = (TranslatableComponent)component;
				jsonObject.addProperty("translate", translatableComponent.getKey());
				if (translatableComponent.getArgs() != null && translatableComponent.getArgs().length > 0) {
					JsonArray jsonArray2 = new JsonArray();

					for (Object object : translatableComponent.getArgs()) {
						if (object instanceof Component) {
							jsonArray2.add(this.serialize((Component)object, object.getClass(), jsonSerializationContext));
						} else {
							jsonArray2.add(new JsonPrimitive(String.valueOf(object)));
						}
					}

					jsonObject.add("with", jsonArray2);
				}
			} else if (component instanceof ScoreComponent) {
				ScoreComponent scoreComponent = (ScoreComponent)component;
				JsonObject jsonObject2 = new JsonObject();
				jsonObject2.addProperty("name", scoreComponent.getName());
				jsonObject2.addProperty("objective", scoreComponent.getObjective());
				jsonObject.add("score", jsonObject2);
			} else if (component instanceof SelectorComponent) {
				SelectorComponent selectorComponent = (SelectorComponent)component;
				jsonObject.addProperty("selector", selectorComponent.getPattern());
			} else if (component instanceof KeybindComponent) {
				KeybindComponent keybindComponent = (KeybindComponent)component;
				jsonObject.addProperty("keybind", keybindComponent.getName());
			} else {
				if (!(component instanceof NbtComponent)) {
					throw new IllegalArgumentException("Don't know how to serialize " + component + " as a Component");
				}

				NbtComponent nbtComponent = (NbtComponent)component;
				jsonObject.addProperty("nbt", nbtComponent.getNbtPath());
				jsonObject.addProperty("interpret", nbtComponent.isInterpreting());
				if (component instanceof NbtComponent.BlockNbtComponent) {
					NbtComponent.BlockNbtComponent blockNbtComponent = (NbtComponent.BlockNbtComponent)component;
					jsonObject.addProperty("block", blockNbtComponent.getPos());
				} else if (component instanceof NbtComponent.EntityNbtComponent) {
					NbtComponent.EntityNbtComponent entityNbtComponent = (NbtComponent.EntityNbtComponent)component;
					jsonObject.addProperty("entity", entityNbtComponent.getSelector());
				} else {
					if (!(component instanceof NbtComponent.StorageNbtComponent)) {
						throw new IllegalArgumentException("Don't know how to serialize " + component + " as a Component");
					}

					NbtComponent.StorageNbtComponent storageNbtComponent = (NbtComponent.StorageNbtComponent)component;
					jsonObject.addProperty("storage", storageNbtComponent.getId().toString());
				}
			}

			return jsonObject;
		}

		public static String toJson(Component component) {
			return GSON.toJson(component);
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

	@Environment(EnvType.CLIENT)
	public interface StyledContentConsumer<T> {
		Optional<T> accept(Style style, String string);
	}
}
