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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;

public interface Component extends Message, Iterable<Component> {
	Component setStyle(Style style);

	Style getStyle();

	default Component append(String string) {
		return this.append(new TextComponent(string));
	}

	Component append(Component component);

	String getContents();

	@Override
	default String getString() {
		StringBuilder stringBuilder = new StringBuilder();
		this.stream().forEach(component -> stringBuilder.append(component.getContents()));
		return stringBuilder.toString();
	}

	default String getString(int i) {
		StringBuilder stringBuilder = new StringBuilder();
		Iterator<Component> iterator = this.stream().iterator();

		while (iterator.hasNext()) {
			int j = i - stringBuilder.length();
			if (j <= 0) {
				break;
			}

			String string = ((Component)iterator.next()).getContents();
			stringBuilder.append(string.length() <= j ? string : string.substring(0, j));
		}

		return stringBuilder.toString();
	}

	default String getColoredString() {
		StringBuilder stringBuilder = new StringBuilder();
		String string = "";
		Iterator<Component> iterator = this.stream().iterator();

		while (iterator.hasNext()) {
			Component component = (Component)iterator.next();
			String string2 = component.getContents();
			if (!string2.isEmpty()) {
				String string3 = component.getStyle().getLegacyFormatCodes();
				if (!string3.equals(string)) {
					if (!string.isEmpty()) {
						stringBuilder.append(ChatFormatting.RESET);
					}

					stringBuilder.append(string3);
					string = string3;
				}

				stringBuilder.append(string2);
			}
		}

		if (!string.isEmpty()) {
			stringBuilder.append(ChatFormatting.RESET);
		}

		return stringBuilder.toString();
	}

	List<Component> getSiblings();

	Stream<Component> stream();

	default Stream<Component> flatStream() {
		return this.stream().map(Component::flattenStyle);
	}

	default Iterator<Component> iterator() {
		return this.flatStream().iterator();
	}

	Component copy();

	default Component deepCopy() {
		Component component = this.copy();
		component.setStyle(this.getStyle().copy());

		for (Component component2 : this.getSiblings()) {
			component.append(component2.deepCopy());
		}

		return component;
	}

	default Component withStyle(Consumer<Style> consumer) {
		consumer.accept(this.getStyle());
		return this;
	}

	default Component withStyle(ChatFormatting... chatFormattings) {
		for (ChatFormatting chatFormatting : chatFormattings) {
			this.withStyle(chatFormatting);
		}

		return this;
	}

	default Component withStyle(ChatFormatting chatFormatting) {
		Style style = this.getStyle();
		if (chatFormatting.isColor()) {
			style.setColor(chatFormatting);
		}

		if (chatFormatting.isFormat()) {
			switch (chatFormatting) {
				case OBFUSCATED:
					style.setObfuscated(true);
					break;
				case BOLD:
					style.setBold(true);
					break;
				case STRIKETHROUGH:
					style.setStrikethrough(true);
					break;
				case UNDERLINE:
					style.setUnderlined(true);
					break;
				case ITALIC:
					style.setItalic(true);
			}
		}

		return this;
	}

	static Component flattenStyle(Component component) {
		Component component2 = component.copy();
		component2.setStyle(component.getStyle().flatCopy());
		return component2;
	}

	public static class Serializer implements JsonDeserializer<Component>, JsonSerializer<Component> {
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

		public Component deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			if (jsonElement.isJsonPrimitive()) {
				return new TextComponent(jsonElement.getAsString());
			} else if (!jsonElement.isJsonObject()) {
				if (jsonElement.isJsonArray()) {
					JsonArray jsonArray3 = jsonElement.getAsJsonArray();
					Component component = null;

					for (JsonElement jsonElement2 : jsonArray3) {
						Component component2 = this.deserialize(jsonElement2, jsonElement2.getClass(), jsonDeserializationContext);
						if (component == null) {
							component = component2;
						} else {
							component.append(component2);
						}
					}

					return component;
				} else {
					throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
				}
			} else {
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				Component component;
				if (jsonObject.has("text")) {
					component = new TextComponent(GsonHelper.getAsString(jsonObject, "text"));
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

						component = new TranslatableComponent(string, objects);
					} else {
						component = new TranslatableComponent(string);
					}
				} else if (jsonObject.has("score")) {
					JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "score");
					if (!jsonObject2.has("name") || !jsonObject2.has("objective")) {
						throw new JsonParseException("A score component needs a least a name and an objective");
					}

					component = new ScoreComponent(GsonHelper.getAsString(jsonObject2, "name"), GsonHelper.getAsString(jsonObject2, "objective"));
					if (jsonObject2.has("value")) {
						((ScoreComponent)component).setValue(GsonHelper.getAsString(jsonObject2, "value"));
					}
				} else if (jsonObject.has("selector")) {
					component = new SelectorComponent(GsonHelper.getAsString(jsonObject, "selector"));
				} else if (jsonObject.has("keybind")) {
					component = new KeybindComponent(GsonHelper.getAsString(jsonObject, "keybind"));
				} else {
					if (!jsonObject.has("nbt")) {
						throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
					}

					String string = GsonHelper.getAsString(jsonObject, "nbt");
					boolean bl = GsonHelper.getAsBoolean(jsonObject, "interpret", false);
					if (jsonObject.has("block")) {
						component = new NbtComponent.BlockNbtComponent(string, bl, GsonHelper.getAsString(jsonObject, "block"));
					} else if (jsonObject.has("entity")) {
						component = new NbtComponent.EntityNbtComponent(string, bl, GsonHelper.getAsString(jsonObject, "entity"));
					} else {
						if (!jsonObject.has("storage")) {
							throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
						}

						component = new NbtComponent.StorageNbtComponent(string, bl, new ResourceLocation(GsonHelper.getAsString(jsonObject, "storage")));
					}
				}

				if (jsonObject.has("extra")) {
					JsonArray jsonArray2 = GsonHelper.getAsJsonArray(jsonObject, "extra");
					if (jsonArray2.size() <= 0) {
						throw new JsonParseException("Unexpected empty array of components");
					}

					for (int j = 0; j < jsonArray2.size(); j++) {
						component.append(this.deserialize(jsonArray2.get(j), type, jsonDeserializationContext));
					}
				}

				component.setStyle(jsonDeserializationContext.deserialize(jsonElement, Style.class));
				return component;
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
				jsonObject2.addProperty("value", scoreComponent.getContents());
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
				} else {
					if (!(component instanceof NbtComponent.EntityNbtComponent)) {
						throw new IllegalArgumentException("Don't know how to serialize " + component + " as a Component");
					}

					NbtComponent.EntityNbtComponent entityNbtComponent = (NbtComponent.EntityNbtComponent)component;
					jsonObject.addProperty("entity", entityNbtComponent.getSelector());
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
		public static Component fromJson(String string) {
			return GsonHelper.fromJson(GSON, string, Component.class, false);
		}

		@Nullable
		public static Component fromJson(JsonElement jsonElement) {
			return GSON.fromJson(jsonElement, Component.class);
		}

		@Nullable
		public static Component fromJsonLenient(String string) {
			return GsonHelper.fromJson(GSON, string, Component.class, true);
		}

		public static Component fromJson(com.mojang.brigadier.StringReader stringReader) {
			try {
				JsonReader jsonReader = new JsonReader(new StringReader(stringReader.getRemaining()));
				jsonReader.setLenient(false);
				Component component = GSON.<Component>getAdapter(Component.class).read(jsonReader);
				stringReader.setCursor(stringReader.getCursor() + getPos(jsonReader));
				return component;
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
