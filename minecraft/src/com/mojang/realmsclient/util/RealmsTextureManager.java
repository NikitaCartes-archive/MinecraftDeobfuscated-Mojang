package com.mojang.realmsclient.util;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.util.UUIDTypeAdapter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsTextureManager {
	private static final Map<String, RealmsTextureManager.RealmsTexture> TEXTURES = Maps.<String, RealmsTextureManager.RealmsTexture>newHashMap();
	static final Map<String, Boolean> SKIN_FETCH_STATUS = Maps.<String, Boolean>newHashMap();
	static final Map<String, String> FETCHED_SKINS = Maps.<String, String>newHashMap();
	static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceLocation TEMPLATE_ICON_LOCATION = new ResourceLocation("textures/gui/presets/isles.png");

	public static void bindWorldTemplate(String string, @Nullable String string2) {
		if (string2 == null) {
			RenderSystem.setShaderTexture(0, TEMPLATE_ICON_LOCATION);
		} else {
			int i = getTextureId(string, string2);
			RenderSystem.setShaderTexture(0, i);
		}
	}

	public static void withBoundFace(String string, Runnable runnable) {
		bindFace(string);
		runnable.run();
	}

	private static void bindDefaultFace(UUID uUID) {
		RenderSystem.setShaderTexture(0, DefaultPlayerSkin.getDefaultSkin(uUID));
	}

	private static void bindFace(String string) {
		UUID uUID = UUIDTypeAdapter.fromString(string);
		if (TEXTURES.containsKey(string)) {
			int i = ((RealmsTextureManager.RealmsTexture)TEXTURES.get(string)).textureId;
			RenderSystem.setShaderTexture(0, i);
		} else if (SKIN_FETCH_STATUS.containsKey(string)) {
			if (!(Boolean)SKIN_FETCH_STATUS.get(string)) {
				bindDefaultFace(uUID);
			} else if (FETCHED_SKINS.containsKey(string)) {
				int i = getTextureId(string, (String)FETCHED_SKINS.get(string));
				RenderSystem.setShaderTexture(0, i);
			} else {
				bindDefaultFace(uUID);
			}
		} else {
			SKIN_FETCH_STATUS.put(string, false);
			bindDefaultFace(uUID);
			Thread thread = new Thread("Realms Texture Downloader") {
				public void run() {
					Map<Type, MinecraftProfileTexture> map = RealmsUtil.getTextures(string);
					if (map.containsKey(Type.SKIN)) {
						MinecraftProfileTexture minecraftProfileTexture = (MinecraftProfileTexture)map.get(Type.SKIN);
						String string = minecraftProfileTexture.getUrl();
						HttpURLConnection httpURLConnection = null;
						RealmsTextureManager.LOGGER.debug("Downloading http texture from {}", string);

						try {
							try {
								httpURLConnection = (HttpURLConnection)new URL(string).openConnection(Minecraft.getInstance().getProxy());
								httpURLConnection.setDoInput(true);
								httpURLConnection.setDoOutput(false);
								httpURLConnection.connect();
								if (httpURLConnection.getResponseCode() / 100 != 2) {
									RealmsTextureManager.SKIN_FETCH_STATUS.remove(string);
									return;
								}

								BufferedImage bufferedImage;
								try {
									bufferedImage = ImageIO.read(httpURLConnection.getInputStream());
								} catch (Exception var17) {
									RealmsTextureManager.SKIN_FETCH_STATUS.remove(string);
									return;
								} finally {
									IOUtils.closeQuietly(httpURLConnection.getInputStream());
								}

								bufferedImage = new SkinProcessor().process(bufferedImage);
								ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
								ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
								RealmsTextureManager.FETCHED_SKINS.put(string, new Base64().encodeToString(byteArrayOutputStream.toByteArray()));
								RealmsTextureManager.SKIN_FETCH_STATUS.put(string, true);
							} catch (Exception var19) {
								RealmsTextureManager.LOGGER.error("Couldn't download http texture", (Throwable)var19);
								RealmsTextureManager.SKIN_FETCH_STATUS.remove(string);
							}
						} finally {
							if (httpURLConnection != null) {
								httpURLConnection.disconnect();
							}
						}
					} else {
						RealmsTextureManager.SKIN_FETCH_STATUS.put(string, true);
					}
				}
			};
			thread.setDaemon(true);
			thread.start();
		}
	}

	private static int getTextureId(String string, String string2) {
		RealmsTextureManager.RealmsTexture realmsTexture = (RealmsTextureManager.RealmsTexture)TEXTURES.get(string);
		if (realmsTexture != null && realmsTexture.image.equals(string2)) {
			return realmsTexture.textureId;
		} else {
			int i;
			if (realmsTexture != null) {
				i = realmsTexture.textureId;
			} else {
				i = GlStateManager._genTexture();
			}

			RealmsTextureManager.TextureData textureData = RealmsTextureManager.TextureData.load(string2);
			RenderSystem.activeTexture(33984);
			RenderSystem.bindTextureForSetup(i);
			TextureUtil.initTexture(textureData.data, textureData.width, textureData.height);
			TEXTURES.put(string, new RealmsTextureManager.RealmsTexture(string2, i));
			return i;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class RealmsTexture {
		final String image;
		final int textureId;

		public RealmsTexture(String string, int i) {
			this.image = string;
			this.textureId = i;
		}
	}

	@Environment(EnvType.CLIENT)
	static class TextureData {
		final int width;
		final int height;
		final IntBuffer data;
		private static final Supplier<RealmsTextureManager.TextureData> MISSING = Suppliers.memoize(() -> {
			int i = 16;
			int j = 16;
			IntBuffer intBuffer = BufferUtils.createIntBuffer(256);
			int k = -16777216;
			int l = -524040;

			for (int m = 0; m < 16; m++) {
				for (int n = 0; n < 16; n++) {
					if (m < 8 ^ n < 8) {
						intBuffer.put(n + m * 16, -524040);
					} else {
						intBuffer.put(n + m * 16, -16777216);
					}
				}
			}

			return new RealmsTextureManager.TextureData(16, 16, intBuffer);
		});

		private TextureData(int i, int j, IntBuffer intBuffer) {
			this.width = i;
			this.height = j;
			this.data = intBuffer;
		}

		public static RealmsTextureManager.TextureData load(String string) {
			try {
				InputStream inputStream = new ByteArrayInputStream(new Base64().decode(string));
				BufferedImage bufferedImage = ImageIO.read(inputStream);
				if (bufferedImage != null) {
					int i = bufferedImage.getWidth();
					int j = bufferedImage.getHeight();
					int[] is = new int[i * j];
					bufferedImage.getRGB(0, 0, i, j, is, 0, i);
					IntBuffer intBuffer = BufferUtils.createIntBuffer(i * j);
					intBuffer.put(is);
					intBuffer.flip();
					return new RealmsTextureManager.TextureData(i, j, intBuffer);
				}

				RealmsTextureManager.LOGGER.warn("Unknown image format: {}", string);
			} catch (IOException var7) {
				RealmsTextureManager.LOGGER.warn("Failed to load world image: {}", string, var7);
			}

			return (RealmsTextureManager.TextureData)MISSING.get();
		}
	}
}
