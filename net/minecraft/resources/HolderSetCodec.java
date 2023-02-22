/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

public class HolderSetCodec<E>
implements Codec<HolderSet<E>> {
    private final ResourceKey<? extends Registry<E>> registryKey;
    private final Codec<Holder<E>> elementCodec;
    private final Codec<List<Holder<E>>> homogenousListCodec;
    private final Codec<Either<TagKey<E>, List<Holder<E>>>> registryAwareCodec;

    private static <E> Codec<List<Holder<E>>> homogenousList(Codec<Holder<E>> codec, boolean bl) {
        Codec<List<Holder<E>>> codec2 = ExtraCodecs.validate(codec.listOf(), ExtraCodecs.ensureHomogenous(Holder::kind));
        if (bl) {
            return codec2;
        }
        return Codec.either(codec2, codec).xmap(either -> either.map(list -> list, List::of), list -> list.size() == 1 ? Either.right((Holder)list.get(0)) : Either.left(list));
    }

    public static <E> Codec<HolderSet<E>> create(ResourceKey<? extends Registry<E>> resourceKey, Codec<Holder<E>> codec, boolean bl) {
        return new HolderSetCodec<E>(resourceKey, codec, bl);
    }

    private HolderSetCodec(ResourceKey<? extends Registry<E>> resourceKey, Codec<Holder<E>> codec, boolean bl) {
        this.registryKey = resourceKey;
        this.elementCodec = codec;
        this.homogenousListCodec = HolderSetCodec.homogenousList(codec, bl);
        this.registryAwareCodec = Codec.either(TagKey.hashedCodec(resourceKey), this.homogenousListCodec);
    }

    @Override
    public <T> DataResult<Pair<HolderSet<E>, T>> decode(DynamicOps<T> dynamicOps, T object) {
        RegistryOps registryOps;
        Optional optional;
        if (dynamicOps instanceof RegistryOps && (optional = (registryOps = (RegistryOps)dynamicOps).getter(this.registryKey)).isPresent()) {
            HolderGetter holderGetter = optional.get();
            return this.registryAwareCodec.decode(dynamicOps, object).map((? super R pair) -> pair.mapFirst(either -> either.map(holderGetter::getOrThrow, HolderSet::direct)));
        }
        return this.decodeWithoutRegistry(dynamicOps, object);
    }

    @Override
    public <T> DataResult<T> encode(HolderSet<E> holderSet, DynamicOps<T> dynamicOps, T object) {
        RegistryOps registryOps;
        Optional optional;
        if (dynamicOps instanceof RegistryOps && (optional = (registryOps = (RegistryOps)dynamicOps).owner(this.registryKey)).isPresent()) {
            if (!holderSet.canSerializeIn(optional.get())) {
                return DataResult.error(() -> "HolderSet " + holderSet + " is not valid in current registry set");
            }
            return this.registryAwareCodec.encode(holderSet.unwrap().mapRight(List::copyOf), dynamicOps, object);
        }
        return this.encodeWithoutRegistry(holderSet, dynamicOps, object);
    }

    private <T> DataResult<Pair<HolderSet<E>, T>> decodeWithoutRegistry(DynamicOps<T> dynamicOps, T object) {
        return this.elementCodec.listOf().decode(dynamicOps, object).flatMap((? super R pair) -> {
            ArrayList<Holder.Direct> list = new ArrayList<Holder.Direct>();
            for (Holder holder : (List)pair.getFirst()) {
                if (holder instanceof Holder.Direct) {
                    Holder.Direct direct = (Holder.Direct)holder;
                    list.add(direct);
                    continue;
                }
                return DataResult.error(() -> "Can't decode element " + holder + " without registry");
            }
            return DataResult.success(new Pair(HolderSet.direct(list), pair.getSecond()));
        });
    }

    private <T> DataResult<T> encodeWithoutRegistry(HolderSet<E> holderSet, DynamicOps<T> dynamicOps, T object) {
        return this.homogenousListCodec.encode(holderSet.stream().toList(), dynamicOps, object);
    }

    @Override
    public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
        return this.encode((HolderSet)object, dynamicOps, object2);
    }
}

