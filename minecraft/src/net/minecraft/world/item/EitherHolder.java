package net.minecraft.world.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record EitherHolder<T>(Optional<Holder<T>> holder, ResourceKey<T> key) {
	public EitherHolder(Holder<T> holder) {
		this(Optional.of(holder), (ResourceKey<T>)holder.unwrapKey().orElseThrow());
	}

	public EitherHolder(ResourceKey<T> resourceKey) {
		this(Optional.empty(), resourceKey);
	}

	public static <T> Codec<EitherHolder<T>> codec(ResourceKey<Registry<T>> resourceKey, Codec<Holder<T>> codec) {
		return Codec.either(
				codec, ResourceKey.codec(resourceKey).comapFlatMap(resourceKeyx -> DataResult.error(() -> "Cannot parse as key without registry"), Function.identity())
			)
			.xmap(EitherHolder::fromEither, EitherHolder::asEither);
	}

	public static <T> StreamCodec<RegistryFriendlyByteBuf, EitherHolder<T>> streamCodec(
		ResourceKey<Registry<T>> resourceKey, StreamCodec<RegistryFriendlyByteBuf, Holder<T>> streamCodec
	) {
		return StreamCodec.composite(ByteBufCodecs.either(streamCodec, ResourceKey.streamCodec(resourceKey)), EitherHolder::asEither, EitherHolder::fromEither);
	}

	public Either<Holder<T>, ResourceKey<T>> asEither() {
		return (Either<Holder<T>, ResourceKey<T>>)this.holder.map(Either::left).orElseGet(() -> Either.right(this.key));
	}

	public static <T> EitherHolder<T> fromEither(Either<Holder<T>, ResourceKey<T>> either) {
		return either.map(EitherHolder::new, EitherHolder::new);
	}

	public Optional<T> unwrap(Registry<T> registry) {
		return this.holder.map(Holder::value).or(() -> registry.getOptional(this.key));
	}

	public Optional<Holder<T>> unwrap(HolderLookup.Provider provider) {
		return this.holder.or(() -> provider.lookupOrThrow(this.key.registryKey()).get(this.key));
	}
}
