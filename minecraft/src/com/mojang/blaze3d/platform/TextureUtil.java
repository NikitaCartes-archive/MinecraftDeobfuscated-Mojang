package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
@DontObfuscate
public class TextureUtil {
	private static final Logger LOGGER = LogUtils.getLogger();
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
		ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
		return readableByteChannel instanceof SeekableByteChannel seekableByteChannel
			? readResource(readableByteChannel, (int)seekableByteChannel.size() + 1)
			: readResource(readableByteChannel, 8192);
	}

	private static ByteBuffer readResource(ReadableByteChannel readableByteChannel, int i) throws IOException {
		ByteBuffer byteBuffer = MemoryUtil.memAlloc(i);

		try {
			while (readableByteChannel.read(byteBuffer) != -1) {
				if (!byteBuffer.hasRemaining()) {
					byteBuffer = MemoryUtil.memRealloc(byteBuffer, byteBuffer.capacity() * 2);
				}
			}

			return byteBuffer;
		} catch (IOException var4) {
			MemoryUtil.memFree(byteBuffer);
			throw var4;
		}
	}

	public static void writeAsPNG(Path path, String string, int i, int j, int k, int l) {
		writeAsPNG(path, string, i, j, k, l, null);
	}

	public static void writeAsPNG(Path path, String string, int i, int j, int k, int l, @Nullable IntUnaryOperator intUnaryOperator) {
		RenderSystem.assertOnRenderThread();
		bind(i);

		for (int m = 0; m <= j; m++) {
			int n = k >> m;
			int o = l >> m;

			try (NativeImage nativeImage = new NativeImage(n, o, false)) {
				nativeImage.downloadTexture(m, false);
				if (intUnaryOperator != null) {
					nativeImage.applyToAllPixels(intUnaryOperator);
				}

				Path path2 = path.resolve(string + "_" + m + ".png");
				nativeImage.writeToFile(path2);
				LOGGER.debug("Exported png to: {}", path2.toAbsolutePath());
			} catch (IOException var15) {
				LOGGER.debug("Unable to write: ", (Throwable)var15);
			}
		}
	}

	public static Path getDebugTexturePath(Path path) {
		return path.resolve("screenshots").resolve("debug");
	}

	public static Path getDebugTexturePath() {
		return getDebugTexturePath(Path.of("."));
	}
}
