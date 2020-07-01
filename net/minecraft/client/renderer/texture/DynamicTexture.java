/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DynamicTexture
extends AbstractTexture {
    private static final Logger LOGGER = LogManager.getLogger();
    @Nullable
    private NativeImage pixels;

    public DynamicTexture(NativeImage nativeImage) {
        this.pixels = nativeImage;
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
                this.upload();
            });
        } else {
            TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
            this.upload();
        }
    }

    public DynamicTexture(int i, int j, boolean bl) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        this.pixels = new NativeImage(i, j, bl);
        TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
    }

    @Override
    public void load(ResourceManager resourceManager) {
    }

    public void upload() {
        if (this.pixels != null) {
            this.bind();
            this.pixels.upload(0, 0, 0, false);
        } else {
            LOGGER.warn("Trying to upload disposed texture {}", (Object)this.getId());
        }
    }

    @Nullable
    public NativeImage getPixels() {
        return this.pixels;
    }

    public void setPixels(NativeImage nativeImage) {
        if (this.pixels != null) {
            this.pixels.close();
        }
        this.pixels = nativeImage;
    }

    @Override
    public void close() {
        if (this.pixels != null) {
            this.pixels.close();
            this.releaseId();
            this.pixels = null;
        }
    }
}

