package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

public class StaticTagHelper<T> {
	private final TagCollection<T> empty = new TagCollection<>(resourceLocation -> Optional.empty(), "", "");
	private TagCollection<T> source = this.empty;
	private final List<StaticTagHelper.Wrapper<T>> wrappers = Lists.<StaticTagHelper.Wrapper<T>>newArrayList();

	public Tag.Named<T> bind(String string) {
		StaticTagHelper.Wrapper<T> wrapper = new StaticTagHelper.Wrapper<>(new ResourceLocation(string));
		this.wrappers.add(wrapper);
		return wrapper;
	}

	@Environment(EnvType.CLIENT)
	public void resetToEmpty() {
		this.source = this.empty;
		Tag<T> tag = this.empty.getEmptyTag();
		this.wrappers.forEach(wrapper -> wrapper.rebind(resourceLocation -> tag));
	}

	public void reset(TagCollection<T> tagCollection) {
		this.source = tagCollection;
		this.wrappers.forEach(wrapper -> wrapper.rebind(tagCollection::getTag));
	}

	public TagCollection<T> getAllTags() {
		return this.source;
	}

	public Set<ResourceLocation> getMissingTags(TagCollection<T> tagCollection) {
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