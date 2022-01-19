/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SetTag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.GsonHelper;

public interface Tag<T> {
    public static <T> Codec<Tag<T>> codec(Supplier<TagCollection<T>> supplier) {
        return ResourceLocation.CODEC.flatXmap(resourceLocation -> Optional.ofNullable(((TagCollection)supplier.get()).getTag((ResourceLocation)resourceLocation)).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown tag: " + resourceLocation)), tag -> Optional.ofNullable(((TagCollection)supplier.get()).getId(tag)).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown tag: " + tag)));
    }

    public boolean contains(T var1);

    public List<T> getValues();

    default public Optional<T> getRandomElement(Random random) {
        List<T> list = this.getValues();
        int i = list.size();
        if (i > 0) {
            return Optional.of(list.get(random.nextInt(i)));
        }
        return Optional.empty();
    }

    public static <T> Tag<T> fromSet(Set<T> set) {
        return SetTag.create(set);
    }

    public static interface Named<T>
    extends Tag<T> {
        public ResourceLocation getName();
    }

    public static class OptionalTagEntry
    implements Entry {
        private final ResourceLocation id;

        public OptionalTagEntry(ResourceLocation resourceLocation) {
            this.id = resourceLocation;
        }

        @Override
        public <T> boolean build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2, Consumer<T> consumer) {
            Tag<T> tag = function.apply(this.id);
            if (tag != null) {
                tag.getValues().forEach(consumer);
            }
            return true;
        }

        @Override
        public void serializeTo(JsonArray jsonArray) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", "#" + this.id);
            jsonObject.addProperty("required", false);
            jsonArray.add(jsonObject);
        }

        public String toString() {
            return "#" + this.id + "?";
        }

        @Override
        public void visitOptionalDependencies(Consumer<ResourceLocation> consumer) {
            consumer.accept(this.id);
        }

        @Override
        public boolean verifyIfPresent(Predicate<ResourceLocation> predicate, Predicate<ResourceLocation> predicate2) {
            return true;
        }
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

        @Override
        public boolean verifyIfPresent(Predicate<ResourceLocation> predicate, Predicate<ResourceLocation> predicate2) {
            return predicate2.test(this.id);
        }

        @Override
        public void visitRequiredDependencies(Consumer<ResourceLocation> consumer) {
            consumer.accept(this.id);
        }
    }

    public static class OptionalElementEntry
    implements Entry {
        private final ResourceLocation id;

        public OptionalElementEntry(ResourceLocation resourceLocation) {
            this.id = resourceLocation;
        }

        @Override
        public <T> boolean build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2, Consumer<T> consumer) {
            T object = function2.apply(this.id);
            if (object != null) {
                consumer.accept(object);
            }
            return true;
        }

        @Override
        public void serializeTo(JsonArray jsonArray) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", this.id.toString());
            jsonObject.addProperty("required", false);
            jsonArray.add(jsonObject);
        }

        @Override
        public boolean verifyIfPresent(Predicate<ResourceLocation> predicate, Predicate<ResourceLocation> predicate2) {
            return true;
        }

        public String toString() {
            return this.id + "?";
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

        @Override
        public boolean verifyIfPresent(Predicate<ResourceLocation> predicate, Predicate<ResourceLocation> predicate2) {
            return predicate.test(this.id);
        }

        public String toString() {
            return this.id.toString();
        }
    }

    public static interface Entry {
        public <T> boolean build(Function<ResourceLocation, Tag<T>> var1, Function<ResourceLocation, T> var2, Consumer<T> var3);

        public void serializeTo(JsonArray var1);

        default public void visitRequiredDependencies(Consumer<ResourceLocation> consumer) {
        }

        default public void visitOptionalDependencies(Consumer<ResourceLocation> consumer) {
        }

        public boolean verifyIfPresent(Predicate<ResourceLocation> var1, Predicate<ResourceLocation> var2);
    }

    public static class Builder {
        private final List<BuilderEntry> entries = Lists.newArrayList();

        public static Builder tag() {
            return new Builder();
        }

        public Builder add(BuilderEntry builderEntry) {
            this.entries.add(builderEntry);
            return this;
        }

        public Builder add(Entry entry, String string) {
            return this.add(new BuilderEntry(entry, string));
        }

        public Builder addElement(ResourceLocation resourceLocation, String string) {
            return this.add(new ElementEntry(resourceLocation), string);
        }

        public Builder addOptionalElement(ResourceLocation resourceLocation, String string) {
            return this.add(new OptionalElementEntry(resourceLocation), string);
        }

        public Builder addTag(ResourceLocation resourceLocation, String string) {
            return this.add(new TagEntry(resourceLocation), string);
        }

        public Builder addOptionalTag(ResourceLocation resourceLocation, String string) {
            return this.add(new OptionalTagEntry(resourceLocation), string);
        }

        public <T> Either<Collection<BuilderEntry>, Tag<T>> build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2) {
            ImmutableSet.Builder builder = ImmutableSet.builder();
            ArrayList<BuilderEntry> list = Lists.newArrayList();
            for (BuilderEntry builderEntry : this.entries) {
                if (builderEntry.getEntry().build(function, function2, builder::add)) continue;
                list.add(builderEntry);
            }
            return list.isEmpty() ? Either.right(Tag.fromSet(builder.build())) : Either.left(list);
        }

        public Stream<BuilderEntry> getEntries() {
            return this.entries.stream();
        }

        public void visitRequiredDependencies(Consumer<ResourceLocation> consumer) {
            this.entries.forEach(builderEntry -> builderEntry.entry.visitRequiredDependencies(consumer));
        }

        public void visitOptionalDependencies(Consumer<ResourceLocation> consumer) {
            this.entries.forEach(builderEntry -> builderEntry.entry.visitOptionalDependencies(consumer));
        }

        public Builder addFromJson(JsonObject jsonObject, String string) {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "values");
            ArrayList<Entry> list = Lists.newArrayList();
            for (JsonElement jsonElement : jsonArray) {
                list.add(Builder.parseEntry(jsonElement));
            }
            if (GsonHelper.getAsBoolean(jsonObject, "replace", false)) {
                this.entries.clear();
            }
            list.forEach(entry -> this.entries.add(new BuilderEntry((Entry)entry, string)));
            return this;
        }

        private static Entry parseEntry(JsonElement jsonElement) {
            ResourceLocation resourceLocation;
            boolean bl;
            String string;
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                string = GsonHelper.getAsString(jsonObject, "id");
                bl = GsonHelper.getAsBoolean(jsonObject, "required", true);
            } else {
                string = GsonHelper.convertToString(jsonElement, "id");
                bl = true;
            }
            if (string.startsWith("#")) {
                resourceLocation = new ResourceLocation(string.substring(1));
                return bl ? new TagEntry(resourceLocation) : new OptionalTagEntry(resourceLocation);
            }
            resourceLocation = new ResourceLocation(string);
            return bl ? new ElementEntry(resourceLocation) : new OptionalElementEntry(resourceLocation);
        }

        public JsonObject serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            for (BuilderEntry builderEntry : this.entries) {
                builderEntry.getEntry().serializeTo(jsonArray);
            }
            jsonObject.addProperty("replace", false);
            jsonObject.add("values", jsonArray);
            return jsonObject;
        }
    }

    public static class BuilderEntry {
        final Entry entry;
        private final String source;

        BuilderEntry(Entry entry, String string) {
            this.entry = entry;
            this.source = string;
        }

        public Entry getEntry() {
            return this.entry;
        }

        public String getSource() {
            return this.source;
        }

        public String toString() {
            return this.entry + " (from " + this.source + ")";
        }
    }
}

