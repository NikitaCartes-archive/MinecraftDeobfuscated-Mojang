/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsScreen;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsTextureManager {
    private static final Map<String, RealmsTexture> textures = new HashMap<String, RealmsTexture>();
    private static final Map<String, Boolean> skinFetchStatus = new HashMap<String, Boolean>();
    private static final Map<String, String> fetchedSkins = new HashMap<String, String>();
    private static final Logger LOGGER = LogManager.getLogger();

    public static void bindWorldTemplate(String string, String string2) {
        if (string2 == null) {
            RealmsScreen.bind("textures/gui/presets/isles.png");
            return;
        }
        int i = RealmsTextureManager.getTextureId(string, string2);
        GlStateManager.bindTexture(i);
    }

    public static void withBoundFace(String string, Runnable runnable) {
        GLX.withTextureRestore(() -> {
            RealmsTextureManager.bindFace(string);
            runnable.run();
        });
    }

    private static void bindDefaultFace(UUID uUID) {
        RealmsScreen.bind((uUID.hashCode() & 1) == 1 ? "minecraft:textures/entity/alex.png" : "minecraft:textures/entity/steve.png");
    }

    private static void bindFace(final String string) {
        UUID uUID = UUIDTypeAdapter.fromString(string);
        if (textures.containsKey(string)) {
            GlStateManager.bindTexture(RealmsTextureManager.textures.get((Object)string).textureId);
            return;
        }
        if (skinFetchStatus.containsKey(string)) {
            if (!skinFetchStatus.get(string).booleanValue()) {
                RealmsTextureManager.bindDefaultFace(uUID);
            } else if (fetchedSkins.containsKey(string)) {
                int i = RealmsTextureManager.getTextureId(string, fetchedSkins.get(string));
                GlStateManager.bindTexture(i);
            } else {
                RealmsTextureManager.bindDefaultFace(uUID);
            }
            return;
        }
        skinFetchStatus.put(string, false);
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
                            httpURLConnection = (HttpURLConnection)new URL(string2).openConnection(Realms.getProxy());
                            httpURLConnection.setDoInput(true);
                            httpURLConnection.setDoOutput(false);
                            httpURLConnection.connect();
                            if (httpURLConnection.getResponseCode() / 100 != 2) {
                                skinFetchStatus.remove(string);
                                return;
                            }
                            try {
                                bufferedImage = ImageIO.read(httpURLConnection.getInputStream());
                            } catch (Exception exception) {
                                skinFetchStatus.remove(string);
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
                            skinFetchStatus.remove(string);
                        } finally {
                            if (httpURLConnection != null) {
                                httpURLConnection.disconnect();
                            }
                        }
                        ImageIO.write((RenderedImage)bufferedImage, "png", byteArrayOutputStream);
                        fetchedSkins.put(string, DatatypeConverter.printBase64Binary((byte[])byteArrayOutputStream.toByteArray()));
                        skinFetchStatus.put(string, true);
                        break block17;
                    }
                    skinFetchStatus.put(string, true);
                    return;
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static int getTextureId(String string, String string2) {
        int i;
        if (textures.containsKey(string)) {
            RealmsTexture realmsTexture = textures.get(string);
            if (realmsTexture.image.equals(string2)) {
                return realmsTexture.textureId;
            }
            GlStateManager.deleteTexture(realmsTexture.textureId);
            i = realmsTexture.textureId;
        } else {
            i = GlStateManager.genTexture();
        }
        IntBuffer intBuffer = null;
        int j = 0;
        int k = 0;
        try {
            BufferedImage bufferedImage;
            ByteArrayInputStream inputStream = new ByteArrayInputStream(new Base64().decode(string2));
            try {
                bufferedImage = ImageIO.read(inputStream);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
            j = bufferedImage.getWidth();
            k = bufferedImage.getHeight();
            int[] is = new int[j * k];
            bufferedImage.getRGB(0, 0, j, k, is, 0, j);
            intBuffer = ByteBuffer.allocateDirect(4 * j * k).order(ByteOrder.nativeOrder()).asIntBuffer();
            intBuffer.put(is);
            intBuffer.flip();
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }
        GlStateManager.activeTexture(GLX.GL_TEXTURE0);
        GlStateManager.bindTexture(i);
        TextureUtil.initTexture(intBuffer, j, k);
        textures.put(string, new RealmsTexture(string2, i));
        return i;
    }

    @Environment(value=EnvType.CLIENT)
    public static class RealmsTexture {
        String image;
        int textureId;

        public RealmsTexture(String string, int i) {
            this.image = string;
            this.textureId = i;
        }
    }
}

