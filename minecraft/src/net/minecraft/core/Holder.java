package net.minecraft.core;

import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public interface Holder<T> {
	T value();

	boolean isBound();

	boolean is(ResourceLocation resourceLocation);

	boolean is(ResourceKey<T> resourceKey);

	boolean is(Predicate<ResourceKey<T>> predicate);

	boolean is(TagKey<T> tagKey);

	Stream<TagKey<T>> tags();

	Either<ResourceKey<T>, T> unwrap();

	Optional<ResourceKey<T>> unwrapKey();

	Holder.Kind kind();

	boolean canSerializeIn(HolderOwner<T> holderOwner);

	static <T> Holder<T> direct(T object) {
		return new Holder.Direct<>(object);
	}

	public static record Direct<T>(T value) implements Holder<T> {
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
		public Holder.Kind kind() {
			return Holder.Kind.DIRECT;
		}

		public String toString() {
			return "Direct{" + this.value + "}";
		}

		@Override
		public boolean canSerializeIn(HolderOwner<T> holderOwner) {
			return true;
		}

		@Override
		public Stream<TagKey<T>> tags() {
			return Stream.of();
		}
	}

	public static enum Kind {
		REFERENCE,
		DIRECT;
	}

	public static class Reference<T> implements Holder<T> {
		private final HolderOwner<T> owner;
		private Set<TagKey<T>> tags = Set.of();
		private final Holder.Reference.Type type;
		@Nullable
		private ResourceKey<T> key;
		@Nullable
		private T value;

		private Reference(Holder.Reference.Type type, HolderOwner<T> holderOwner, @Nullable ResourceKey<T> resourceKey, @Nullable T object) {
			this.owner = holderOwner;
			this.type = type;
			this.key = resourceKey;
			this.value = object;
		}

		public static <T> Holder.Reference<T> createStandAlone(HolderOwner<T> holderOwner, ResourceKey<T> resourceKey) {
			return new Holder.Reference<>(Holder.Reference.Type.STAND_ALONE, holderOwner, resourceKey, null);
		}

		@Deprecated
		public static <T> Holder.Reference<T> createIntrusive(HolderOwner<T> holderOwner, @Nullable T object) {
			return new Holder.Reference<>(Holder.Reference.Type.INTRUSIVE, holderOwner, null, object);
		}

		public ResourceKey<T> key() {
			if (this.key == null) {
				throw new IllegalStateException("Trying to access unbound value '" + this.value + "' from registry " + this.owner);
			} else {
				return this.key;
			}
		}

		@Override
		public T value() {
			if (this.value == null) {
				throw new IllegalStateException("Trying to access unbound value '" + this.key + "' from registry " + this.owner);
			} else {
				return this.value;
			}
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
		public Holder.Kind kind() {
			return Holder.Kind.REFERENCE;
		}

		@Override
		public boolean isBound() {
			return this.key != null && this.value != null;
		}

		void bindKey(ResourceKey<T> resourceKey) {
			if (this.key != null && resourceKey != this.key) {
				throw new IllegalStateException("Can't change holder key: existing=" + this.key + ", new=" + resourceKey);
			} else {
				this.key = resourceKey;
			}
		}

		void bindValue(T object) {
			if (this.type == Holder.Reference.Type.INTRUSIVE && this.value != object) {
				throw new IllegalStateException("Can't change holder " + this.key + " value: existing=" + this.value + ", new=" + object);
			} else {
				this.value = object;
			}
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
}
