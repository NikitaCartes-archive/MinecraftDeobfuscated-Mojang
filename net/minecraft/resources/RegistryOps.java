/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.DelegatingOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;

public class RegistryOps<T>
extends DelegatingOps<T> {
    private final RegistryInfoLookup lookupProvider;

    private static RegistryInfoLookup memoizeLookup(final RegistryInfoLookup registryInfoLookup) {
        return new RegistryInfoLookup(){
            private final Map<ResourceKey<? extends Registry<?>>, Optional<? extends RegistryInfo<?>>> lookups = new HashMap();

            @Override
            public <T> Optional<RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                return this.lookups.computeIfAbsent(resourceKey, registryInfoLookup::lookup);
            }
        };
    }

    public static <T> RegistryOps<T> create(DynamicOps<T> dynamicOps, final HolderLookup.Provider provider) {
        return RegistryOps.create(dynamicOps, RegistryOps.memoizeLookup(new RegistryInfoLookup(){

            public <E> Optional<RegistryInfo<E>> lookup(ResourceKey<? extends Registry<? extends E>> resourceKey) {
                return provider.lookup(resourceKey).map(registryLookup -> new RegistryInfo(registryLookup, registryLookup, registryLookup.registryLifecycle()));
            }
        }));
    }

    public static <T> RegistryOps<T> create(DynamicOps<T> dynamicOps, RegistryInfoLookup registryInfoLookup) {
        return new RegistryOps<T>(dynamicOps, registryInfoLookup);
    }

    private RegistryOps(DynamicOps<T> dynamicOps, RegistryInfoLookup registryInfoLookup) {
        super(dynamicOps);
        this.lookupProvider = registryInfoLookup;
    }

    public <E> Optional<HolderOwner<E>> owner(ResourceKey<? extends Registry<? extends E>> resourceKey) {
        return this.lookupProvider.lookup(resourceKey).map(RegistryInfo::owner);
    }

    public <E> Optional<HolderGetter<E>> getter(ResourceKey<? extends Registry<? extends E>> resourceKey) {
        return this.lookupProvider.lookup(resourceKey).map(RegistryInfo::getter);
    }

    public static <E, O> RecordCodecBuilder<O, HolderGetter<E>> retrieveGetter(ResourceKey<? extends Registry<? extends E>> resourceKey) {
        return ExtraCodecs.retrieveContext(dynamicOps -> {
            if (dynamicOps instanceof RegistryOps) {
                RegistryOps registryOps = (RegistryOps)dynamicOps;
                return registryOps.lookupProvider.lookup(resourceKey).map(registryInfo -> DataResult.success(registryInfo.getter(), registryInfo.elementsLifecycle())).orElseGet(() -> DataResult.error("Unknown registry: " + resourceKey));
            }
            return DataResult.error("Not a registry ops");
        }).forGetter(object -> null);
    }

    public static <E, O> RecordCodecBuilder<O, Holder.Reference<E>> retrieveElement(ResourceKey<E> resourceKey) {
        ResourceKey resourceKey2 = ResourceKey.createRegistryKey(resourceKey.registry());
        return ExtraCodecs.retrieveContext(dynamicOps -> {
            if (dynamicOps instanceof RegistryOps) {
                RegistryOps registryOps = (RegistryOps)dynamicOps;
                return registryOps.lookupProvider.lookup(resourceKey2).flatMap(registryInfo -> registryInfo.getter().get(resourceKey)).map(DataResult::success).orElseGet(() -> DataResult.error("Can't find value: " + resourceKey));
            }
            return DataResult.error("Not a registry ops");
        }).forGetter(object -> null);
    }

    public static interface RegistryInfoLookup {
        public <T> Optional<RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> var1);
    }

    public record RegistryInfo<T>(HolderOwner<T> owner, HolderGetter<T> getter, Lifecycle elementsLifecycle) {
    }
}

