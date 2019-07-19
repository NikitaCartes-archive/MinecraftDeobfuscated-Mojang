/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import java.io.Closeable;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SimpleTexture
extends AbstractTexture {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final ResourceLocation location;

    public SimpleTexture(ResourceLocation resourceLocation) {
        this.location = resourceLocation;
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {
        try (TextureImage textureImage = this.getTextureImage(resourceManager);){
            boolean bl = false;
            boolean bl2 = false;
            textureImage.throwIfError();
            TextureMetadataSection textureMetadataSection = textureImage.getTextureMetadata();
            if (textureMetadataSection != null) {
                bl = textureMetadataSection.isBlur();
                bl2 = textureMetadataSection.isClamp();
            }
            this.bind();
            TextureUtil.prepareImage(this.getId(), 0, textureImage.getImage().getWidth(), textureImage.getImage().getHeight());
            textureImage.getImage().upload(0, 0, 0, 0, 0, textureImage.getImage().getWidth(), textureImage.getImage().getHeight(), bl, bl2, false);
        }
    }

    protected TextureImage getTextureImage(ResourceManager resourceManager) {
        return TextureImage.load(resourceManager, this.location);
    }

    @Environment(value=EnvType.CLIENT)
    public static class TextureImage
    implements Closeable {
        private final TextureMetadataSection metadata;
        private final NativeImage image;
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

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        public static TextureImage load(ResourceManager resourceManager, ResourceLocation resourceLocation) {
            try (Resource resource = resourceManager.getResource(resourceLocation);){
                NativeImage nativeImage = NativeImage.read(resource.getInputStream());
                TextureMetadataSection textureMetadataSection = null;
                try {
                    textureMetadataSection = resource.getMetadata(TextureMetadataSection.SERIALIZER);
                } catch (RuntimeException runtimeException) {
                    LOGGER.warn("Failed reading metadata of: {}", (Object)resourceLocation, (Object)runtimeException);
                }
                TextureImage textureImage = new TextureImage(textureMetadataSection, nativeImage);
                return textureImage;
            } catch (IOException iOException) {
                return new TextureImage(iOException);
            }
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

