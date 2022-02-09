/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import net.minecraft.server.packs.resources.ResourceManager;

public interface CloseableResourceManager
extends ResourceManager,
AutoCloseable {
    @Override
    public void close();
}

