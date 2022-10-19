/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

@FunctionalInterface
public interface ResourceProvider {
    public Optional<Resource> getResource(ResourceLocation var1);

    default public Resource getResourceOrThrow(ResourceLocation resourceLocation) throws FileNotFoundException {
        return this.getResource(resourceLocation).orElseThrow(() -> new FileNotFoundException(resourceLocation.toString()));
    }

    default public InputStream open(ResourceLocation resourceLocation) throws IOException {
        return this.getResourceOrThrow(resourceLocation).open();
    }

    default public BufferedReader openAsReader(ResourceLocation resourceLocation) throws IOException {
        return this.getResourceOrThrow(resourceLocation).openAsReader();
    }

    public static ResourceProvider fromMap(Map<ResourceLocation, Resource> map) {
        return resourceLocation -> Optional.ofNullable((Resource)map.get(resourceLocation));
    }
}

