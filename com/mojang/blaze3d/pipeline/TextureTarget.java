/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class TextureTarget
extends RenderTarget {
    public TextureTarget(int i, int j, boolean bl, boolean bl2) {
        super(bl);
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        this.resize(i, j, bl2);
    }
}

