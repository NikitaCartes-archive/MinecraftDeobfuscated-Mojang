/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class Tesselator {
    private static final int MAX_MEMORY_USE = 0x800000;
    private static final int MAX_FLOATS = 0x200000;
    private final BufferBuilder builder;
    private static final Tesselator INSTANCE = new Tesselator();

    public static Tesselator getInstance() {
        RenderSystem.assertOnGameThreadOrInit();
        return INSTANCE;
    }

    public Tesselator(int i) {
        this.builder = new BufferBuilder(i);
    }

    public Tesselator() {
        this(0x200000);
    }

    public void end() {
        BufferUploader.drawWithShader(this.builder.end());
    }

    public BufferBuilder getBuilder() {
        return this.builder;
    }
}

