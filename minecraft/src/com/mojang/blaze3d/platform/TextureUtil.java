package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
@DontObfuscate
public class TextureUtil {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final int MIN_MIPMAP_LEVEL = 0;
	private static final int DEFAULT_IMAGE_BUFFER_SIZE = 8192;

	public static int generateTextureId() {
		RenderSystem.assertOnRenderThreadOrInit();
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			int[] is = new int[ThreadLocalRandom.current().nextInt(15) + 1];
			GlStateManager._genTextures(is);
			int i = GlStateManager._genTexture();
			GlStateManager._deleteTextures(is);
			return i;
		} else {
			return GlStateManager._genTexture();
		}
	}

	public static void releaseTextureId(int i) {
		RenderSystem.assertOnRenderThreadOrInit();
		GlStateManager._deleteTexture(i);
	}

	public static void prepareImage(int i, int j, int k) {
		prepareImage(NativeImage.InternalGlFormat.RGBA, i, 0, j, k);
	}

	public static void prepareImage(NativeImage.InternalGlFormat internalGlFormat, int i, int j, int k) {
		prepareImage(internalGlFormat, i, 0, j, k);
	}

	public static void prepareImage(int i, int j, int k, int l) {
		prepareImage(NativeImage.InternalGlFormat.RGBA, i, j, k, l);
	}

	public static void prepareImage(NativeImage.InternalGlFormat internalGlFormat, int i, int j, int k, int l) {
		RenderSystem.assertOnRenderThreadOrInit();
		bind(i);
		if (j >= 0) {
			GlStateManager._texParameter(3553, 33085, j);
			GlStateManager._texParameter(3553, 33082, 0);
			GlStateManager._texParameter(3553, 33083, j);
			GlStateManager._texParameter(3553, 34049, 0.0F);
		}

		for (int m = 0; m <= j; m++) {
			GlStateManager._texImage2D(3553, m, internalGlFormat.glFormat(), k >> m, l >> m, 0, 6408, 5121, null);
		}
	}

	private static void bind(int i) {
		RenderSystem.assertOnRenderThreadOrInit();
		GlStateManager._bindTexture(i);
	}

	public static ByteBuffer readResource(InputStream inputStream) throws IOException {
		ByteBuffer byteBuffer;
		if (inputStream instanceof FileInputStream fileInputStream) {
			FileChannel fileChannel = fileInputStream.getChannel();
			byteBuffer = MemoryUtil.memAlloc((int)fileChannel.size() + 1);

			while (fileChannel.read(byteBuffer) != -1) {
			}
		} else {
			byteBuffer = MemoryUtil.memAlloc(8192);
			ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);

			while (readableByteChannel.read(byteBuffer) != -1) {
				if (byteBuffer.remaining() == 0) {
					byteBuffer = MemoryUtil.memRealloc(byteBuffer, byteBuffer.capacity() * 2);
				}
			}
		}

		return byteBuffer;
	}

	@Nullable
	public static String readResourceAsString(InputStream inputStream) {
		RenderSystem.assertOnRenderThread();
		ByteBuffer byteBuffer = null;

		try {
			byteBuffer = readResource(inputStream);
			int i = byteBuffer.position();
			byteBuffer.rewind();
			return MemoryUtil.memASCII(byteBuffer, i);
		} catch (IOException var7) {
		} finally {
			if (byteBuffer != null) {
				MemoryUtil.memFree(byteBuffer);
			}
		}

		return null;
	}

	public static void writeAsPNG(String string, int i, int j, int k, int l) {
		RenderSystem.assertOnRenderThread();
		bind(i);

		for (int m = 0; m <= j; m++) {
			String string2 = string + "_" + m + ".png";
			int n = k >> m;
			int o = l >> m;

			try (NativeImage nativeImage = new NativeImage(n, o, false)) {
				nativeImage.downloadTexture(m, false);
				nativeImage.writeToFile(string2);
				LOGGER.debug("Exported png to: {}", new File(string2).getAbsolutePath());
			} catch (IOException var14) {
				LOGGER.debug("Unable to write: ", (Throwable)var14);
			}
		}
	}

	public static void initTexture(IntBuffer intBuffer, int i, int j) {
		RenderSystem.assertOnRenderThread();
		GL11.glPixelStorei(3312, 0);
		GL11.glPixelStorei(3313, 0);
		GL11.glPixelStorei(3314, 0);
		GL11.glPixelStorei(3315, 0);
		GL11.glPixelStorei(3316, 0);
		GL11.glPixelStorei(3317, 4);
		GL11.glTexImage2D(3553, 0, 6408, i, j, 0, 32993, 33639, intBuffer);
		GL11.glTexParameteri(3553, 10240, 9728);
		GL11.glTexParameteri(3553, 10241, 9729);
	}
}
