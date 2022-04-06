/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;

public interface ResourceManager
extends ResourceProvider {
    public Set<String> getNamespaces();

    public List<Resource> getResourceStack(ResourceLocation var1);

    public Map<ResourceLocation, Resource> listResources(String var1, Predicate<ResourceLocation> var2);

    public Map<ResourceLocation, List<Resource>> listResourceStacks(String var1, Predicate<ResourceLocation> var2);

    public Stream<PackResources> listPacks();

    public static enum Empty implements ResourceManager
    {
        INSTANCE;


        @Override
        public Set<String> getNamespaces() {
            return Set.of();
        }

        @Override
        public Optional<Resource> getResource(ResourceLocation resourceLocation) {
            return Optional.empty();
        }

        @Override
        public List<Resource> getResourceStack(ResourceLocation resourceLocation) {
            return List.of();
        }

        @Override
        public Map<ResourceLocation, Resource> listResources(String string, Predicate<ResourceLocation> predicate) {
            return Map.of();
        }

        @Override
        public Map<ResourceLocation, List<Resource>> listResourceStacks(String string, Predicate<ResourceLocation> predicate) {
            return Map.of();
        }

        @Override
        public Stream<PackResources> listPacks() {
            return Stream.of(new PackResources[0]);
        }
    }
}

