/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import java.io.Closeable;
import java.io.InputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.jetbrains.annotations.Nullable;

public interface Resource
extends Closeable {
    @Environment(value=EnvType.CLIENT)
    public ResourceLocation getLocation();

    public InputStream getInputStream();

    @Nullable
    @Environment(value=EnvType.CLIENT)
    public <T> T getMetadata(MetadataSectionSerializer<T> var1);

    public String getSourceName();
}

