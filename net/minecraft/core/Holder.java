/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

public interface Holder<T> {
    public T value();

    public boolean isBound();

    public boolean is(ResourceLocation var1);

    public boolean is(ResourceKey<T> var1);

    public boolean is(Predicate<ResourceKey<T>> var1);

    public boolean is(TagKey<T> var1);

    public Stream<TagKey<T>> tags();

    public Either<ResourceKey<T>, T> unwrap();

    public Optional<ResourceKey<T>> unwrapKey();

    public Kind kind();

    public boolean canSerializeIn(HolderOwner<T> var1);

    public static <T> Holder<T> direct(T object) {
        return new Direct<T>(object);
    }

    public record Direct<T>(T value) implements Holder<T>
    {
        @Override
        public boolean isBound() {
            return true;
        }

        @Override
        public boolean is(ResourceLocation resourceLocation) {
            return false;
        }

        @Override
        public boolean is(ResourceKey<T> resourceKey) {
            return false;
        }

        @Override
        public boolean is(TagKey<T> tagKey) {
            return false;
        }

        @Override
        public boolean is(Predicate<ResourceKey<T>> predicate) {
            return false;
        }

        @Override
        public Either<ResourceKey<T>, T> unwrap() {
            return Either.right(this.value);
        }

        @Override
        public Optional<ResourceKey<T>> unwrapKey() {
            return Optional.empty();
        }

        @Override
        public Kind kind() {
            return Kind.DIRECT;
        }

        @Override
        public String toString() {
            return "Direct{" + this.value + "}";
        }

        @Override
        public boolean canSerializeIn(HolderOwner<T> holderOwner) {
            return true;
        }

        @Override
        public Stream<TagKey<T>> tags() {
            return Stream.of(new TagKey[0]);
        }
    }

    public static class Reference<T>
    implements Holder<T> {
        private final HolderOwner<T> owner;
        private Set<TagKey<T>> tags = Set.of();
        private final Type type;
        @Nullable
        private ResourceKey<T> key;
        @Nullable
        private T value;

        private Reference(Type type, HolderOwner<T> holderOwner, @Nullable ResourceKey<T> resourceKey, @Nullable T object) {
            this.owner = holderOwner;
            this.type = type;
            this.key = resourceKey;
            this.value = object;
        }

        public static <T> Reference<T> createStandAlone(HolderOwner<T> holderOwner, ResourceKey<T> resourceKey) {
            return new Reference<Object>(Type.STAND_ALONE, holderOwner, resourceKey, null);
        }

        @Deprecated
        public static <T> Reference<T> createIntrusive(HolderOwner<T> holderOwner, @Nullable T object) {
            return new Reference<T>(Type.INTRUSIVE, holderOwner, null, object);
        }

        public ResourceKey<T> key() {
            if (this.key == null) {
                throw new IllegalStateException("Trying to access unbound value '" + this.value + "' from registry " + this.owner);
            }
            return this.key;
        }

        @Override
        public T value() {
            if (this.value == null) {
                throw new IllegalStateException("Trying to access unbound value '" + this.key + "' from registry " + this.owner);
            }
            return this.value;
        }

        @Override
        public boolean is(ResourceLocation resourceLocation) {
            return this.key().location().equals(resourceLocation);
        }

        @Override
        public boolean is(ResourceKey<T> resourceKey) {
            return this.key() == resourceKey;
        }

        @Override
        public boolean is(TagKey<T> tagKey) {
            return this.tags.contains(tagKey);
        }

        @Override
        public boolean is(Predicate<ResourceKey<T>> predicate) {
            return predicate.test(this.key());
        }

        @Override
        public boolean canSerializeIn(HolderOwner<T> holderOwner) {
            return this.owner.canSerializeIn(holderOwner);
        }

        @Override
        public Either<ResourceKey<T>, T> unwrap() {
            return Either.left(this.key());
        }

        @Override
        public Optional<ResourceKey<T>> unwrapKey() {
            return Optional.of(this.key());
        }

        @Override
        public Kind kind() {
            return Kind.REFERENCE;
        }

        @Override
        public boolean isBound() {
            return this.key != null && this.value != null;
        }

        void bindKey(ResourceKey<T> resourceKey) {
            if (this.key != null && resourceKey != this.key) {
                throw new IllegalStateException("Can't change holder key: existing=" + this.key + ", new=" + resourceKey);
            }
            this.key = resourceKey;
        }

        void bindValue(T object) {
            if (this.type == Type.INTRUSIVE && this.value != object) {
                throw new IllegalStateException("Can't change holder " + this.key + " value: existing=" + this.value + ", new=" + object);
            }
            this.value = object;
        }

        void bindTags(Collection<TagKey<T>> collection) {
            this.tags = Set.copyOf(collection);
        }

        @Override
        public Stream<TagKey<T>> tags() {
            return this.tags.stream();
        }

        public String toString() {
            return "Reference{" + this.key + "=" + this.value + "}";
        }

        static enum Type {
            STAND_ALONE,
            INTRUSIVE;

        }
    }

    public static enum Kind {
        REFERENCE,
        DIRECT;

    }
}

