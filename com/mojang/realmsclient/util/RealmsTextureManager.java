/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsTextureManager {
    private static final Map<String, RealmsTexture> TEXTURES = Maps.newHashMap();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation TEMPLATE_ICON_LOCATION = new ResourceLocation("textures/gui/presets/isles.png");

    public static ResourceLocation worldTemplate(String string, @Nullable String string2) {
        if (string2 == null) {
            return TEMPLATE_ICON_LOCATION;
        }
        return RealmsTextureManager.getTexture(string, string2);
    }

    private static ResourceLocation getTexture(String string, String string2) {
        RealmsTexture realmsTexture = TEXTURES.get(string);
        if (realmsTexture != null && realmsTexture.image().equals(string2)) {
            return realmsTexture.textureId;
        }
        NativeImage nativeImage = RealmsTextureManager.loadImage(string2);
        if (nativeImage == null) {
            ResourceLocation resourceLocation = MissingTextureAtlasSprite.getLocation();
            TEXTURES.put(string, new RealmsTexture(string2, resourceLocation));
            return resourceLocation;
        }
        ResourceLocation resourceLocation = new ResourceLocation("realms", "dynamic/" + string);
        Minecraft.getInstance().getTextureManager().register(resourceLocation, (AbstractTexture)new DynamicTexture(nativeImage));
        TEXTURES.put(string, new RealmsTexture(string2, resourceLocation));
        return resourceLocation;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Nullable
    private static NativeImage loadImage(String string) {
        byte[] bs = Base64.getDecoder().decode(string);
        ByteBuffer byteBuffer = MemoryUtil.memAlloc(bs.length);
        try {
            NativeImage nativeImage = NativeImage.read(byteBuffer.put(bs).flip());
            return nativeImage;
        } catch (IOException iOException) {
            LOGGER.warn("Failed to load world image: {}", (Object)string, (Object)iOException);
        } finally {
            MemoryUtil.memFree(byteBuffer);
        }
        return null;
    }

    @Environment(value=EnvType.CLIENT)
    public record RealmsTexture(String image, ResourceLocation textureId) {
    }
}

