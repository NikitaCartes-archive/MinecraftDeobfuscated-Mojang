/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

public interface PackResources
extends AutoCloseable {
    public static final String METADATA_EXTENSION = ".mcmeta";
    public static final String PACK_META = "pack.mcmeta";

    @Nullable
    public IoSupplier<InputStream> getRootResource(String ... var1);

    @Nullable
    public IoSupplier<InputStream> getResource(PackType var1, ResourceLocation var2);

    public void listResources(PackType var1, String var2, String var3, ResourceOutput var4);

    public Set<String> getNamespaces(PackType var1);

    @Nullable
    public <T> T getMetadataSection(MetadataSectionSerializer<T> var1) throws IOException;

    public String packId();

    default public boolean isBuiltin() {
        return false;
    }

    @Override
    public void close();

    @FunctionalInterface
    public static interface ResourceOutput
    extends BiConsumer<ResourceLocation, IoSupplier<InputStream>> {
    }
}

