/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.Random;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultedRegistry<T>
extends MappedRegistry<T> {
    private final ResourceLocation defaultKey;
    private T defaultValue;

    public DefaultedRegistry(String string, ResourceKey<Registry<T>> resourceKey, Lifecycle lifecycle) {
        super(resourceKey, lifecycle);
        this.defaultKey = new ResourceLocation(string);
    }

    @Override
    public <V extends T> V registerMapping(int i, ResourceKey<T> resourceKey, V object) {
        if (this.defaultKey.equals(resourceKey.location())) {
            this.defaultValue = object;
        }
        return super.registerMapping(i, resourceKey, object);
    }

    @Override
    public int getId(@Nullable T object) {
        int i = super.getId(object);
        return i == -1 ? super.getId(this.defaultValue) : i;
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
        return object == null ? this.defaultValue : object;
    }

    @Override
    @NotNull
    public T byId(int i) {
        Object object = super.byId(i);
        return object == null ? this.defaultValue : object;
    }

    @Override
    @NotNull
    public T getRandom(Random random) {
        Object object = super.getRandom(random);
        return object == null ? this.defaultValue : object;
    }

    public ResourceLocation getDefaultKey() {
        return this.defaultKey;
    }
}

