/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class RegistryFileCodec<E>
implements Codec<Holder<E>> {
    private final ResourceKey<? extends Registry<E>> registryKey;
    private final Codec<E> elementCodec;
    private final boolean allowInline;

    public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
        return RegistryFileCodec.create(resourceKey, codec, true);
    }

    public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, boolean bl) {
        return new RegistryFileCodec<E>(resourceKey, codec, bl);
    }

    private RegistryFileCodec(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, boolean bl) {
        this.registryKey = resourceKey;
        this.elementCodec = codec;
        this.allowInline = bl;
    }

    @Override
    public <T> DataResult<T> encode(Holder<E> holder, DynamicOps<T> dynamicOps, T object) {
        RegistryOps registryOps;
        Optional optional;
        if (dynamicOps instanceof RegistryOps && (optional = (registryOps = (RegistryOps)dynamicOps).registry(this.registryKey)).isPresent()) {
            if (!holder.isValidInRegistry(optional.get())) {
                return DataResult.error("Element " + holder + " is not valid in current registry set");
            }
            return holder.unwrap().map(resourceKey -> ResourceLocation.CODEC.encode(resourceKey.location(), dynamicOps, object), object2 -> this.elementCodec.encode(object2, dynamicOps, object));
        }
        return this.elementCodec.encode(holder.value(), dynamicOps, object);
    }

    @Override
    public <T> DataResult<Pair<Holder<E>, T>> decode(DynamicOps<T> dynamicOps, T object) {
        if (dynamicOps instanceof RegistryOps) {
            RegistryOps registryOps = (RegistryOps)dynamicOps;
            Optional optional = registryOps.registry(this.registryKey);
            if (optional.isEmpty()) {
                return DataResult.error("Registry does not exist: " + this.registryKey);
            }
            Registry registry = optional.get();
            DataResult dataResult = ResourceLocation.CODEC.decode(dynamicOps, object);
            if (dataResult.result().isEmpty()) {
                if (!this.allowInline) {
                    return DataResult.error("Inline definitions not allowed here");
                }
                return this.elementCodec.decode(dynamicOps, object).map((? super R pair) -> pair.mapFirst(Holder::direct));
            }
            Pair pair2 = dataResult.result().get();
            ResourceKey resourceKey = ResourceKey.create(this.registryKey, (ResourceLocation)pair2.getFirst());
            Optional<RegistryLoader.Bound> optional2 = registryOps.registryLoader();
            if (optional2.isPresent()) {
                return optional2.get().overrideElementFromResources(this.registryKey, this.elementCodec, resourceKey, registryOps.getAsJson()).map((? super R holder) -> Pair.of(holder, pair2.getSecond()));
            }
            Holder holder2 = registry.getOrCreateHolder(resourceKey);
            return DataResult.success(Pair.of(holder2, pair2.getSecond()), Lifecycle.stable());
        }
        return this.elementCodec.decode(dynamicOps, object).map((? super R pair) -> pair.mapFirst(Holder::direct));
    }

    public String toString() {
        return "RegistryFileCodec[" + this.registryKey + " " + this.elementCodec + "]";
    }

    @Override
    public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
        return this.encode((Holder)object, dynamicOps, object2);
    }
}

