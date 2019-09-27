/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class LayeredTexture
extends AbstractTexture {
    private static final Logger LOGGER = LogManager.getLogger();
    public final List<String> layerPaths;

    public LayeredTexture(String ... strings) {
        this.layerPaths = Lists.newArrayList(strings);
        if (this.layerPaths.isEmpty()) {
            throw new IllegalStateException("Layered texture with no layers.");
        }
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {
        Iterator<String> iterator = this.layerPaths.iterator();
        String string = iterator.next();
        try (Resource resource = resourceManager.getResource(new ResourceLocation(string));){
            NativeImage nativeImage = NativeImage.read(resource.getInputStream());
            while (iterator.hasNext()) {
                String string2 = iterator.next();
                if (string2 == null) continue;
                Resource resource2 = resourceManager.getResource(new ResourceLocation(string2));
                Throwable throwable = null;
                try {
                    NativeImage nativeImage2 = NativeImage.read(resource2.getInputStream());
                    Throwable throwable2 = null;
                    try {
                        for (int i = 0; i < nativeImage2.getHeight(); ++i) {
                            for (int j = 0; j < nativeImage2.getWidth(); ++j) {
                                nativeImage.blendPixel(j, i, nativeImage2.getPixelRGBA(j, i));
                            }
                        }
                    } catch (Throwable throwable3) {
                        throwable2 = throwable3;
                        throw throwable3;
                    } finally {
                        if (nativeImage2 == null) continue;
                        if (throwable2 != null) {
                            try {
                                nativeImage2.close();
                            } catch (Throwable throwable4) {
                                throwable2.addSuppressed(throwable4);
                            }
                            continue;
                        }
                        nativeImage2.close();
                    }
                } catch (Throwable throwable5) {
                    throwable = throwable5;
                    throw throwable5;
                } finally {
                    if (resource2 == null) continue;
                    if (throwable != null) {
                        try {
                            resource2.close();
                        } catch (Throwable throwable6) {
                            throwable.addSuppressed(throwable6);
                        }
                        continue;
                    }
                    resource2.close();
                }
            }
            if (!RenderSystem.isOnRenderThreadOrInit()) {
                RenderSystem.recordRenderCall(() -> this.doLoad(nativeImage));
            } else {
                this.doLoad(nativeImage);
            }
        } catch (IOException iOException) {
            LOGGER.error("Couldn't load layered image", (Throwable)iOException);
        }
    }

    private void doLoad(NativeImage nativeImage) {
        TextureUtil.prepareImage(this.getId(), nativeImage.getWidth(), nativeImage.getHeight());
        nativeImage.upload(0, 0, 0, true);
    }
}

