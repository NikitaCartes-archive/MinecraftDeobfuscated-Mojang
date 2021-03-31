/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import java.io.Closeable;
import java.io.InputStream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.jetbrains.annotations.Nullable;

public interface Resource
extends Closeable {
    public ResourceLocation getLocation();

    public InputStream getInputStream();

    public boolean hasMetadata();

    @Nullable
    public <T> T getMetadata(MetadataSectionSerializer<T> var1);

    public String getSourceName();
}

