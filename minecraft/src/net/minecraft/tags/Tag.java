package net.minecraft.tags;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
	boolean contains(T object);

	List<T> getValues();

	default T getRandomElement(Random random) {
		List<T> list = this.getValues();
		return (T)list.get(random.nextInt(list.size()));
	}

	static <T> Tag<T> fromSet(Set<T> set) {
		final ImmutableList<T> immutableList = ImmutableList.copyOf(set);
		return new Tag<T>() {
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

	public static class Builder {
		private final Set<Tag.Entry> entries = Sets.<Tag.Entry>newLinkedHashSet();

		public static Tag.Builder tag() {
			return new Tag.Builder();
		}

		public Tag.Builder add(Tag.Entry entry) {
			this.entries.add(entry);
			return this;
		}

		public Tag.Builder addElement(ResourceLocation resourceLocation) {
			return this.add(new Tag.ElementEntry(resourceLocation));
		}

		public Tag.Builder addTag(ResourceLocation resourceLocation) {
			return this.add(new Tag.TagEntry(resourceLocation));
		}

		public <T> Optional<Tag<T>> build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2) {
			ImmutableSet.Builder<T> builder = ImmutableSet.builder();

			for (Tag.Entry entry : this.entries) {
				if (!entry.build(function, function2, builder::add)) {
					return Optional.empty();
				}
			}

			return Optional.of(Tag.fromSet(builder.build()));
		}

		public Stream<Tag.Entry> getEntries() {
			return this.entries.stream();
		}

		public <T> Stream<Tag.Entry> getUnresolvedEntries(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2) {
			return this.getEntries().filter(entry -> !entry.build(function, function2, object -> {
				}));
		}

		public Tag.Builder addFromJson(JsonObject jsonObject) {
			JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "values");
			List<Tag.Entry> list = Lists.<Tag.Entry>newArrayList();

			for (JsonElement jsonElement : jsonArray) {
				String string = GsonHelper.convertToString(jsonElement, "value");
				if (string.startsWith("#")) {
					list.add(new Tag.TagEntry(new ResourceLocation(string.substring(1))));
				} else {
					list.add(new Tag.ElementEntry(new ResourceLocation(string)));
				}
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

			for (Tag.Entry entry : this.entries) {
				entry.serializeTo(jsonArray);
			}

			jsonObject.addProperty("replace", false);
			jsonObject.add("values", jsonArray);
			return jsonObject;
		}
	}

	public static class ElementEntry implements Tag.Entry {
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

		public String toString() {
			return this.id.toString();
		}
	}

	public interface Entry {
		<T> boolean build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2, Consumer<T> consumer);

		void serializeTo(JsonArray jsonArray);
	}

	public interface Named<T> extends Tag<T> {
		ResourceLocation getName();
	}

	public static class TagEntry implements Tag.Entry {
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
				tag.getValues().forEach(consumer);
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
	}

	public static class TypedBuilder<T> extends Tag.Builder {
		private final Function<T, ResourceLocation> elementLookup;

		public TypedBuilder(Function<T, ResourceLocation> function) {
			this.elementLookup = function;
		}

		public Tag.TypedBuilder<T> add(T object) {
			this.addElement((ResourceLocation)this.elementLookup.apply(object));
			return this;
		}

		public Tag.TypedBuilder<T> add(Collection<T> collection) {
			collection.stream().map(this.elementLookup).forEach(this::addElement);
			return this;
		}

		@SafeVarargs
		public final Tag.TypedBuilder<T> add(T... objects) {
			this.add(Arrays.asList(objects));
			return this;
		}

		public Tag.TypedBuilder<T> addTag(Tag.Named<T> named) {
			this.addTag(named.getName());
			return this;
		}
	}
}
