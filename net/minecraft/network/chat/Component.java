/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.NbtComponent;
import net.minecraft.network.chat.ScoreComponent;
import net.minecraft.network.chat.SelectorComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;
import org.jetbrains.annotations.Nullable;

public interface Component
extends Message,
FormattedText {
    public Style getStyle();

    public String getContents();

    @Override
    default public String getString() {
        return FormattedText.super.getString();
    }

    default public String getString(int i) {
        StringBuilder stringBuilder = new StringBuilder();
        this.visit(string -> {
            int j = i - stringBuilder.length();
            if (j <= 0) {
                return STOP_ITERATION;
            }
            stringBuilder.append(string.length() <= j ? string : string.substring(0, j));
            return Optional.empty();
        });
        return stringBuilder.toString();
    }

    public List<Component> getSiblings();

    public MutableComponent plainCopy();

    public MutableComponent copy();

    @Override
    @Environment(value=EnvType.CLIENT)
    default public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        Style style2 = this.getStyle().applyTo(style);
        Optional<T> optional = this.visitSelf(styledContentConsumer, style2);
        if (optional.isPresent()) {
            return optional;
        }
        for (Component component : this.getSiblings()) {
            Optional<T> optional2 = component.visit(styledContentConsumer, style2);
            if (!optional2.isPresent()) continue;
            return optional2;
        }
        return Optional.empty();
    }

    @Override
    default public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        Optional<T> optional = this.visitSelf(contentConsumer);
        if (optional.isPresent()) {
            return optional;
        }
        for (Component component : this.getSiblings()) {
            Optional<T> optional2 = component.visit(contentConsumer);
            if (!optional2.isPresent()) continue;
            return optional2;
        }
        return Optional.empty();
    }

    @Environment(value=EnvType.CLIENT)
    default public <T> Optional<T> visitSelf(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        return styledContentConsumer.accept(style, this.getContents());
    }

    default public <T> Optional<T> visitSelf(FormattedText.ContentConsumer<T> contentConsumer) {
        return contentConsumer.accept(this.getContents());
    }

    @Environment(value=EnvType.CLIENT)
    public static Component nullToEmpty(@Nullable String string) {
        return string != null ? new TextComponent(string) : TextComponent.EMPTY;
    }

    public static class Serializer
    implements JsonDeserializer<MutableComponent>,
    JsonSerializer<Component> {
        private static final Gson GSON = Util.make(() -> {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.disableHtmlEscaping();
            gsonBuilder.registerTypeHierarchyAdapter(Component.class, new Serializer());
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
            } catch (NoSuchFieldException noSuchFieldException) {
                throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", noSuchFieldException);
            }
        });
        private static final Field JSON_READER_LINESTART = Util.make(() -> {
            try {
                new JsonReader(new StringReader(""));
                Field field = JsonReader.class.getDeclaredField("lineStart");
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException noSuchFieldException) {
                throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", noSuchFieldException);
            }
        });

        /*
         * WARNING - void declaration
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public MutableComponent deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            void var5_19;
            if (jsonElement.isJsonPrimitive()) {
                return new TextComponent(jsonElement.getAsString());
            }
            if (jsonElement.isJsonObject()) {
                void var5_17;
                String string;
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject.has("text")) {
                    TextComponent textComponent = new TextComponent(GsonHelper.getAsString(jsonObject, "text"));
                } else if (jsonObject.has("translate")) {
                    string = GsonHelper.getAsString(jsonObject, "translate");
                    if (jsonObject.has("with")) {
                        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "with");
                        Object[] objects = new Object[jsonArray.size()];
                        for (int i = 0; i < objects.length; ++i) {
                            TextComponent textComponent;
                            objects[i] = this.deserialize(jsonArray.get(i), type, jsonDeserializationContext);
                            if (!(objects[i] instanceof TextComponent) || !(textComponent = (TextComponent)objects[i]).getStyle().isEmpty() || !textComponent.getSiblings().isEmpty()) continue;
                            objects[i] = textComponent.getText();
                        }
                        TranslatableComponent translatableComponent = new TranslatableComponent(string, objects);
                    } else {
                        TranslatableComponent translatableComponent = new TranslatableComponent(string);
                    }
                } else if (jsonObject.has("score")) {
                    JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "score");
                    if (!jsonObject2.has("name") || !jsonObject2.has("objective")) throw new JsonParseException("A score component needs a least a name and an objective");
                    ScoreComponent scoreComponent = new ScoreComponent(GsonHelper.getAsString(jsonObject2, "name"), GsonHelper.getAsString(jsonObject2, "objective"));
                } else if (jsonObject.has("selector")) {
                    SelectorComponent selectorComponent = new SelectorComponent(GsonHelper.getAsString(jsonObject, "selector"));
                } else if (jsonObject.has("keybind")) {
                    KeybindComponent keybindComponent = new KeybindComponent(GsonHelper.getAsString(jsonObject, "keybind"));
                } else {
                    if (!jsonObject.has("nbt")) throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
                    string = GsonHelper.getAsString(jsonObject, "nbt");
                    boolean bl = GsonHelper.getAsBoolean(jsonObject, "interpret", false);
                    if (jsonObject.has("block")) {
                        NbtComponent.BlockNbtComponent blockNbtComponent = new NbtComponent.BlockNbtComponent(string, bl, GsonHelper.getAsString(jsonObject, "block"));
                    } else if (jsonObject.has("entity")) {
                        NbtComponent.EntityNbtComponent entityNbtComponent = new NbtComponent.EntityNbtComponent(string, bl, GsonHelper.getAsString(jsonObject, "entity"));
                    } else {
                        if (!jsonObject.has("storage")) throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
                        NbtComponent.StorageNbtComponent storageNbtComponent = new NbtComponent.StorageNbtComponent(string, bl, new ResourceLocation(GsonHelper.getAsString(jsonObject, "storage")));
                    }
                }
                if (jsonObject.has("extra")) {
                    JsonArray jsonArray2 = GsonHelper.getAsJsonArray(jsonObject, "extra");
                    if (jsonArray2.size() <= 0) throw new JsonParseException("Unexpected empty array of components");
                    for (int j = 0; j < jsonArray2.size(); ++j) {
                        var5_17.append(this.deserialize(jsonArray2.get(j), type, jsonDeserializationContext));
                    }
                }
                var5_17.setStyle((Style)jsonDeserializationContext.deserialize(jsonElement, (Type)((Object)Style.class)));
                return var5_17;
            }
            if (!jsonElement.isJsonArray()) throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
            JsonArray jsonArray3 = jsonElement.getAsJsonArray();
            Object var5_18 = null;
            for (JsonElement jsonElement2 : jsonArray3) {
                MutableComponent mutableComponent2 = this.deserialize(jsonElement2, jsonElement2.getClass(), jsonDeserializationContext);
                if (var5_19 == null) {
                    MutableComponent mutableComponent = mutableComponent2;
                    continue;
                }
                var5_19.append(mutableComponent2);
            }
            return var5_19;
        }

        private void serializeStyle(Style style, JsonObject jsonObject, JsonSerializationContext jsonSerializationContext) {
            JsonElement jsonElement = jsonSerializationContext.serialize(style);
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject2 = (JsonObject)jsonElement;
                for (Map.Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
                    jsonObject.add(entry.getKey(), entry.getValue());
                }
            }
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public JsonElement serialize(Component component, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            if (!component.getStyle().isEmpty()) {
                this.serializeStyle(component.getStyle(), jsonObject, jsonSerializationContext);
            }
            if (!component.getSiblings().isEmpty()) {
                JsonArray jsonArray = new JsonArray();
                for (Component component2 : component.getSiblings()) {
                    jsonArray.add(this.serialize(component2, (Type)component2.getClass(), jsonSerializationContext));
                }
                jsonObject.add("extra", jsonArray);
            }
            if (component instanceof TextComponent) {
                jsonObject.addProperty("text", ((TextComponent)component).getText());
                return jsonObject;
            } else if (component instanceof TranslatableComponent) {
                TranslatableComponent translatableComponent = (TranslatableComponent)component;
                jsonObject.addProperty("translate", translatableComponent.getKey());
                if (translatableComponent.getArgs() == null || translatableComponent.getArgs().length <= 0) return jsonObject;
                JsonArray jsonArray2 = new JsonArray();
                for (Object object : translatableComponent.getArgs()) {
                    if (object instanceof Component) {
                        jsonArray2.add(this.serialize((Component)object, (Type)object.getClass(), jsonSerializationContext));
                        continue;
                    }
                    jsonArray2.add(new JsonPrimitive(String.valueOf(object)));
                }
                jsonObject.add("with", jsonArray2);
                return jsonObject;
            } else if (component instanceof ScoreComponent) {
                ScoreComponent scoreComponent = (ScoreComponent)component;
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.addProperty("name", scoreComponent.getName());
                jsonObject2.addProperty("objective", scoreComponent.getObjective());
                jsonObject.add("score", jsonObject2);
                return jsonObject;
            } else if (component instanceof SelectorComponent) {
                SelectorComponent selectorComponent = (SelectorComponent)component;
                jsonObject.addProperty("selector", selectorComponent.getPattern());
                return jsonObject;
            } else if (component instanceof KeybindComponent) {
                KeybindComponent keybindComponent = (KeybindComponent)component;
                jsonObject.addProperty("keybind", keybindComponent.getName());
                return jsonObject;
            } else {
                if (!(component instanceof NbtComponent)) throw new IllegalArgumentException("Don't know how to serialize " + component + " as a Component");
                NbtComponent nbtComponent = (NbtComponent)component;
                jsonObject.addProperty("nbt", nbtComponent.getNbtPath());
                jsonObject.addProperty("interpret", nbtComponent.isInterpreting());
                if (component instanceof NbtComponent.BlockNbtComponent) {
                    NbtComponent.BlockNbtComponent blockNbtComponent = (NbtComponent.BlockNbtComponent)component;
                    jsonObject.addProperty("block", blockNbtComponent.getPos());
                    return jsonObject;
                } else if (component instanceof NbtComponent.EntityNbtComponent) {
                    NbtComponent.EntityNbtComponent entityNbtComponent = (NbtComponent.EntityNbtComponent)component;
                    jsonObject.addProperty("entity", entityNbtComponent.getSelector());
                    return jsonObject;
                } else {
                    if (!(component instanceof NbtComponent.StorageNbtComponent)) throw new IllegalArgumentException("Don't know how to serialize " + component + " as a Component");
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
                MutableComponent mutableComponent = GSON.getAdapter(MutableComponent.class).read(jsonReader);
                stringReader.setCursor(stringReader.getCursor() + Serializer.getPos(jsonReader));
                return mutableComponent;
            } catch (IOException | StackOverflowError throwable) {
                throw new JsonParseException(throwable);
            }
        }

        private static int getPos(JsonReader jsonReader) {
            try {
                return JSON_READER_POS.getInt(jsonReader) - JSON_READER_LINESTART.getInt(jsonReader) + 1;
            } catch (IllegalAccessException illegalAccessException) {
                throw new IllegalStateException("Couldn't read position of JsonReader", illegalAccessException);
            }
        }

        @Override
        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((Component)object, type, jsonSerializationContext);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

