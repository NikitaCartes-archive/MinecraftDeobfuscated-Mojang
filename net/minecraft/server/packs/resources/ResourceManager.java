/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

public interface ResourceManager {
    @Environment(value=EnvType.CLIENT)
    public Set<String> getNamespaces();

    public Resource getResource(ResourceLocation var1) throws IOException;

    @Environment(value=EnvType.CLIENT)
    public boolean hasResource(ResourceLocation var1);

    public List<Resource> getResources(ResourceLocation var1) throws IOException;

    public Collection<ResourceLocation> listResources(String var1, Predicate<String> var2);
}

