package net.minecraft.resources;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

public class HolderSetCodec<E> implements Codec<HolderSet<E>> {
	private final ResourceKey<? extends Registry<E>> registryKey;
	private final Codec<Holder<E>> elementCodec;
	private final Codec<List<Holder<E>>> homogenousListCodec;
	private final Codec<Either<TagKey<E>, List<Holder<E>>>> registryAwareCodec;

	private static <E> Codec<List<Holder<E>>> homogenousList(Codec<Holder<E>> codec, boolean bl) {
		Codec<List<Holder<E>>> codec2 = ExtraCodecs.validate(codec.listOf(), ExtraCodecs.ensureHomogenous(Holder::kind));
		return bl
			? codec2
			: Codec.either(codec2, codec)
				.xmap(either -> either.map(list -> list, List::of), list -> list.size() == 1 ? Either.right((Holder)list.get(0)) : Either.left(list));
	}

	public static <E> Codec<HolderSet<E>> create(ResourceKey<? extends Registry<E>> resourceKey, Codec<Holder<E>> codec, boolean bl) {
		return new HolderSetCodec<>(resourceKey, codec, bl);
	}

	private HolderSetCodec(ResourceKey<? extends Registry<E>> resourceKey, Codec<Holder<E>> codec, boolean bl) {
		this.registryKey = resourceKey;
		this.elementCodec = codec;
		this.homogenousListCodec = homogenousList(codec, bl);
		this.registryAwareCodec = Codec.either(TagKey.hashedCodec(resourceKey), this.homogenousListCodec);
	}

	@Override
	public <T> DataResult<Pair<HolderSet<E>, T>> decode(DynamicOps<T> dynamicOps, T object) {
		if (dynamicOps instanceof RegistryOps<T> registryOps) {
			Optional<HolderGetter<E>> optional = registryOps.getter(this.registryKey);
			if (optional.isPresent()) {
				HolderGetter<E> holderGetter = (HolderGetter<E>)optional.get();
				return this.registryAwareCodec.decode(dynamicOps, object).map(pair -> pair.mapFirst(either -> either.map(holderGetter::getOrThrow, HolderSet::direct)));
			}
		}

		return this.decodeWithoutRegistry(dynamicOps, object);
	}

	public <T> DataResult<T> encode(HolderSet<E> holderSet, DynamicOps<T> dynamicOps, T object) {
		if (dynamicOps instanceof RegistryOps<T> registryOps) {
			Optional<HolderOwner<E>> optional = registryOps.owner(this.registryKey);
			if (optional.isPresent()) {
				if (!holderSet.canSerializeIn((HolderOwner<E>)optional.get())) {
					return DataResult.error(() -> "HolderSet " + holderSet + " is not valid in current registry set");
				}

				return this.registryAwareCodec.encode(holderSet.unwrap().mapRight(List::copyOf), dynamicOps, object);
			}
		}

		return this.encodeWithoutRegistry(holderSet, dynamicOps, object);
	}

	private <T> DataResult<Pair<HolderSet<E>, T>> decodeWithoutRegistry(DynamicOps<T> dynamicOps, T object) {
		return this.elementCodec.listOf().decode(dynamicOps, object).flatMap(pair -> {
			List<Holder.Direct<E>> list = new ArrayList();

			for (Holder<E> holder : (List)pair.getFirst()) {
				if (!(holder instanceof Holder.Direct<E> direct)) {
					return DataResult.error(() -> "Can't decode element " + holder + " without registry");
				}

				list.add(direct);
			}

			return DataResult.success(new Pair<>(HolderSet.direct(list), pair.getSecond()));
		});
	}

	private <T> DataResult<T> encodeWithoutRegistry(HolderSet<E> holderSet, DynamicOps<T> dynamicOps, T object) {
		return this.homogenousListCodec.encode(holderSet.stream().toList(), dynamicOps, object);
	}
}
