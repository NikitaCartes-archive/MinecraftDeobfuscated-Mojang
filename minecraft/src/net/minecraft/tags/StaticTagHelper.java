package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class StaticTagHelper<T> {
	private final ResourceKey<? extends Registry<T>> key;
	private final String directory;
	private TagCollection<T> source = TagCollection.empty();
	private final List<StaticTagHelper.Wrapper<T>> wrappers = Lists.<StaticTagHelper.Wrapper<T>>newArrayList();

	public StaticTagHelper(ResourceKey<? extends Registry<T>> resourceKey, String string) {
		this.key = resourceKey;
		this.directory = string;
	}

	public Tag.Named<T> bind(String string) {
		StaticTagHelper.Wrapper<T> wrapper = new StaticTagHelper.Wrapper<>(new ResourceLocation(string));
		this.wrappers.add(wrapper);
		return wrapper;
	}

	@Environment(EnvType.CLIENT)
	public void resetToEmpty() {
		this.source = TagCollection.empty();
		Tag<T> tag = SetTag.empty();
		this.wrappers.forEach(wrapper -> wrapper.rebind(resourceLocation -> tag));
	}

	public void reset(TagContainer tagContainer) {
		TagCollection<T> tagCollection = tagContainer.getOrEmpty(this.key);
		this.source = tagCollection;
		this.wrappers.forEach(wrapper -> wrapper.rebind(tagCollection::getTag));
	}

	public TagCollection<T> getAllTags() {
		return this.source;
	}

	public Set<ResourceLocation> getMissingTags(TagContainer tagContainer) {
		TagCollection<T> tagCollection = tagContainer.getOrEmpty(this.key);
		Set<ResourceLocation> set = (Set<ResourceLocation>)this.wrappers.stream().map(StaticTagHelper.Wrapper::getName).collect(Collectors.toSet());
		ImmutableSet<ResourceLocation> immutableSet = ImmutableSet.copyOf(tagCollection.getAvailableTags());
		return Sets.<ResourceLocation>difference(set, immutableSet);
	}

	public ResourceKey<? extends Registry<T>> getKey() {
		return this.key;
	}

	public String getDirectory() {
		return this.directory;
	}

	protected void addToCollection(TagContainer.Builder builder) {
		builder.add(
			this.key, TagCollection.of((Map<ResourceLocation, Tag<T>>)this.wrappers.stream().collect(Collectors.toMap(Tag.Named::getName, wrapper -> wrapper)))
		);
	}

	static class Wrapper<T> implements Tag.Named<T> {
		@Nullable
		private Tag<T> tag;
		protected final ResourceLocation name;

		private Wrapper(ResourceLocation resourceLocation) {
			this.name = resourceLocation;
		}

		@Override
		public ResourceLocation getName() {
			return this.name;
		}

		private Tag<T> resolve() {
			if (this.tag == null) {
				throw new IllegalStateException("Tag " + this.name + " used before it was bound");
			} else {
				return this.tag;
			}
		}

		void rebind(Function<ResourceLocation, Tag<T>> function) {
			this.tag = (Tag<T>)function.apply(this.name);
		}

		@Override
		public boolean contains(T object) {
			return this.resolve().contains(object);
		}

		@Override
		public List<T> getValues() {
			return this.resolve().getValues();
		}
	}
}
