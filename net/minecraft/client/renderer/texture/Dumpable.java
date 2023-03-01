/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import java.io.IOException;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(value=EnvType.CLIENT)
public interface Dumpable {
    public void dumpContents(ResourceLocation var1, Path var2) throws IOException;
}

