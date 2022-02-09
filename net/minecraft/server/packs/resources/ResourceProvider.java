/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import java.io.IOException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

@FunctionalInterface
public interface ResourceProvider {
    public Resource getResource(ResourceLocation var1) throws IOException;
}

