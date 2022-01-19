/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SimpleTexture
extends AbstractTexture {
    static final Logger LOGGER = LogUtils.getLogger();
    protected final ResourceLocation location;

    public SimpleTexture(ResourceLocation resourceLocation) {
        this.location = resourceLocation;
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {
        boolean bl2;
        boolean bl;
        TextureImage textureImage = this.getTextureImage(resourceManager);
        textureImage.throwIfError();
        TextureMetadataSection textureMetadataSection = textureImage.getTextureMetadata();
        if (textureMetadataSection != null) {
            bl = textureMetadataSection.isBlur();
            bl2 = textureMetadataSection.isClamp();
        } else {
            bl = false;
            bl2 = false;
        }
        NativeImage nativeImage = textureImage.getImage();
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> this.doLoad(nativeImage, bl, bl2));
        } else {
            this.doLoad(nativeImage, bl, bl2);
        }
    }

    private void doLoad(NativeImage nativeImage, boolean bl, boolean bl2) {
        TextureUtil.prepareImage(this.getId(), 0, nativeImage.getWidth(), nativeImage.getHeight());
        nativeImage.upload(0, 0, 0, 0, 0, nativeImage.getWidth(), nativeImage.getHeight(), bl, bl2, false, true);
    }

    protected TextureImage getTextureImage(ResourceManager resourceManager) {
        return TextureImage.load(resourceManager, this.location);
    }

    @Environment(value=EnvType.CLIENT)
    protected static class TextureImage
    implements Closeable {
        @Nullable
        private final TextureMetadataSection metadata;
        @Nullable
        private final NativeImage image;
        @Nullable
        private final IOException exception;

        public TextureImage(IOException iOException) {
            this.exception = iOException;
            this.metadata = null;
            this.image = null;
        }

        public TextureImage(@Nullable TextureMetadataSection textureMetadataSection, NativeImage nativeImage) {
            this.exception = null;
            this.metadata = textureMetadataSection;
            this.image = nativeImage;
        }

        public static TextureImage load(ResourceManager resourceManager, ResourceLocation resourceLocation) {
            TextureImage textureImage;
            block10: {
                Resource resource = resourceManager.getResource(resourceLocation);
                try {
                    NativeImage nativeImage = NativeImage.read(resource.getInputStream());
                    TextureMetadataSection textureMetadataSection = null;
                    try {
                        textureMetadataSection = resource.getMetadata(TextureMetadataSection.SERIALIZER);
                    } catch (RuntimeException runtimeException) {
                        LOGGER.warn("Failed reading metadata of: {}", (Object)resourceLocation, (Object)runtimeException);
                    }
                    textureImage = new TextureImage(textureMetadataSection, nativeImage);
                    if (resource == null) break block10;
                } catch (Throwable throwable) {
                    try {
                        if (resource != null) {
                            try {
                                resource.close();
                            } catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    } catch (IOException iOException) {
                        return new TextureImage(iOException);
                    }
                }
                resource.close();
            }
            return textureImage;
        }

        @Nullable
        public TextureMetadataSection getTextureMetadata() {
            return this.metadata;
        }

        public NativeImage getImage() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            }
            return this.image;
        }

        @Override
        public void close() {
            if (this.image != null) {
                this.image.close();
            }
        }

        public void throwIfError() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            }
        }
    }
}

