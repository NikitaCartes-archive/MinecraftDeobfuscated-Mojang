/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public class Tag<T> {
    private final ResourceLocation id;
    private final Set<T> values;
    private final Collection<Entry<T>> source;

    public Tag(ResourceLocation resourceLocation) {
        this.id = resourceLocation;
        this.values = Collections.emptySet();
        this.source = Collections.emptyList();
    }

    public Tag(ResourceLocation resourceLocation, Collection<Entry<T>> collection, boolean bl) {
        this.id = resourceLocation;
        this.values = bl ? Sets.newLinkedHashSet() : Sets.newHashSet();
        this.source = collection;
        for (Entry<T> entry : collection) {
            entry.build(this.values);
        }
    }

    public JsonObject serializeToJson(Function<T, ResourceLocation> function) {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (Entry<T> entry : this.source) {
            entry.serializeTo(jsonArray, function);
        }
        jsonObject.addProperty("replace", false);
        jsonObject.add("values", jsonArray);
        return jsonObject;
    }

    public boolean contains(T object) {
        return this.values.contains(object);
    }

    public Collection<T> getValues() {
        return this.values;
    }

    public Collection<Entry<T>> getSource() {
        return this.source;
    }

    public T getRandomElement(Random random) {
        ArrayList<T> list = Lists.newArrayList(this.getValues());
        return (T)list.get(random.nextInt(list.size()));
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public static class TagEntry<T>
    implements Entry<T> {
        @Nullable
        private final ResourceLocation id;
        @Nullable
        private Tag<T> tag;

        public TagEntry(ResourceLocation resourceLocation) {
            this.id = resourceLocation;
        }

        public TagEntry(Tag<T> tag) {
            this.id = tag.getId();
            this.tag = tag;
        }

        @Override
        public boolean canBuild(Function<ResourceLocation, Tag<T>> function) {
            if (this.tag == null) {
                this.tag = function.apply(this.id);
            }
            return this.tag != null;
        }

        @Override
        public void build(Collection<T> collection) {
            if (this.tag == null) {
                throw new IllegalStateException("Cannot build unresolved tag entry");
            }
            collection.addAll(this.tag.getValues());
        }

        public ResourceLocation getId() {
            if (this.tag != null) {
                return this.tag.getId();
            }
            if (this.id != null) {
                return this.id;
            }
            throw new IllegalStateException("Cannot serialize an anonymous tag to json!");
        }

        @Override
        public void serializeTo(JsonArray jsonArray, Function<T, ResourceLocation> function) {
            jsonArray.add("#" + this.getId());
        }
    }

    public static class ValuesEntry<T>
    implements Entry<T> {
        private final Collection<T> values;

        public ValuesEntry(Collection<T> collection) {
            this.values = collection;
        }

        @Override
        public void build(Collection<T> collection) {
            collection.addAll(this.values);
        }

        @Override
        public void serializeTo(JsonArray jsonArray, Function<T, ResourceLocation> function) {
            for (T object : this.values) {
                ResourceLocation resourceLocation = function.apply(object);
                if (resourceLocation == null) {
                    throw new IllegalStateException("Unable to serialize an anonymous value to json!");
                }
                jsonArray.add(resourceLocation.toString());
            }
        }

        public Collection<T> getValues() {
            return this.values;
        }
    }

    public static interface Entry<T> {
        default public boolean canBuild(Function<ResourceLocation, Tag<T>> function) {
            return true;
        }

        public void build(Collection<T> var1);

        public void serializeTo(JsonArray var1, Function<T, ResourceLocation> var2);
    }

    public static class Builder<T> {
        private final Set<Entry<T>> values = Sets.newLinkedHashSet();
        private boolean ordered;

        public static <T> Builder<T> tag() {
            return new Builder<T>();
        }

        public Builder<T> add(Entry<T> entry) {
            this.values.add(entry);
            return this;
        }

        public Builder<T> add(T object) {
            this.values.add(new ValuesEntry<T>(Collections.singleton(object)));
            return this;
        }

        @SafeVarargs
        public final Builder<T> add(T ... objects) {
            this.values.add(new ValuesEntry<T>(Lists.newArrayList(objects)));
            return this;
        }

        public Builder<T> addTag(Tag<T> tag) {
            this.values.add(new TagEntry<T>(tag));
            return this;
        }

        public Builder<T> keepOrder(boolean bl) {
            this.ordered = bl;
            return this;
        }

        public boolean canBuild(Function<ResourceLocation, Tag<T>> function) {
            for (Entry<T> entry : this.values) {
                if (entry.canBuild(function)) continue;
                return false;
            }
            return true;
        }

        public Tag<T> build(ResourceLocation resourceLocation) {
            return new Tag<T>(resourceLocation, this.values, this.ordered);
        }

        public Builder<T> addFromJson(Function<ResourceLocation, Optional<T>> function, JsonObject jsonObject) {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "values");
            ArrayList list = Lists.newArrayList();
            for (JsonElement jsonElement : jsonArray) {
                String string = GsonHelper.convertToString(jsonElement, "value");
                if (string.startsWith("#")) {
                    list.add(new TagEntry(new ResourceLocation(string.substring(1))));
                    continue;
                }
                ResourceLocation resourceLocation = new ResourceLocation(string);
                list.add(new ValuesEntry<T>(Collections.singleton(function.apply(resourceLocation).orElseThrow(() -> new JsonParseException("Unknown value '" + resourceLocation + "'")))));
            }
            if (GsonHelper.getAsBoolean(jsonObject, "replace", false)) {
                this.values.clear();
            }
            this.values.addAll(list);
            return this;
        }
    }
}

