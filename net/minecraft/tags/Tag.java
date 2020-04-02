/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public interface Tag<T> {
    public boolean contains(T var1);

    public List<T> getValues();

    default public T getRandomElement(Random random) {
        List<T> list = this.getValues();
        return list.get(random.nextInt(list.size()));
    }

    public static <T> Tag<T> fromSet(final Set<T> set) {
        final ImmutableList<T> immutableList = ImmutableList.copyOf(set);
        return new Tag<T>(){

            @Override
            public boolean contains(T object) {
                return set.contains(object);
            }

            @Override
            public List<T> getValues() {
                return immutableList;
            }
        };
    }

    public static interface Named<T>
    extends Tag<T> {
        public ResourceLocation getName();
    }

    public static class TagEntry
    implements Entry {
        private final ResourceLocation id;

        public TagEntry(ResourceLocation resourceLocation) {
            this.id = resourceLocation;
        }

        @Override
        public <T> boolean build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2, Consumer<T> consumer) {
            Tag<T> tag = function.apply(this.id);
            if (tag == null) {
                return false;
            }
            tag.getValues().forEach(consumer);
            return true;
        }

        @Override
        public void serializeTo(JsonArray jsonArray) {
            jsonArray.add("#" + this.id);
        }

        public String toString() {
            return "#" + this.id;
        }
    }

    public static class ElementEntry
    implements Entry {
        private final ResourceLocation id;

        public ElementEntry(ResourceLocation resourceLocation) {
            this.id = resourceLocation;
        }

        @Override
        public <T> boolean build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2, Consumer<T> consumer) {
            T object = function2.apply(this.id);
            if (object == null) {
                return false;
            }
            consumer.accept(object);
            return true;
        }

        @Override
        public void serializeTo(JsonArray jsonArray) {
            jsonArray.add(this.id.toString());
        }

        public String toString() {
            return this.id.toString();
        }
    }

    public static interface Entry {
        public <T> boolean build(Function<ResourceLocation, Tag<T>> var1, Function<ResourceLocation, T> var2, Consumer<T> var3);

        public void serializeTo(JsonArray var1);
    }

    public static class TypedBuilder<T>
    extends Builder {
        private final Function<T, ResourceLocation> elementLookup;

        public TypedBuilder(Function<T, ResourceLocation> function) {
            this.elementLookup = function;
        }

        public TypedBuilder<T> add(T object) {
            this.addElement(this.elementLookup.apply(object));
            return this;
        }

        public TypedBuilder<T> add(Collection<T> collection) {
            collection.stream().map(this.elementLookup).forEach(this::addElement);
            return this;
        }

        @SafeVarargs
        public final TypedBuilder<T> add(T ... objects) {
            this.add((Collection<T>)Arrays.asList(objects));
            return this;
        }

        public TypedBuilder<T> addTag(Named<T> named) {
            this.addTag(named.getName());
            return this;
        }
    }

    public static class Builder {
        private final Set<Entry> entries = Sets.newLinkedHashSet();

        public static Builder tag() {
            return new Builder();
        }

        public Builder add(Entry entry) {
            this.entries.add(entry);
            return this;
        }

        public Builder addElement(ResourceLocation resourceLocation) {
            return this.add(new ElementEntry(resourceLocation));
        }

        public Builder addTag(ResourceLocation resourceLocation) {
            return this.add(new TagEntry(resourceLocation));
        }

        public <T> Optional<Tag<T>> build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2) {
            ImmutableSet.Builder builder = ImmutableSet.builder();
            for (Entry entry : this.entries) {
                if (entry.build(function, function2, builder::add)) continue;
                return Optional.empty();
            }
            return Optional.of(Tag.fromSet(builder.build()));
        }

        public Stream<Entry> getEntries() {
            return this.entries.stream();
        }

        public <T> Stream<Entry> getUnresolvedEntries(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2) {
            return this.getEntries().filter(entry -> !entry.build(function, function2, object -> {}));
        }

        public Builder addFromJson(JsonObject jsonObject) {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "values");
            ArrayList<Entry> list = Lists.newArrayList();
            for (JsonElement jsonElement : jsonArray) {
                String string = GsonHelper.convertToString(jsonElement, "value");
                if (string.startsWith("#")) {
                    list.add(new TagEntry(new ResourceLocation(string.substring(1))));
                    continue;
                }
                list.add(new ElementEntry(new ResourceLocation(string)));
            }
            if (GsonHelper.getAsBoolean(jsonObject, "replace", false)) {
                this.entries.clear();
            }
            this.entries.addAll(list);
            return this;
        }

        public JsonObject serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            for (Entry entry : this.entries) {
                entry.serializeTo(jsonArray);
            }
            jsonObject.addProperty("replace", false);
            jsonObject.add("values", jsonArray);
            return jsonObject;
        }
    }
}

