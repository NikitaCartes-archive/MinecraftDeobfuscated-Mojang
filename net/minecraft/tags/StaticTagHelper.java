/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SetTag;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagContainer;
import org.jetbrains.annotations.Nullable;

public class StaticTagHelper<T> {
    private final ResourceKey<? extends Registry<T>> key;
    private final String directory;
    private TagCollection<T> source = TagCollection.empty();
    private final List<Wrapper<T>> wrappers = Lists.newArrayList();

    public StaticTagHelper(ResourceKey<? extends Registry<T>> resourceKey, String string) {
        this.key = resourceKey;
        this.directory = string;
    }

    public Tag.Named<T> bind(String string) {
        Wrapper wrapper = new Wrapper(new ResourceLocation(string));
        this.wrappers.add(wrapper);
        return wrapper;
    }

    public void resetToEmpty() {
        this.source = TagCollection.empty();
        SetTag tag = SetTag.empty();
        this.wrappers.forEach(wrapper -> wrapper.rebind(resourceLocation -> tag));
    }

    public void reset(TagContainer tagContainer) {
        TagCollection tagCollection = tagContainer.getOrEmpty(this.key);
        this.source = tagCollection;
        this.wrappers.forEach(wrapper -> wrapper.rebind(tagCollection::getTag));
    }

    public TagCollection<T> getAllTags() {
        return this.source;
    }

    public Set<ResourceLocation> getMissingTags(TagContainer tagContainer) {
        TagCollection tagCollection = tagContainer.getOrEmpty(this.key);
        Set set = this.wrappers.stream().map(Wrapper::getName).collect(Collectors.toSet());
        ImmutableSet<ResourceLocation> immutableSet = ImmutableSet.copyOf(tagCollection.getAvailableTags());
        return Sets.difference(set, immutableSet);
    }

    public ResourceKey<? extends Registry<T>> getKey() {
        return this.key;
    }

    public String getDirectory() {
        return this.directory;
    }

    protected void addToCollection(TagContainer.Builder builder) {
        builder.add(this.key, TagCollection.of(this.wrappers.stream().collect(Collectors.toMap(Tag.Named::getName, wrapper -> wrapper))));
    }

    static class Wrapper<T>
    implements Tag.Named<T> {
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
            }
            return this.tag;
        }

        void rebind(Function<ResourceLocation, Tag<T>> function) {
            this.tag = function.apply(this.name);
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

