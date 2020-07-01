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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SetTag;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagContainer;
import org.jetbrains.annotations.Nullable;

public class StaticTagHelper<T> {
    private TagCollection<T> source = TagCollection.empty();
    private final List<Wrapper<T>> wrappers = Lists.newArrayList();
    private final Function<TagContainer, TagCollection<T>> collectionGetter;

    public StaticTagHelper(Function<TagContainer, TagCollection<T>> function) {
        this.collectionGetter = function;
    }

    public Tag.Named<T> bind(String string) {
        Wrapper wrapper = new Wrapper(new ResourceLocation(string));
        this.wrappers.add(wrapper);
        return wrapper;
    }

    @Environment(value=EnvType.CLIENT)
    public void resetToEmpty() {
        this.source = TagCollection.empty();
        SetTag tag = SetTag.empty();
        this.wrappers.forEach(wrapper -> wrapper.rebind(resourceLocation -> tag));
    }

    public void reset(TagContainer tagContainer) {
        TagCollection tagCollection = this.collectionGetter.apply(tagContainer);
        this.source = tagCollection;
        this.wrappers.forEach(wrapper -> wrapper.rebind(tagCollection::getTag));
    }

    public TagCollection<T> getAllTags() {
        return this.source;
    }

    public List<? extends Tag<T>> getWrappers() {
        return this.wrappers;
    }

    public Set<ResourceLocation> getMissingTags(TagContainer tagContainer) {
        TagCollection<T> tagCollection = this.collectionGetter.apply(tagContainer);
        Set set = this.wrappers.stream().map(Wrapper::getName).collect(Collectors.toSet());
        ImmutableSet<ResourceLocation> immutableSet = ImmutableSet.copyOf(tagCollection.getAvailableTags());
        return Sets.difference(set, immutableSet);
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

