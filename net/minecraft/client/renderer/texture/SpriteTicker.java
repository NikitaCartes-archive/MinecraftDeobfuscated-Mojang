/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface SpriteTicker
extends AutoCloseable {
    public void tickAndUpload(int var1, int var2);

    @Override
    public void close();
}

