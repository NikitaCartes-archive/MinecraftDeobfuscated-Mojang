package com.mojang.blaze3d.platform;

import com.google.common.base.Charsets;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public final class NativeImage implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Set<StandardOpenOption> OPEN_OPTIONS = EnumSet.of(
		StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
	);
	private final NativeImage.Format format;
	private final int width;
	private final int height;
	private final boolean useStbFree;
	private long pixels;
	private final long size;

	public NativeImage(int i, int j, boolean bl) {
		this(NativeImage.Format.RGBA, i, j, bl);
	}

	public NativeImage(NativeImage.Format format, int i, int j, boolean bl) {
		this.format = format;
		this.width = i;
		this.height = j;
		this.size = (long)i * (long)j * (long)format.components();
		this.useStbFree = false;
		if (bl) {
			this.pixels = MemoryUtil.nmemCalloc(1L, this.size);
		} else {
			this.pixels = MemoryUtil.nmemAlloc(this.size);
		}
	}

	private NativeImage(NativeImage.Format format, int i, int j, boolean bl, long l) {
		this.format = format;
		this.width = i;
		this.height = j;
		this.useStbFree = bl;
		this.pixels = l;
		this.size = (long)(i * j * format.components());
	}

	public String toString() {
		return "NativeImage[" + this.format + " " + this.width + "x" + this.height + "@" + this.pixels + (this.useStbFree ? "S" : "N") + "]";
	}

	public static NativeImage read(InputStream inputStream) throws IOException {
		return read(NativeImage.Format.RGBA, inputStream);
	}

	public static NativeImage read(@Nullable NativeImage.Format format, InputStream inputStream) throws IOException {
		ByteBuffer byteBuffer = null;

		NativeImage var3;
		try {
			byteBuffer = TextureUtil.readResource(inputStream);
			byteBuffer.rewind();
			var3 = read(format, byteBuffer);
		} finally {
			MemoryUtil.memFree(byteBuffer);
			IOUtils.closeQuietly(inputStream);
		}

		return var3;
	}

	public static NativeImage read(ByteBuffer byteBuffer) throws IOException {
		return read(NativeImage.Format.RGBA, byteBuffer);
	}

	public static NativeImage read(@Nullable NativeImage.Format format, ByteBuffer byteBuffer) throws IOException {
		if (format != null && !format.supportedByStb()) {
			throw new UnsupportedOperationException("Don't know how to read format " + format);
		} else if (MemoryUtil.memAddress(byteBuffer) == 0L) {
			throw new IllegalArgumentException("Invalid buffer");
		} else {
			NativeImage var8;
			try (MemoryStack memoryStack = MemoryStack.stackPush()) {
				IntBuffer intBuffer = memoryStack.mallocInt(1);
				IntBuffer intBuffer2 = memoryStack.mallocInt(1);
				IntBuffer intBuffer3 = memoryStack.mallocInt(1);
				ByteBuffer byteBuffer2 = STBImage.stbi_load_from_memory(byteBuffer, intBuffer, intBuffer2, intBuffer3, format == null ? 0 : format.components);
				if (byteBuffer2 == null) {
					throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
				}

				var8 = new NativeImage(
					format == null ? NativeImage.Format.getStbFormat(intBuffer3.get(0)) : format,
					intBuffer.get(0),
					intBuffer2.get(0),
					true,
					MemoryUtil.memAddress(byteBuffer2)
				);
			}

			return var8;
		}
	}

	private static void setClamp(boolean bl) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		if (bl) {
			GlStateManager._texParameter(3553, 10242, 10496);
			GlStateManager._texParameter(3553, 10243, 10496);
		} else {
			GlStateManager._texParameter(3553, 10242, 10497);
			GlStateManager._texParameter(3553, 10243, 10497);
		}
	}

	private static void setFilter(boolean bl, boolean bl2) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		if (bl) {
			GlStateManager._texParameter(3553, 10241, bl2 ? 9987 : 9729);
			GlStateManager._texParameter(3553, 10240, 9729);
		} else {
			GlStateManager._texParameter(3553, 10241, bl2 ? 9986 : 9728);
			GlStateManager._texParameter(3553, 10240, 9728);
		}
	}

	private void checkAllocated() {
		if (this.pixels == 0L) {
			throw new IllegalStateException("Image is not allocated.");
		}
	}

	public void close() {
		if (this.pixels != 0L) {
			if (this.useStbFree) {
				STBImage.nstbi_image_free(this.pixels);
			} else {
				MemoryUtil.nmemFree(this.pixels);
			}
		}

		this.pixels = 0L;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public NativeImage.Format format() {
		return this.format;
	}

	public int getPixelRGBA(int i, int j) {
		if (this.format != NativeImage.Format.RGBA) {
			throw new IllegalArgumentException(String.format("getPixelRGBA only works on RGBA images; have %s", this.format));
		} else if (i <= this.width && j <= this.height) {
			this.checkAllocated();
			long l = (long)((i + j * this.width) * 4);
			return MemoryUtil.memGetInt(this.pixels + l);
		} else {
			throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", i, j, this.width, this.height));
		}
	}

	public void setPixelRGBA(int i, int j, int k) {
		if (this.format != NativeImage.Format.RGBA) {
			throw new IllegalArgumentException(String.format("getPixelRGBA only works on RGBA images; have %s", this.format));
		} else if (i <= this.width && j <= this.height) {
			this.checkAllocated();
			long l = (long)((i + j * this.width) * 4);
			MemoryUtil.memPutInt(this.pixels + l, k);
		} else {
			throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", i, j, this.width, this.height));
		}
	}

	public byte getLuminanceOrAlpha(int i, int j) {
		if (!this.format.hasLuminanceOrAlpha()) {
			throw new IllegalArgumentException(String.format("no luminance or alpha in %s", this.format));
		} else if (i <= this.width && j <= this.height) {
			int k = (i + j * this.width) * this.format.components() + this.format.luminanceOrAlphaOffset() / 8;
			return MemoryUtil.memGetByte(this.pixels + (long)k);
		} else {
			throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", i, j, this.width, this.height));
		}
	}

	public void blendPixel(int i, int j, int k) {
		if (this.format != NativeImage.Format.RGBA) {
			throw new UnsupportedOperationException("Can only call blendPixel with RGBA format");
		} else {
			int l = this.getPixelRGBA(i, j);
			float f = (float)getA(k) / 255.0F;
			float g = (float)getB(k) / 255.0F;
			float h = (float)getG(k) / 255.0F;
			float m = (float)getR(k) / 255.0F;
			float n = (float)getA(l) / 255.0F;
			float o = (float)getB(l) / 255.0F;
			float p = (float)getG(l) / 255.0F;
			float q = (float)getR(l) / 255.0F;
			float s = 1.0F - f;
			float t = f * f + n * s;
			float u = g * f + o * s;
			float v = h * f + p * s;
			float w = m * f + q * s;
			if (t > 1.0F) {
				t = 1.0F;
			}

			if (u > 1.0F) {
				u = 1.0F;
			}

			if (v > 1.0F) {
				v = 1.0F;
			}

			if (w > 1.0F) {
				w = 1.0F;
			}

			int x = (int)(t * 255.0F);
			int y = (int)(u * 255.0F);
			int z = (int)(v * 255.0F);
			int aa = (int)(w * 255.0F);
			this.setPixelRGBA(i, j, combine(x, y, z, aa));
		}
	}

	@Deprecated
	public int[] makePixelArray() {
		if (this.format != NativeImage.Format.RGBA) {
			throw new UnsupportedOperationException("can only call makePixelArray for RGBA images.");
		} else {
			this.checkAllocated();
			int[] is = new int[this.getWidth() * this.getHeight()];

			for (int i = 0; i < this.getHeight(); i++) {
				for (int j = 0; j < this.getWidth(); j++) {
					int k = this.getPixelRGBA(j, i);
					int l = getA(k);
					int m = getB(k);
					int n = getG(k);
					int o = getR(k);
					int p = l << 24 | o << 16 | n << 8 | m;
					is[j + i * this.getWidth()] = p;
				}
			}

			return is;
		}
	}

	public void upload(int i, int j, int k, boolean bl) {
		this.upload(i, j, k, 0, 0, this.width, this.height, false, bl);
	}

	public void upload(int i, int j, int k, int l, int m, int n, int o, boolean bl, boolean bl2) {
		this.upload(i, j, k, l, m, n, o, false, false, bl, bl2);
	}

	public void upload(int i, int j, int k, int l, int m, int n, int o, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
		if (!RenderSystem.isOnRenderThreadOrInit()) {
			RenderSystem.recordRenderCall(() -> this._upload(i, j, k, l, m, n, o, bl, bl2, bl3, bl4));
		} else {
			this._upload(i, j, k, l, m, n, o, bl, bl2, bl3, bl4);
		}
	}

	private void _upload(int i, int j, int k, int l, int m, int n, int o, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		this.checkAllocated();
		setFilter(bl, bl3);
		setClamp(bl2);
		if (n == this.getWidth()) {
			GlStateManager._pixelStore(3314, 0);
		} else {
			GlStateManager._pixelStore(3314, this.getWidth());
		}

		GlStateManager._pixelStore(3316, l);
		GlStateManager._pixelStore(3315, m);
		this.format.setUnpackPixelStoreState();
		GlStateManager._texSubImage2D(3553, i, j, k, n, o, this.format.glFormat(), 5121, this.pixels);
		if (bl4) {
			this.close();
		}
	}

	public void downloadTexture(int i, boolean bl) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		this.checkAllocated();
		this.format.setPackPixelStoreState();
		GlStateManager._getTexImage(3553, i, this.format.glFormat(), 5121, this.pixels);
		if (bl && this.format.hasAlpha()) {
			for (int j = 0; j < this.getHeight(); j++) {
				for (int k = 0; k < this.getWidth(); k++) {
					this.setPixelRGBA(k, j, this.getPixelRGBA(k, j) | 255 << this.format.alphaOffset());
				}
			}
		}
	}

	public void writeToFile(File file) throws IOException {
		this.writeToFile(file.toPath());
	}

	public void copyFromFont(STBTTFontinfo sTBTTFontinfo, int i, int j, int k, float f, float g, float h, float l, int m, int n) {
		if (m < 0 || m + j > this.getWidth() || n < 0 || n + k > this.getHeight()) {
			throw new IllegalArgumentException(String.format("Out of bounds: start: (%s, %s) (size: %sx%s); size: %sx%s", m, n, j, k, this.getWidth(), this.getHeight()));
		} else if (this.format.components() != 1) {
			throw new IllegalArgumentException("Can only write fonts into 1-component images.");
		} else {
			STBTruetype.nstbtt_MakeGlyphBitmapSubpixel(
				sTBTTFontinfo.address(), this.pixels + (long)m + (long)(n * this.getWidth()), j, k, this.getWidth(), f, g, h, l, i
			);
		}
	}

	public void writeToFile(Path path) throws IOException {
		if (!this.format.supportedByStb()) {
			throw new UnsupportedOperationException("Don't know how to write format " + this.format);
		} else {
			this.checkAllocated();
			WritableByteChannel writableByteChannel = Files.newByteChannel(path, OPEN_OPTIONS);
			Throwable var3 = null;

			try {
				if (!this.writeToChannel(writableByteChannel)) {
					throw new IOException("Could not write image to the PNG file \"" + path.toAbsolutePath() + "\": " + STBImage.stbi_failure_reason());
				}
			} catch (Throwable var12) {
				var3 = var12;
				throw var12;
			} finally {
				if (writableByteChannel != null) {
					if (var3 != null) {
						try {
							writableByteChannel.close();
						} catch (Throwable var11) {
							var3.addSuppressed(var11);
						}
					} else {
						writableByteChannel.close();
					}
				}
			}
		}
	}

	public byte[] asByteArray() throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		Throwable var2 = null;

		byte[] var5;
		try {
			WritableByteChannel writableByteChannel = Channels.newChannel(byteArrayOutputStream);
			Throwable var4 = null;

			try {
				if (!this.writeToChannel(writableByteChannel)) {
					throw new IOException("Could not write image to byte array: " + STBImage.stbi_failure_reason());
				}

				var5 = byteArrayOutputStream.toByteArray();
			} catch (Throwable var28) {
				var4 = var28;
				throw var28;
			} finally {
				if (writableByteChannel != null) {
					if (var4 != null) {
						try {
							writableByteChannel.close();
						} catch (Throwable var27) {
							var4.addSuppressed(var27);
						}
					} else {
						writableByteChannel.close();
					}
				}
			}
		} catch (Throwable var30) {
			var2 = var30;
			throw var30;
		} finally {
			if (byteArrayOutputStream != null) {
				if (var2 != null) {
					try {
						byteArrayOutputStream.close();
					} catch (Throwable var26) {
						var2.addSuppressed(var26);
					}
				} else {
					byteArrayOutputStream.close();
				}
			}
		}

		return var5;
	}

	private boolean writeToChannel(WritableByteChannel writableByteChannel) throws IOException {
		NativeImage.WriteCallback writeCallback = new NativeImage.WriteCallback(writableByteChannel);

		boolean var4;
		try {
			int i = Math.min(this.getHeight(), Integer.MAX_VALUE / this.getWidth() / this.format.components());
			if (i < this.getHeight()) {
				LOGGER.warn("Dropping image height from {} to {} to fit the size into 32-bit signed int", this.getHeight(), i);
			}

			if (STBImageWrite.nstbi_write_png_to_func(writeCallback.address(), 0L, this.getWidth(), i, this.format.components(), this.pixels, 0) != 0) {
				writeCallback.throwIfException();
				return true;
			}

			var4 = false;
		} finally {
			writeCallback.free();
		}

		return var4;
	}

	public void copyFrom(NativeImage nativeImage) {
		if (nativeImage.format() != this.format) {
			throw new UnsupportedOperationException("Image formats don't match.");
		} else {
			int i = this.format.components();
			this.checkAllocated();
			nativeImage.checkAllocated();
			if (this.width == nativeImage.width) {
				MemoryUtil.memCopy(nativeImage.pixels, this.pixels, Math.min(this.size, nativeImage.size));
			} else {
				int j = Math.min(this.getWidth(), nativeImage.getWidth());
				int k = Math.min(this.getHeight(), nativeImage.getHeight());

				for (int l = 0; l < k; l++) {
					int m = l * nativeImage.getWidth() * i;
					int n = l * this.getWidth() * i;
					MemoryUtil.memCopy(nativeImage.pixels + (long)m, this.pixels + (long)n, (long)j);
				}
			}
		}
	}

	public void fillRect(int i, int j, int k, int l, int m) {
		for (int n = j; n < j + l; n++) {
			for (int o = i; o < i + k; o++) {
				this.setPixelRGBA(o, n, m);
			}
		}
	}

	public void copyRect(int i, int j, int k, int l, int m, int n, boolean bl, boolean bl2) {
		for (int o = 0; o < n; o++) {
			for (int p = 0; p < m; p++) {
				int q = bl ? m - 1 - p : p;
				int r = bl2 ? n - 1 - o : o;
				int s = this.getPixelRGBA(i + p, j + o);
				this.setPixelRGBA(i + k + q, j + l + r, s);
			}
		}
	}

	public void flipY() {
		this.checkAllocated();

		try (MemoryStack memoryStack = MemoryStack.stackPush()) {
			int i = this.format.components();
			int j = this.getWidth() * i;
			long l = memoryStack.nmalloc(j);

			for (int k = 0; k < this.getHeight() / 2; k++) {
				int m = k * this.getWidth() * i;
				int n = (this.getHeight() - 1 - k) * this.getWidth() * i;
				MemoryUtil.memCopy(this.pixels + (long)m, l, (long)j);
				MemoryUtil.memCopy(this.pixels + (long)n, this.pixels + (long)m, (long)j);
				MemoryUtil.memCopy(l, this.pixels + (long)n, (long)j);
			}
		}
	}

	public void resizeSubRectTo(int i, int j, int k, int l, NativeImage nativeImage) {
		this.checkAllocated();
		if (nativeImage.format() != this.format) {
			throw new UnsupportedOperationException("resizeSubRectTo only works for images of the same format.");
		} else {
			int m = this.format.components();
			STBImageResize.nstbir_resize_uint8(
				this.pixels + (long)((i + j * this.getWidth()) * m), k, l, this.getWidth() * m, nativeImage.pixels, nativeImage.getWidth(), nativeImage.getHeight(), 0, m
			);
		}
	}

	public void untrack() {
		DebugMemoryUntracker.untrack(this.pixels);
	}

	public static NativeImage fromBase64(String string) throws IOException {
		byte[] bs = Base64.getDecoder().decode(string.replaceAll("\n", "").getBytes(Charsets.UTF_8));

		NativeImage var5;
		try (MemoryStack memoryStack = MemoryStack.stackPush()) {
			ByteBuffer byteBuffer = memoryStack.malloc(bs.length);
			byteBuffer.put(bs);
			byteBuffer.rewind();
			var5 = read(byteBuffer);
		}

		return var5;
	}

	public static int getA(int i) {
		return i >> 24 & 0xFF;
	}

	public static int getR(int i) {
		return i >> 0 & 0xFF;
	}

	public static int getG(int i) {
		return i >> 8 & 0xFF;
	}

	public static int getB(int i) {
		return i >> 16 & 0xFF;
	}

	public static int combine(int i, int j, int k, int l) {
		return (i & 0xFF) << 24 | (j & 0xFF) << 16 | (k & 0xFF) << 8 | (l & 0xFF) << 0;
	}

	@Environment(EnvType.CLIENT)
	public static enum Format {
		RGBA(4, 6408, true, true, true, false, true, 0, 8, 16, 255, 24, true),
		RGB(3, 6407, true, true, true, false, false, 0, 8, 16, 255, 255, true),
		LUMINANCE_ALPHA(2, 6410, false, false, false, true, true, 255, 255, 255, 0, 8, true),
		LUMINANCE(1, 6409, false, false, false, true, false, 0, 0, 0, 0, 255, true);

		private final int components;
		private final int glFormat;
		private final boolean hasRed;
		private final boolean hasGreen;
		private final boolean hasBlue;
		private final boolean hasLuminance;
		private final boolean hasAlpha;
		private final int redOffset;
		private final int greenOffset;
		private final int blueOffset;
		private final int luminanceOffset;
		private final int alphaOffset;
		private final boolean supportedByStb;

		private Format(int j, int k, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, int l, int m, int n, int o, int p, boolean bl6) {
			this.components = j;
			this.glFormat = k;
			this.hasRed = bl;
			this.hasGreen = bl2;
			this.hasBlue = bl3;
			this.hasLuminance = bl4;
			this.hasAlpha = bl5;
			this.redOffset = l;
			this.greenOffset = m;
			this.blueOffset = n;
			this.luminanceOffset = o;
			this.alphaOffset = p;
			this.supportedByStb = bl6;
		}

		public int components() {
			return this.components;
		}

		public void setPackPixelStoreState() {
			RenderSystem.assertThread(RenderSystem::isOnRenderThread);
			GlStateManager._pixelStore(3333, this.components());
		}

		public void setUnpackPixelStoreState() {
			RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
			GlStateManager._pixelStore(3317, this.components());
		}

		public int glFormat() {
			return this.glFormat;
		}

		public boolean hasAlpha() {
			return this.hasAlpha;
		}

		public int alphaOffset() {
			return this.alphaOffset;
		}

		public boolean hasLuminanceOrAlpha() {
			return this.hasLuminance || this.hasAlpha;
		}

		public int luminanceOrAlphaOffset() {
			return this.hasLuminance ? this.luminanceOffset : this.alphaOffset;
		}

		public boolean supportedByStb() {
			return this.supportedByStb;
		}

		private static NativeImage.Format getStbFormat(int i) {
			switch (i) {
				case 1:
					return LUMINANCE;
				case 2:
					return LUMINANCE_ALPHA;
				case 3:
					return RGB;
				case 4:
				default:
					return RGBA;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum InternalGlFormat {
		RGBA(6408),
		RGB(6407),
		LUMINANCE_ALPHA(6410),
		LUMINANCE(6409),
		INTENSITY(32841);

		private final int glFormat;

		private InternalGlFormat(int j) {
			this.glFormat = j;
		}

		int glFormat() {
			return this.glFormat;
		}
	}

	@Environment(EnvType.CLIENT)
	static class WriteCallback extends STBIWriteCallback {
		private final WritableByteChannel output;
		@Nullable
		private IOException exception;

		private WriteCallback(WritableByteChannel writableByteChannel) {
			this.output = writableByteChannel;
		}

		@Override
		public void invoke(long l, long m, int i) {
			ByteBuffer byteBuffer = getData(m, i);

			try {
				this.output.write(byteBuffer);
			} catch (IOException var8) {
				this.exception = var8;
			}
		}

		public void throwIfException() throws IOException {
			if (this.exception != null) {
				throw this.exception;
			}
		}
	}
}
