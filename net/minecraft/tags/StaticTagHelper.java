/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import org.jetbrains.annotations.Nullable;

public class StaticTagHelper<T> {
    private final TagCollection<T> empty = new TagCollection(resourceLocation -> Optional.empty(), "", "");
    private TagCollection<T> source = this.empty;
    private final List<Wrapper<T>> wrappers = Lists.newArrayList();

    public Tag.Named<T> bind(String string) {
        Wrapper wrapper = new Wrapper(new ResourceLocation(string));
        this.wrappers.add(wrapper);
        return wrapper;
    }

    @Environment(value=EnvType.CLIENT)
    public void resetToEmpty() {
        this.source = this.empty;
        Tag tag = this.empty.getEmptyTag();
        this.wrappers.forEach(wrapper -> wrapper.rebind(resourceLocation -> tag));
    }

    public void reset(TagCollection<T> tagCollection) {
        this.source = tagCollection;
        this.wrappers.forEach(wrapper -> wrapper.rebind(tagCollection::getTag));
    }

    public TagCollection<T> getAllTags() {
        return this.source;
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

