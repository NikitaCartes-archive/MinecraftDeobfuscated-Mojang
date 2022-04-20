/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
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
import org.jetbrains.annotations.Nullable;

public interface Component
extends Message,
FormattedText {
    public Style getStyle();

    public ComponentContents getContents();

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

    default public MutableComponent plainCopy() {
        return MutableComponent.create(this.getContents());
    }

    default public MutableComponent copy() {
        return new MutableComponent(this.getContents(), new ArrayList<Component>(this.getSiblings()), this.getStyle());
    }

    public FormattedCharSequence getVisualOrderText();

    @Override
    default public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        Style style2 = this.getStyle().applyTo(style);
        Optional<T> optional = this.getContents().visit(styledContentConsumer, style2);
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
        Optional<T> optional = this.getContents().visit(contentConsumer);
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

    default public List<Component> toFlatList(Style style2) {
        ArrayList<Component> list = Lists.newArrayList();
        this.visit((style, string) -> {
            if (!string.isEmpty()) {
                list.add(Component.literal(string).withStyle(style));
            }
            return Optional.empty();
        }, style2);
        return list;
    }

    public static Component nullToEmpty(@Nullable String string) {
        return string != null ? Component.literal(string) : CommonComponents.EMPTY;
    }

    public static MutableComponent literal(String string) {
        return MutableComponent.create(new LiteralContents(string));
    }

    public static MutableComponent translatable(String string) {
        return MutableComponent.create(new TranslatableContents(string));
    }

    public static MutableComponent translatable(String string, Object ... objects) {
        return MutableComponent.create(new TranslatableContents(string, objects));
    }

    public static MutableComponent empty() {
        return MutableComponent.create(ComponentContents.EMPTY);
    }

    public static MutableComponent keybind(String string) {
        return MutableComponent.create(new KeybindContents(string));
    }

    public static MutableComponent nbt(String string, boolean bl, Optional<Component> optional, DataSource dataSource) {
        return MutableComponent.create(new NbtContents(string, bl, optional, dataSource));
    }

    public static MutableComponent score(String string, String string2) {
        return MutableComponent.create(new ScoreContents(string, string2));
    }

    public static MutableComponent selector(String string, Optional<Component> optional) {
        return MutableComponent.create(new SelectorContents(string, optional));
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
            if (jsonElement.isJsonPrimitive()) {
                return Component.literal(jsonElement.getAsString());
            }
            if (jsonElement.isJsonObject()) {
                MutableComponent mutableComponent;
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject.has("text")) {
                    string = GsonHelper.getAsString(jsonObject, "text");
                    mutableComponent = string.isEmpty() ? Component.empty() : Component.literal(string);
                } else if (jsonObject.has("translate")) {
                    string = GsonHelper.getAsString(jsonObject, "translate");
                    if (jsonObject.has("with")) {
                        void var9_17;
                        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "with");
                        Object[] objects = new Object[jsonArray.size()];
                        boolean bl = false;
                        while (var9_17 < objects.length) {
                            objects[var9_17] = Serializer.unwrapTextArgument(this.deserialize(jsonArray.get((int)var9_17), type, jsonDeserializationContext));
                            ++var9_17;
                        }
                        mutableComponent = Component.translatable(string, objects);
                    } else {
                        mutableComponent = Component.translatable(string);
                    }
                } else if (jsonObject.has("score")) {
                    JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "score");
                    if (!jsonObject2.has("name") || !jsonObject2.has("objective")) throw new JsonParseException("A score component needs a least a name and an objective");
                    mutableComponent = Component.score(GsonHelper.getAsString(jsonObject2, "name"), GsonHelper.getAsString(jsonObject2, "objective"));
                } else if (jsonObject.has("selector")) {
                    Optional<Component> optional = this.parseSeparator(type, jsonDeserializationContext, jsonObject);
                    mutableComponent = Component.selector(GsonHelper.getAsString(jsonObject, "selector"), optional);
                } else if (jsonObject.has("keybind")) {
                    mutableComponent = Component.keybind(GsonHelper.getAsString(jsonObject, "keybind"));
                } else {
                    void var9_21;
                    if (!jsonObject.has("nbt")) throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
                    string = GsonHelper.getAsString(jsonObject, "nbt");
                    Optional<Component> optional2 = this.parseSeparator(type, jsonDeserializationContext, jsonObject);
                    boolean bl = GsonHelper.getAsBoolean(jsonObject, "interpret", false);
                    if (jsonObject.has("block")) {
                        BlockDataSource blockDataSource = new BlockDataSource(GsonHelper.getAsString(jsonObject, "block"));
                    } else if (jsonObject.has("entity")) {
                        EntityDataSource entityDataSource = new EntityDataSource(GsonHelper.getAsString(jsonObject, "entity"));
                    } else {
                        if (!jsonObject.has("storage")) throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
                        StorageDataSource storageDataSource = new StorageDataSource(new ResourceLocation(GsonHelper.getAsString(jsonObject, "storage")));
                    }
                    mutableComponent = Component.nbt(string, bl, optional2, (DataSource)var9_21);
                }
                if (jsonObject.has("extra")) {
                    JsonArray jsonArray2 = GsonHelper.getAsJsonArray(jsonObject, "extra");
                    if (jsonArray2.size() <= 0) throw new JsonParseException("Unexpected empty array of components");
                    for (int j = 0; j < jsonArray2.size(); ++j) {
                        mutableComponent.append(this.deserialize(jsonArray2.get(j), type, jsonDeserializationContext));
                    }
                }
                mutableComponent.setStyle((Style)jsonDeserializationContext.deserialize(jsonElement, (Type)((Object)Style.class)));
                return mutableComponent;
            }
            if (!jsonElement.isJsonArray()) throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
            JsonArray jsonArray3 = jsonElement.getAsJsonArray();
            MutableComponent mutableComponent = null;
            for (JsonElement jsonElement2 : jsonArray3) {
                MutableComponent mutableComponent2 = this.deserialize(jsonElement2, jsonElement2.getClass(), jsonDeserializationContext);
                if (mutableComponent == null) {
                    mutableComponent = mutableComponent2;
                    continue;
                }
                mutableComponent.append(mutableComponent2);
            }
            return mutableComponent;
        }

        private static Object unwrapTextArgument(Object object) {
            ComponentContents componentContents;
            Component component;
            if (object instanceof Component && (component = (Component)object).getStyle().isEmpty() && component.getSiblings().isEmpty() && (componentContents = component.getContents()) instanceof LiteralContents) {
                LiteralContents literalContents = (LiteralContents)componentContents;
                return literalContents.text();
            }
            return object;
        }

        private Optional<Component> parseSeparator(Type type, JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
            if (jsonObject.has("separator")) {
                return Optional.of(this.deserialize(jsonObject.get("separator"), type, jsonDeserializationContext));
            }
            return Optional.empty();
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
            ComponentContents componentContents;
            JsonObject jsonObject = new JsonObject();
            if (!component.getStyle().isEmpty()) {
                this.serializeStyle(component.getStyle(), jsonObject, jsonSerializationContext);
            }
            if (!component.getSiblings().isEmpty()) {
                JsonArray jsonArray = new JsonArray();
                for (Component component2 : component.getSiblings()) {
                    jsonArray.add(this.serialize(component2, (Type)((Object)Component.class), jsonSerializationContext));
                }
                jsonObject.add("extra", jsonArray);
            }
            if ((componentContents = component.getContents()) == ComponentContents.EMPTY) {
                jsonObject.addProperty("text", "");
                return jsonObject;
            } else if (componentContents instanceof LiteralContents) {
                LiteralContents literalContents = (LiteralContents)componentContents;
                jsonObject.addProperty("text", literalContents.text());
                return jsonObject;
            } else if (componentContents instanceof TranslatableContents) {
                TranslatableContents translatableContents = (TranslatableContents)componentContents;
                jsonObject.addProperty("translate", translatableContents.getKey());
                if (translatableContents.getArgs().length <= 0) return jsonObject;
                JsonArray jsonArray2 = new JsonArray();
                for (Object object : translatableContents.getArgs()) {
                    if (object instanceof Component) {
                        jsonArray2.add(this.serialize((Component)object, (Type)object.getClass(), jsonSerializationContext));
                        continue;
                    }
                    jsonArray2.add(new JsonPrimitive(String.valueOf(object)));
                }
                jsonObject.add("with", jsonArray2);
                return jsonObject;
            } else if (componentContents instanceof ScoreContents) {
                ScoreContents scoreContents = (ScoreContents)componentContents;
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.addProperty("name", scoreContents.getName());
                jsonObject2.addProperty("objective", scoreContents.getObjective());
                jsonObject.add("score", jsonObject2);
                return jsonObject;
            } else if (componentContents instanceof SelectorContents) {
                SelectorContents selectorContents = (SelectorContents)componentContents;
                jsonObject.addProperty("selector", selectorContents.getPattern());
                this.serializeSeparator(jsonSerializationContext, jsonObject, selectorContents.getSeparator());
                return jsonObject;
            } else if (componentContents instanceof KeybindContents) {
                KeybindContents keybindContents = (KeybindContents)componentContents;
                jsonObject.addProperty("keybind", keybindContents.getName());
                return jsonObject;
            } else {
                if (!(componentContents instanceof NbtContents)) throw new IllegalArgumentException("Don't know how to serialize " + componentContents + " as a Component");
                NbtContents nbtContents = (NbtContents)componentContents;
                jsonObject.addProperty("nbt", nbtContents.getNbtPath());
                jsonObject.addProperty("interpret", nbtContents.isInterpreting());
                this.serializeSeparator(jsonSerializationContext, jsonObject, nbtContents.getSeparator());
                DataSource dataSource = nbtContents.getDataSource();
                if (dataSource instanceof BlockDataSource) {
                    BlockDataSource blockDataSource = (BlockDataSource)dataSource;
                    jsonObject.addProperty("block", blockDataSource.posPattern());
                    return jsonObject;
                } else if (dataSource instanceof EntityDataSource) {
                    EntityDataSource entityDataSource = (EntityDataSource)dataSource;
                    jsonObject.addProperty("entity", entityDataSource.selectorPattern());
                    return jsonObject;
                } else {
                    if (!(dataSource instanceof StorageDataSource)) throw new IllegalArgumentException("Don't know how to serialize " + componentContents + " as a Component");
                    StorageDataSource storageDataSource = (StorageDataSource)dataSource;
                    jsonObject.addProperty("storage", storageDataSource.id().toString());
                }
            }
            return jsonObject;
        }

        private void serializeSeparator(JsonSerializationContext jsonSerializationContext, JsonObject jsonObject, Optional<Component> optional) {
            optional.ifPresent(component -> jsonObject.add("separator", this.serialize((Component)component, (Type)component.getClass(), jsonSerializationContext)));
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

