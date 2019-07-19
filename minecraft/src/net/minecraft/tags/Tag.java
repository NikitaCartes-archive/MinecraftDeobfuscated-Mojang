package net.minecraft.tags;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class Tag<T> {
	private final ResourceLocation id;
	private final Set<T> values;
	private final Collection<Tag.Entry<T>> source;

	public Tag(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
		this.values = Collections.emptySet();
		this.source = Collections.emptyList();
	}

	public Tag(ResourceLocation resourceLocation, Collection<Tag.Entry<T>> collection, boolean bl) {
		this.id = resourceLocation;
		this.values = (Set<T>)(bl ? Sets.<T>newLinkedHashSet() : Sets.<T>newHashSet());
		this.source = collection;

		for (Tag.Entry<T> entry : collection) {
			entry.build(this.values);
		}
	}

	public JsonObject serializeToJson(Function<T, ResourceLocation> function) {
		JsonObject jsonObject = new JsonObject();
		JsonArray jsonArray = new JsonArray();

		for (Tag.Entry<T> entry : this.source) {
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

	public Collection<Tag.Entry<T>> getSource() {
		return this.source;
	}

	public T getRandomElement(Random random) {
		List<T> list = Lists.<T>newArrayList(this.getValues());
		return (T)list.get(random.nextInt(list.size()));
	}

	public ResourceLocation getId() {
		return this.id;
	}

	public static class Builder<T> {
		private final Set<Tag.Entry<T>> values = Sets.<Tag.Entry<T>>newLinkedHashSet();
		private boolean ordered;

		public static <T> Tag.Builder<T> tag() {
			return new Tag.Builder<>();
		}

		public Tag.Builder<T> add(Tag.Entry<T> entry) {
			this.values.add(entry);
			return this;
		}

		public Tag.Builder<T> add(T object) {
			this.values.add(new Tag.ValuesEntry(Collections.singleton(object)));
			return this;
		}

		@SafeVarargs
		public final Tag.Builder<T> add(T... objects) {
			this.values.add(new Tag.ValuesEntry(Lists.<T>newArrayList(objects)));
			return this;
		}

		public Tag.Builder<T> addTag(Tag<T> tag) {
			this.values.add(new Tag.TagEntry<>(tag));
			return this;
		}

		public Tag.Builder<T> keepOrder(boolean bl) {
			this.ordered = bl;
			return this;
		}

		public boolean canBuild(Function<ResourceLocation, Tag<T>> function) {
			for (Tag.Entry<T> entry : this.values) {
				if (!entry.canBuild(function)) {
					return false;
				}
			}

			return true;
		}

		public Tag<T> build(ResourceLocation resourceLocation) {
			return new Tag<>(resourceLocation, this.values, this.ordered);
		}

		public Tag.Builder<T> addFromJson(Function<ResourceLocation, Optional<T>> function, JsonObject jsonObject) {
			JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "values");
			List<Tag.Entry<T>> list = Lists.<Tag.Entry<T>>newArrayList();

			for (JsonElement jsonElement : jsonArray) {
				String string = GsonHelper.convertToString(jsonElement, "value");
				if (string.startsWith("#")) {
					list.add(new Tag.TagEntry(new ResourceLocation(string.substring(1))));
				} else {
					ResourceLocation resourceLocation = new ResourceLocation(string);
					list.add(
						new Tag.ValuesEntry(
							Collections.singleton(((Optional)function.apply(resourceLocation)).orElseThrow(() -> new JsonParseException("Unknown value '" + resourceLocation + "'")))
						)
					);
				}
			}

			if (GsonHelper.getAsBoolean(jsonObject, "replace", false)) {
				this.values.clear();
			}

			this.values.addAll(list);
			return this;
		}
	}

	public interface Entry<T> {
		default boolean canBuild(Function<ResourceLocation, Tag<T>> function) {
			return true;
		}

		void build(Collection<T> collection);

		void serializeTo(JsonArray jsonArray, Function<T, ResourceLocation> function);
	}

	public static class TagEntry<T> implements Tag.Entry<T> {
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
				this.tag = (Tag<T>)function.apply(this.id);
			}

			return this.tag != null;
		}

		@Override
		public void build(Collection<T> collection) {
			if (this.tag == null) {
				throw new IllegalStateException("Cannot build unresolved tag entry");
			} else {
				collection.addAll(this.tag.getValues());
			}
		}

		public ResourceLocation getId() {
			if (this.tag != null) {
				return this.tag.getId();
			} else if (this.id != null) {
				return this.id;
			} else {
				throw new IllegalStateException("Cannot serialize an anonymous tag to json!");
			}
		}

		@Override
		public void serializeTo(JsonArray jsonArray, Function<T, ResourceLocation> function) {
			jsonArray.add("#" + this.getId());
		}
	}

	public static class ValuesEntry<T> implements Tag.Entry<T> {
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
				ResourceLocation resourceLocation = (ResourceLocation)function.apply(object);
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
}
