package net.minecraft.tags;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public interface Tag<T> {
	static <T> Codec<Tag<T>> codec(Supplier<TagCollection<T>> supplier) {
		return ResourceLocation.CODEC
			.flatXmap(
				resourceLocation -> (DataResult)Optional.ofNullable(((TagCollection)supplier.get()).getTag(resourceLocation))
						.map(DataResult::success)
						.orElseGet(() -> DataResult.error("Unknown tag: " + resourceLocation)),
				tag -> (DataResult)Optional.ofNullable(((TagCollection)supplier.get()).getId(tag))
						.map(DataResult::success)
						.orElseGet(() -> DataResult.error("Unknown tag: " + tag))
			);
	}

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
		private final List<Tag.BuilderEntry> entries = Lists.<Tag.BuilderEntry>newArrayList();

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

		public Tag.Builder addTag(ResourceLocation resourceLocation, String string) {
			return this.add(new Tag.TagEntry(resourceLocation), string);
		}

		public <T> Optional<Tag<T>> build(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2) {
			ImmutableSet.Builder<T> builder = ImmutableSet.builder();

			for (Tag.BuilderEntry builderEntry : this.entries) {
				if (!builderEntry.getEntry().build(function, function2, builder::add)) {
					return Optional.empty();
				}
			}

			return Optional.of(Tag.fromSet(builder.build()));
		}

		public Stream<Tag.BuilderEntry> getEntries() {
			return this.entries.stream();
		}

		public <T> Stream<Tag.BuilderEntry> getUnresolvedEntries(Function<ResourceLocation, Tag<T>> function, Function<ResourceLocation, T> function2) {
			return this.getEntries().filter(builderEntry -> !builderEntry.getEntry().build(function, function2, object -> {
				}));
		}

		public Tag.Builder addFromJson(JsonObject jsonObject, String string) {
			JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "values");
			List<Tag.Entry> list = Lists.<Tag.Entry>newArrayList();

			for (JsonElement jsonElement : jsonArray) {
				String string2 = GsonHelper.convertToString(jsonElement, "value");
				if (string2.startsWith("#")) {
					list.add(new Tag.TagEntry(new ResourceLocation(string2.substring(1))));
				} else {
					list.add(new Tag.ElementEntry(new ResourceLocation(string2)));
				}
			}

			if (GsonHelper.getAsBoolean(jsonObject, "replace", false)) {
				this.entries.clear();
			}

			list.forEach(entry -> this.entries.add(new Tag.BuilderEntry(entry, string)));
			return this;
		}

		public JsonObject serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			JsonArray jsonArray = new JsonArray();

			for (Tag.BuilderEntry builderEntry : this.entries) {
				builderEntry.getEntry().serializeTo(jsonArray);
			}

			jsonObject.addProperty("replace", false);
			jsonObject.add("values", jsonArray);
			return jsonObject;
		}
	}

	public static class BuilderEntry {
		private final Tag.Entry entry;
		private final String source;

		private BuilderEntry(Tag.Entry entry, String string) {
			this.entry = entry;
			this.source = string;
		}

		public Tag.Entry getEntry() {
			return this.entry;
		}

		public String toString() {
			return this.entry.toString() + " (from " + this.source + ")";
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
}
