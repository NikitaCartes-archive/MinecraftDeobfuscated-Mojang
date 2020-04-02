/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import org.jetbrains.annotations.Nullable;

public class StaticTagHelper<T> {
    private TagCollection<T> source = new TagCollection(resourceLocation -> Optional.empty(), "", "");
    private final List<Wrapper<T>> wrappers = Lists.newArrayList();

    public Tag.Named<T> bind(String string) {
        Wrapper wrapper = new Wrapper(new ResourceLocation(string));
        this.wrappers.add(wrapper);
        return wrapper;
    }

    public void reset(TagCollection<T> tagCollection) {
        this.source = tagCollection;
        this.wrappers.forEach(wrapper -> wrapper.rebind(tagCollection));
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

        void rebind(TagCollection<T> tagCollection) {
            this.tag = tagCollection.getTag(this.name);
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

