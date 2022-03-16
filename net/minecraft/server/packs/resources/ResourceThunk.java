/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import java.io.IOException;
import net.minecraft.server.packs.resources.Resource;

public class ResourceThunk {
    private final String packId;
    private final ResourceSupplier resourceSupplier;

    public ResourceThunk(String string, ResourceSupplier resourceSupplier) {
        this.packId = string;
        this.resourceSupplier = resourceSupplier;
    }

    public String sourcePackId() {
        return this.packId;
    }

    public Resource open() throws IOException {
        return this.resourceSupplier.open();
    }

    @FunctionalInterface
    public static interface ResourceSupplier {
        public Resource open() throws IOException;
    }
}

