/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class Tesselator {
    private final BufferBuilder builder;
    private final BufferUploader uploader = new BufferUploader();
    private static final Tesselator INSTANCE = new Tesselator(0x200000);

    public static Tesselator getInstance() {
        return INSTANCE;
    }

    public Tesselator(int i) {
        this.builder = new BufferBuilder(i);
    }

    public void end() {
        this.builder.end();
        this.uploader.end(this.builder);
    }

    public BufferBuilder getBuilder() {
        return this.builder;
    }
}

