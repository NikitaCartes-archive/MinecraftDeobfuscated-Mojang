/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.jetbrains.annotations.Nullable;

public class Resource {
    private final String packId;
    private final IoSupplier<InputStream> streamSupplier;
    private final IoSupplier<ResourceMetadata> metadataSupplier;
    @Nullable
    private ResourceMetadata cachedMetadata;

    public Resource(String string, IoSupplier<InputStream> ioSupplier, IoSupplier<ResourceMetadata> ioSupplier2) {
        this.packId = string;
        this.streamSupplier = ioSupplier;
        this.metadataSupplier = ioSupplier2;
    }

    public Resource(String string, IoSupplier<InputStream> ioSupplier) {
        this.packId = string;
        this.streamSupplier = ioSupplier;
        this.metadataSupplier = () -> ResourceMetadata.EMPTY;
        this.cachedMetadata = ResourceMetadata.EMPTY;
    }

    public String sourcePackId() {
        return this.packId;
    }

    public InputStream open() throws IOException {
        return this.streamSupplier.get();
    }

    public BufferedReader openAsReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.open(), StandardCharsets.UTF_8));
    }

    public ResourceMetadata metadata() throws IOException {
        if (this.cachedMetadata == null) {
            this.cachedMetadata = this.metadataSupplier.get();
        }
        return this.cachedMetadata;
    }

    @FunctionalInterface
    public static interface IoSupplier<T> {
        public T get() throws IOException;
    }
}

