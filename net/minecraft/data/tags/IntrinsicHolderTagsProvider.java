/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

public abstract class IntrinsicHolderTagsProvider<T>
extends TagsProvider<T> {
    private final Function<T, ResourceKey<T>> keyExtractor;

    public IntrinsicHolderTagsProvider(PackOutput packOutput, ResourceKey<? extends Registry<T>> resourceKey, CompletableFuture<HolderLookup.Provider> completableFuture, Function<T, ResourceKey<T>> function) {
        super(packOutput, resourceKey, completableFuture);
        this.keyExtractor = function;
    }

    @Override
    protected IntrinsicTagAppender<T> tag(TagKey<T> tagKey) {
        TagBuilder tagBuilder = this.getOrCreateRawBuilder(tagKey);
        return new IntrinsicTagAppender<T>(tagBuilder, this.keyExtractor);
    }

    @Override
    protected /* synthetic */ TagsProvider.TagAppender tag(TagKey tagKey) {
        return this.tag(tagKey);
    }

    protected static class IntrinsicTagAppender<T>
    extends TagsProvider.TagAppender<T> {
        private final Function<T, ResourceKey<T>> keyExtractor;

        IntrinsicTagAppender(TagBuilder tagBuilder, Function<T, ResourceKey<T>> function) {
            super(tagBuilder);
            this.keyExtractor = function;
        }

        @Override
        public IntrinsicTagAppender<T> addTag(TagKey<T> tagKey) {
            super.addTag(tagKey);
            return this;
        }

        public final IntrinsicTagAppender<T> add(T object) {
            ((TagsProvider.TagAppender)this).add(this.keyExtractor.apply(object));
            return this;
        }

        @SafeVarargs
        public final IntrinsicTagAppender<T> add(T ... objects) {
            Stream.of(objects).map(this.keyExtractor).forEach(this::add);
            return this;
        }

        @Override
        public /* synthetic */ TagsProvider.TagAppender addTag(TagKey tagKey) {
            return this.addTag(tagKey);
        }
    }
}

