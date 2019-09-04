/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(value=EnvType.CLIENT)
public interface TextureObject {
    public void pushFilter(boolean var1, boolean var2);

    public void popFilter();

    public void load(ResourceManager var1) throws IOException;

    public int getId();

    default public void bind() {
        RenderSystem.bindTexture(this.getId());
    }

    default public void reset(TextureManager textureManager, ResourceManager resourceManager, ResourceLocation resourceLocation, Executor executor) {
        textureManager.register(resourceLocation, this);
    }
}

