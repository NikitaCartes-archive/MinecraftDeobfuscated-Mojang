/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
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

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void load(ResourceManager resourceManager) throws IOException {
        Iterator<String> iterator = this.layerPaths.iterator();
        String string = iterator.next();
        try (Resource resource = resourceManager.getResource(new ResourceLocation(string));
             NativeImage nativeImage = NativeImage.read(resource.getInputStream());){
            while (iterator.hasNext()) {
                Resource resource2;
                block51: {
                    String string2 = iterator.next();
                    if (string2 == null) continue;
                    resource2 = resourceManager.getResource(new ResourceLocation(string2));
                    Throwable throwable = null;
                    try {
                        try (NativeImage nativeImage2 = NativeImage.read(resource2.getInputStream());){
                            for (int i = 0; i < nativeImage2.getHeight(); ++i) {
                                for (int j = 0; j < nativeImage2.getWidth(); ++j) {
                                    nativeImage.blendPixel(j, i, nativeImage2.getPixelRGBA(j, i));
                                }
                            }
                        }
                        if (resource2 == null) continue;
                        if (throwable == null) break block51;
                    } catch (Throwable throwable2) {
                        try {
                            throwable = throwable2;
                            throw throwable2;
                        } catch (Throwable throwable3) {
                            if (resource2 == null) throw throwable3;
                            if (throwable == null) {
                                resource2.close();
                                throw throwable3;
                            }
                            try {
                                resource2.close();
                                throw throwable3;
                            } catch (Throwable throwable4) {
                                throwable.addSuppressed(throwable4);
                                throw throwable3;
                            }
                        }
                    }
                    try {
                        resource2.close();
                        continue;
                    } catch (Throwable throwable5) {
                        throwable.addSuppressed(throwable5);
                        continue;
                    }
                }
                resource2.close();
            }
            TextureUtil.prepareImage(this.getId(), nativeImage.getWidth(), nativeImage.getHeight());
            nativeImage.upload(0, 0, 0, false);
            return;
        } catch (IOException iOException) {
            LOGGER.error("Couldn't load layered image", (Throwable)iOException);
        }
    }
}

