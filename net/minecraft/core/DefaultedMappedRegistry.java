/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultedMappedRegistry<T>
extends MappedRegistry<T>
implements DefaultedRegistry<T> {
    private final ResourceLocation defaultKey;
    private Holder.Reference<T> defaultValue;

    public DefaultedMappedRegistry(String string, ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, boolean bl) {
        super(resourceKey, lifecycle, bl);
        this.defaultKey = new ResourceLocation(string);
    }

    @Override
    public Holder.Reference<T> registerMapping(int i, ResourceKey<T> resourceKey, T object, Lifecycle lifecycle) {
        Holder reference = super.registerMapping(i, (ResourceKey)resourceKey, (Object)object, lifecycle);
        if (this.defaultKey.equals(resourceKey.location())) {
            this.defaultValue = reference;
        }
        return reference;
    }

    @Override
    public int getId(@Nullable T object) {
        int i = super.getId(object);
        return i == -1 ? super.getId(this.defaultValue.value()) : i;
    }

    @Override
    @NotNull
    public ResourceLocation getKey(T object) {
        ResourceLocation resourceLocation = super.getKey(object);
        return resourceLocation == null ? this.defaultKey : resourceLocation;
    }

    @Override
    @NotNull
    public T get(@Nullable ResourceLocation resourceLocation) {
        Object object = super.get(resourceLocation);
        return object == null ? this.defaultValue.value() : object;
    }

    @Override
    public Optional<T> getOptional(@Nullable ResourceLocation resourceLocation) {
        return Optional.ofNullable(super.get(resourceLocation));
    }

    @Override
    @NotNull
    public T byId(int i) {
        Object object = super.byId(i);
        return object == null ? this.defaultValue.value() : object;
    }

    @Override
    public Optional<Holder.Reference<T>> getRandom(RandomSource randomSource) {
        return super.getRandom(randomSource).or(() -> Optional.of(this.defaultValue));
    }

    @Override
    public ResourceLocation getDefaultKey() {
        return this.defaultKey;
    }

    @Override
    public /* synthetic */ Holder registerMapping(int i, ResourceKey resourceKey, Object object, Lifecycle lifecycle) {
        return this.registerMapping(i, resourceKey, object, lifecycle);
    }
}

