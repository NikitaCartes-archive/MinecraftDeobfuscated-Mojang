/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

@Environment(value=EnvType.CLIENT)
public class LazyLoadedImage {
    private final ResourceLocation id;
    private final Resource resource;
    private final AtomicReference<NativeImage> image = new AtomicReference();
    private final AtomicInteger referenceCount;

    public LazyLoadedImage(ResourceLocation resourceLocation, Resource resource, int i) {
        this.id = resourceLocation;
        this.resource = resource;
        this.referenceCount = new AtomicInteger(i);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public NativeImage get() throws IOException {
        NativeImage nativeImage = this.image.get();
        if (nativeImage == null) {
            LazyLoadedImage lazyLoadedImage = this;
            synchronized (lazyLoadedImage) {
                nativeImage = this.image.get();
                if (nativeImage == null) {
                    try (InputStream inputStream = this.resource.open();){
                        nativeImage = NativeImage.read(inputStream);
                        this.image.set(nativeImage);
                    } catch (IOException iOException) {
                        throw new IOException("Failed to load image " + this.id, iOException);
                    }
                }
            }
        }
        return nativeImage;
    }

    public void release() {
        NativeImage nativeImage;
        int i = this.referenceCount.decrementAndGet();
        if (i <= 0 && (nativeImage = (NativeImage)this.image.getAndSet(null)) != null) {
            nativeImage.close();
        }
    }
}

