/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.util.RealmsUtil;
import com.mojang.realmsclient.util.SkinProcessor;
import com.mojang.util.UUIDTypeAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

@Environment(value=EnvType.CLIENT)
public class RealmsTextureManager {
    private static final Map<String, RealmsTexture> TEXTURES = Maps.newHashMap();
    static final Map<String, Boolean> SKIN_FETCH_STATUS = Maps.newHashMap();
    static final Map<String, String> FETCHED_SKINS = Maps.newHashMap();
    static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation TEMPLATE_ICON_LOCATION = new ResourceLocation("textures/gui/presets/isles.png");

    public static void bindWorldTemplate(String string, @Nullable String string2) {
        if (string2 == null) {
            RenderSystem.setShaderTexture(0, TEMPLATE_ICON_LOCATION);
            return;
        }
        int i = RealmsTextureManager.getTextureId(string, string2);
        RenderSystem.setShaderTexture(0, i);
    }

    public static void withBoundFace(String string, Runnable runnable) {
        RealmsTextureManager.bindFace(string);
        runnable.run();
    }

    private static void bindDefaultFace(UUID uUID) {
        RenderSystem.setShaderTexture(0, DefaultPlayerSkin.getDefaultSkin(uUID));
    }

    private static void bindFace(final String string) {
        UUID uUID = UUIDTypeAdapter.fromString(string);
        if (TEXTURES.containsKey(string)) {
            int i = RealmsTextureManager.TEXTURES.get((Object)string).textureId;
            RenderSystem.setShaderTexture(0, i);
            return;
        }
        if (SKIN_FETCH_STATUS.containsKey(string)) {
            if (!SKIN_FETCH_STATUS.get(string).booleanValue()) {
                RealmsTextureManager.bindDefaultFace(uUID);
            } else if (FETCHED_SKINS.containsKey(string)) {
                int i = RealmsTextureManager.getTextureId(string, FETCHED_SKINS.get(string));
                RenderSystem.setShaderTexture(0, i);
            } else {
                RealmsTextureManager.bindDefaultFace(uUID);
            }
            return;
        }
        SKIN_FETCH_STATUS.put(string, false);
        RealmsTextureManager.bindDefaultFace(uUID);
        Thread thread = new Thread("Realms Texture Downloader"){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                block17: {
                    block16: {
                        ByteArrayOutputStream byteArrayOutputStream;
                        BufferedImage bufferedImage;
                        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = RealmsUtil.getTextures(string);
                        if (!map.containsKey((Object)MinecraftProfileTexture.Type.SKIN)) break block16;
                        MinecraftProfileTexture minecraftProfileTexture = map.get((Object)MinecraftProfileTexture.Type.SKIN);
                        String string2 = minecraftProfileTexture.getUrl();
                        HttpURLConnection httpURLConnection = null;
                        LOGGER.debug("Downloading http texture from {}", (Object)string2);
                        try {
                            httpURLConnection = (HttpURLConnection)new URL(string2).openConnection(Minecraft.getInstance().getProxy());
                            httpURLConnection.setDoInput(true);
                            httpURLConnection.setDoOutput(false);
                            httpURLConnection.connect();
                            if (httpURLConnection.getResponseCode() / 100 != 2) {
                                SKIN_FETCH_STATUS.remove(string);
                                return;
                            }
                            try {
                                bufferedImage = ImageIO.read(httpURLConnection.getInputStream());
                            } catch (Exception exception) {
                                SKIN_FETCH_STATUS.remove(string);
                                if (httpURLConnection != null) {
                                    httpURLConnection.disconnect();
                                }
                                return;
                            } finally {
                                IOUtils.closeQuietly(httpURLConnection.getInputStream());
                            }
                            bufferedImage = new SkinProcessor().process(bufferedImage);
                            byteArrayOutputStream = new ByteArrayOutputStream();
                        } catch (Exception exception2) {
                            LOGGER.error("Couldn't download http texture", (Throwable)exception2);
                            SKIN_FETCH_STATUS.remove(string);
                        } finally {
                            if (httpURLConnection != null) {
                                httpURLConnection.disconnect();
                            }
                        }
                        ImageIO.write((RenderedImage)bufferedImage, "png", byteArrayOutputStream);
                        FETCHED_SKINS.put(string, new Base64().encodeToString(byteArrayOutputStream.toByteArray()));
                        SKIN_FETCH_STATUS.put(string, true);
                        break block17;
                    }
                    SKIN_FETCH_STATUS.put(string, true);
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    private static int getTextureId(String string, String string2) {
        RealmsTexture realmsTexture = TEXTURES.get(string);
        if (realmsTexture != null && realmsTexture.image.equals(string2)) {
            return realmsTexture.textureId;
        }
        int i = realmsTexture != null ? realmsTexture.textureId : GlStateManager._genTexture();
        TextureData textureData = TextureData.load(string2);
        RenderSystem.activeTexture(33984);
        RenderSystem.bindTextureForSetup(i);
        TextureUtil.initTexture(textureData.data, textureData.width, textureData.height);
        TEXTURES.put(string, new RealmsTexture(string2, i));
        return i;
    }

    @Environment(value=EnvType.CLIENT)
    public static class RealmsTexture {
        final String image;
        final int textureId;

        public RealmsTexture(String string, int i) {
            this.image = string;
            this.textureId = i;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class TextureData {
        final int width;
        final int height;
        final IntBuffer data;
        private static final Supplier<TextureData> MISSING = Suppliers.memoize(() -> {
            int i = 16;
            int j = 16;
            IntBuffer intBuffer = BufferUtils.createIntBuffer(256);
            int k = -16777216;
            int l = -524040;
            for (int m = 0; m < 16; ++m) {
                for (int n = 0; n < 16; ++n) {
                    if (m < 8 ^ n < 8) {
                        intBuffer.put(n + m * 16, -524040);
                        continue;
                    }
                    intBuffer.put(n + m * 16, -16777216);
                }
            }
            return new TextureData(16, 16, intBuffer);
        });

        private TextureData(int i, int j, IntBuffer intBuffer) {
            this.width = i;
            this.height = j;
            this.data = intBuffer;
        }

        public static TextureData load(String string) {
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(new Base64().decode(string));
                BufferedImage bufferedImage = ImageIO.read(inputStream);
                if (bufferedImage != null) {
                    int i = bufferedImage.getWidth();
                    int j = bufferedImage.getHeight();
                    int[] is = new int[i * j];
                    bufferedImage.getRGB(0, 0, i, j, is, 0, i);
                    IntBuffer intBuffer = BufferUtils.createIntBuffer(i * j);
                    intBuffer.put(is);
                    intBuffer.flip();
                    return new TextureData(i, j, intBuffer);
                }
                LOGGER.warn("Unknown image format: {}", (Object)string);
            } catch (IOException iOException) {
                LOGGER.warn("Failed to load world image: {}", (Object)string, (Object)iOException);
            }
            return MISSING.get();
        }
    }
}

