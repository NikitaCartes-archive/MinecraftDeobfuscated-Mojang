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
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class RegistryFixedCodec<E>
implements Codec<Holder<E>> {
    private final ResourceKey<? extends Registry<E>> registryKey;

    public static <E> RegistryFixedCodec<E> create(ResourceKey<? extends Registry<E>> resourceKey) {
        return new RegistryFixedCodec<E>(resourceKey);
    }

    private RegistryFixedCodec(ResourceKey<? extends Registry<E>> resourceKey) {
        this.registryKey = resourceKey;
    }

    @Override
    public <T> DataResult<T> encode(Holder<E> holder, DynamicOps<T> dynamicOps, T object2) {
        RegistryOps registryOps;
        Optional optional;
        if (dynamicOps instanceof RegistryOps && (optional = (registryOps = (RegistryOps)dynamicOps).owner(this.registryKey)).isPresent()) {
            if (!holder.canSerializeIn(optional.get())) {
                return DataResult.error("Element " + holder + " is not valid in current registry set");
            }
            return holder.unwrap().map(resourceKey -> ResourceLocation.CODEC.encode(resourceKey.location(), dynamicOps, object2), object -> DataResult.error("Elements from registry " + this.registryKey + " can't be serialized to a value"));
        }
        return DataResult.error("Can't access registry " + this.registryKey);
    }

    @Override
    public <T> DataResult<Pair<Holder<E>, T>> decode(DynamicOps<T> dynamicOps, T object) {
        RegistryOps registryOps;
        Optional optional;
        if (dynamicOps instanceof RegistryOps && (optional = (registryOps = (RegistryOps)dynamicOps).getter(this.registryKey)).isPresent()) {
            return ResourceLocation.CODEC.decode(dynamicOps, object).flatMap((? super R pair) -> {
                ResourceLocation resourceLocation = (ResourceLocation)pair.getFirst();
                return ((HolderGetter)optional.get()).get(ResourceKey.create(this.registryKey, resourceLocation)).map(DataResult::success).orElseGet(() -> DataResult.error("Failed to get element " + resourceLocation)).map((? super R reference) -> Pair.of(reference, pair.getSecond())).setLifecycle(Lifecycle.stable());
            });
        }
        return DataResult.error("Can't access registry " + this.registryKey);
    }

    public String toString() {
        return "RegistryFixedCodec[" + this.registryKey + "]";
    }

    @Override
    public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
        return this.encode((Holder)object, dynamicOps, object2);
    }
}

