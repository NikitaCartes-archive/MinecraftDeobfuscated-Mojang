/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class LayeredColorMaskTexture
extends AbstractTexture {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ResourceLocation baseLayerResource;
    private final List<String> layerMaskPaths;
    private final List<DyeColor> layerColors;

    public LayeredColorMaskTexture(ResourceLocation resourceLocation, List<String> list, List<DyeColor> list2) {
        this.baseLayerResource = resourceLocation;
        this.layerMaskPaths = list;
        this.layerColors = list2;
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {
        try (Resource resource = resourceManager.getResource(this.baseLayerResource);
             NativeImage nativeImage = NativeImage.read(resource.getInputStream());){
            NativeImage nativeImage2 = new NativeImage(nativeImage.getWidth(), nativeImage.getHeight(), false);
            nativeImage2.copyFrom(nativeImage);
            for (int i = 0; i < 17 && i < this.layerMaskPaths.size() && i < this.layerColors.size(); ++i) {
                String string = this.layerMaskPaths.get(i);
                if (string == null) continue;
                try (Resource resource2 = resourceManager.getResource(new ResourceLocation(string));
                     NativeImage nativeImage3 = NativeImage.read(resource2.getInputStream());){
                    int j = this.layerColors.get(i).getTextureDiffuseColorBGR();
                    if (nativeImage3.getWidth() != nativeImage2.getWidth() || nativeImage3.getHeight() != nativeImage2.getHeight()) continue;
                    for (int k = 0; k < nativeImage3.getHeight(); ++k) {
                        for (int l = 0; l < nativeImage3.getWidth(); ++l) {
                            int m = nativeImage3.getPixelRGBA(l, k);
                            if ((m & 0xFF000000) == 0) continue;
                            int n = (m & 0xFF) << 24 & 0xFF000000;
                            int o = nativeImage.getPixelRGBA(l, k);
                            int p = Mth.colorMultiply(o, j) & 0xFFFFFF;
                            nativeImage2.blendPixel(l, k, n | p);
                        }
                    }
                    continue;
                }
            }
            if (!RenderSystem.isOnRenderThreadOrInit()) {
                RenderSystem.recordRenderCall(() -> this.doLoad(nativeImage2));
            } else {
                this.doLoad(nativeImage2);
            }
        } catch (IOException iOException) {
            LOGGER.error("Couldn't load layered color mask image", (Throwable)iOException);
        }
    }

    private void doLoad(NativeImage nativeImage) {
        TextureUtil.prepareImage(this.getId(), nativeImage.getWidth(), nativeImage.getHeight());
        RenderSystem.pixelTransfer(3357, Float.MAX_VALUE);
        nativeImage.upload(0, 0, 0, true);
        RenderSystem.pixelTransfer(3357, 0.0f);
    }
}

