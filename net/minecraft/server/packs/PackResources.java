/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.jetbrains.annotations.Nullable;

public interface PackResources
extends AutoCloseable {
    public static final String METADATA_EXTENSION = ".mcmeta";
    public static final String PACK_META = "pack.mcmeta";

    @Nullable
    public InputStream getRootResource(String var1) throws IOException;

    public InputStream getResource(PackType var1, ResourceLocation var2) throws IOException;

    public Collection<ResourceLocation> getResources(PackType var1, String var2, String var3, int var4, Predicate<String> var5);

    public boolean hasResource(PackType var1, ResourceLocation var2);

    public Set<String> getNamespaces(PackType var1);

    @Nullable
    public <T> T getMetadataSection(MetadataSectionSerializer<T> var1) throws IOException;

    public String getName();

    @Override
    public void close();
}

