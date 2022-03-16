/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.server.packs.resources.ResourceThunk;

public interface ResourceManager
extends ResourceProvider {
    public Set<String> getNamespaces();

    public boolean hasResource(ResourceLocation var1);

    public List<ResourceThunk> getResourceStack(ResourceLocation var1) throws IOException;

    public Map<ResourceLocation, ResourceThunk> listResources(String var1, Predicate<ResourceLocation> var2);

    public Map<ResourceLocation, List<ResourceThunk>> listResourceStacks(String var1, Predicate<ResourceLocation> var2);

    public Stream<PackResources> listPacks();

    public static enum Empty implements ResourceManager
    {
        INSTANCE;


        @Override
        public Set<String> getNamespaces() {
            return Set.of();
        }

        @Override
        public Resource getResource(ResourceLocation resourceLocation) throws IOException {
            throw new FileNotFoundException(resourceLocation.toString());
        }

        @Override
        public boolean hasResource(ResourceLocation resourceLocation) {
            return false;
        }

        @Override
        public List<ResourceThunk> getResourceStack(ResourceLocation resourceLocation) throws IOException {
            throw new FileNotFoundException(resourceLocation.toString());
        }

        @Override
        public Map<ResourceLocation, ResourceThunk> listResources(String string, Predicate<ResourceLocation> predicate) {
            return Map.of();
        }

        @Override
        public Map<ResourceLocation, List<ResourceThunk>> listResourceStacks(String string, Predicate<ResourceLocation> predicate) {
            return Map.of();
        }

        @Override
        public Stream<PackResources> listPacks() {
            return Stream.of(new PackResources[0]);
        }
    }
}

