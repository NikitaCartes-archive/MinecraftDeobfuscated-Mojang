package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class Tag<T> {
	private static final Tag<?> EMPTY = new Tag(List.of());
	final List<T> elements;

	public Tag(Collection<T> collection) {
		this.elements = List.copyOf(collection);
	}

	public List<T> getValues() {
		return this.elements;
	}

	public static <T> Tag<T> empty() {
		return (Tag<T>)EMPTY;
	}

	public static class Builder {
		private final List<Tag.BuilderEntry> entries = new ArrayList();

		public static Tag.Builder tag() {
			return new Tag.Builder();
		}

		public Tag.Builder add(Tag.BuilderEntry builderEntry) {
			this.entries.add(builderEntry);
			return this;
		}

		public Tag.Builder add(Tag.Entry entry, String string) {
			return this.add(new Tag.BuilderEntry(entry, string));
		}

		public Tag.Builder addElement(ResourceLocation resourceLocation, String string) {
			return this.add(new Tag.ElementEntry(resourceLocation), string);
		}

		public Tag.Builder addOptionalElement(ResourceLocation resourceLocation, String string) {
			return this.add(new Tag.OptionalElementEntry(resourceLocation), string);
		}

		public Tag.Builder addTag(ResourceLocation resourceLocation, String string) {
			return this.add(new Tag.TagEntry(resourceLocation), string);
		}

		public Tag.Builder addOptionalTag(ResourceLocation resourceLocation, String string) {
			return this.add(new Tag.OptionalTagEntry(resourceLocation), string);
		}

		public <T> Either<Collection<Tag.BuilderEntry>, Tag<T>> build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2) {
			ImmutableSet.Builder<T> builder = ImmutableSet.builder();
			List<Tag.BuilderEntry> list = new ArrayList();

			for (Tag.BuilderEntry builderEntry : this.entries) {
				if (!builderEntry.entry().build(function, function2, builder::add)) {
					list.add(builderEntry);
				}
			}

			return list.isEmpty() ? Either.right(new Tag<>(builder.build())) : Either.left(list);
		}

		public Stream<Tag.BuilderEntry> getEntries() {
			return this.entries.stream();
		}

		public void visitRequiredDependencies(Consumer<ResourceLocation> consumer) {
			this.entries.forEach(builderEntry -> builderEntry.entry.visitRequiredDependencies(consumer));
		}

		public void visitOptionalDependencies(Consumer<ResourceLocation> consumer) {
			this.entries.forEach(builderEntry -> builderEntry.entry.visitOptionalDependencies(consumer));
		}

		public Tag.Builder addFromJson(JsonObject jsonObject, String string) {
			JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "values");
			List<Tag.Entry> list = new ArrayList();

			for (JsonElement jsonElement : jsonArray) {
				list.add(parseEntry(jsonElement));
			}

			if (GsonHelper.getAsBoolean(jsonObject, "replace", false)) {
				this.entries.clear();
			}

			list.forEach(entry -> this.entries.add(new Tag.BuilderEntry(entry, string)));
			return this;
		}

		private static Tag.Entry parseEntry(JsonElement jsonElement) {
			String string;
			boolean bl;
			if (jsonElement.isJsonObject()) {
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				string = GsonHelper.getAsString(jsonObject, "id");
				bl = GsonHelper.getAsBoolean(jsonObject, "required", true);
			} else {
				string = GsonHelper.convertToString(jsonElement, "id");
				bl = true;
			}

			if (string.startsWith("#")) {
				ResourceLocation resourceLocation = new ResourceLocation(string.substring(1));
				return (Tag.Entry)(bl ? new Tag.TagEntry(resourceLocation) : new Tag.OptionalTagEntry(resourceLocation));
			} else {
				ResourceLocation resourceLocation = new ResourceLocation(string);
				return (Tag.Entry)(bl ? new Tag.ElementEntry(resourceLocation) : new Tag.OptionalElementEntry(resourceLocation));
			}
		}

		public JsonObject serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			JsonArray jsonArray = new JsonArray();

			for (Tag.BuilderEntry builderEntry : this.entries) {
				builderEntry.entry().serializeTo(jsonArray);
			}

			jsonObject.addProperty("replace", false);
			jsonObject.add("values", jsonArray);
			return jsonObject;
		}
	}

	public static record BuilderEntry(Tag.Entry entry, String source) {

		public String toString() {
			return this.entry + " (from " + this.source + ")";
		}
	}

	static class ElementEntry implements Tag.Entry {
		private final ResourceLocation id;

		public ElementEntry(ResourceLocation resourceLocation) {
			this.id = resourceLocation;
		}

		@Override
		public <T> boolean build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2, Consumer<T> consumer) {
			T object = (T)function2.apply(this.id);
			if (object == null) {
				return false;
			} else {
				consumer.accept(object);
				return true;
			}
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

	public interface Entry {
		<T> boolean build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2, Consumer<T> consumer);

		void serializeTo(JsonArray jsonArray);

		default void visitRequiredDependencies(Consumer<ResourceLocation> consumer) {
		}

		default void visitOptionalDependencies(Consumer<ResourceLocation> consumer) {
		}

		boolean verifyIfPresent(Predicate<ResourceLocation> predicate, Predicate<ResourceLocation> predicate2);
	}

	static class OptionalElementEntry implements Tag.Entry {
		private final ResourceLocation id;

		public OptionalElementEntry(ResourceLocation resourceLocation) {
			this.id = resourceLocation;
		}

		@Override
		public <T> boolean build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2, Consumer<T> consumer) {
			T object = (T)function2.apply(this.id);
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

	static class OptionalTagEntry implements Tag.Entry {
		private final ResourceLocation id;

		public OptionalTagEntry(ResourceLocation resourceLocation) {
			this.id = resourceLocation;
		}

		@Override
		public <T> boolean build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2, Consumer<T> consumer) {
			Tag<T> tag = (Tag<T>)function.apply(this.id);
			if (tag != null) {
				tag.elements.forEach(consumer);
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

	static class TagEntry implements Tag.Entry {
		private final ResourceLocation id;

		public TagEntry(ResourceLocation resourceLocation) {
			this.id = resourceLocation;
		}

		@Override
		public <T> boolean build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2, Consumer<T> consumer) {
			Tag<T> tag = (Tag<T>)function.apply(this.id);
			if (tag == null) {
				return false;
			} else {
				tag.elements.forEach(consumer);
				return true;
			}
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
}
