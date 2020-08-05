package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

public class StaticTagHelper<T> {
	private TagCollection<T> source = TagCollection.empty();
	private final List<StaticTagHelper.Wrapper<T>> wrappers = Lists.<StaticTagHelper.Wrapper<T>>newArrayList();
	private final Function<TagContainer, TagCollection<T>> collectionGetter;

	public StaticTagHelper(Function<TagContainer, TagCollection<T>> function) {
		this.collectionGetter = function;
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
		TagCollection<T> tagCollection = (TagCollection<T>)this.collectionGetter.apply(tagContainer);
		this.source = tagCollection;
		this.wrappers.forEach(wrapper -> wrapper.rebind(tagCollection::getTag));
	}

	public TagCollection<T> getAllTags() {
		return this.source;
	}

	public List<? extends Tag.Named<T>> getWrappers() {
		return this.wrappers;
	}

	public Set<ResourceLocation> getMissingTags(TagContainer tagContainer) {
		TagCollection<T> tagCollection = (TagCollection<T>)this.collectionGetter.apply(tagContainer);
		Set<ResourceLocation> set = (Set<ResourceLocation>)this.wrappers.stream().map(StaticTagHelper.Wrapper::getName).collect(Collectors.toSet());
		ImmutableSet<ResourceLocation> immutableSet = ImmutableSet.copyOf(tagCollection.getAvailableTags());
		return Sets.<ResourceLocation>difference(set, immutableSet);
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
