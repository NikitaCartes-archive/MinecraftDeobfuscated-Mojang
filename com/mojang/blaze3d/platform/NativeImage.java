/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.platform.DebugMemoryUntracker;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.FastColor;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public final class NativeImage
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<StandardOpenOption> OPEN_OPTIONS = EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    private final Format format;
    private final int width;
    private final int height;
    private final boolean useStbFree;
    private long pixels;
    private final long size;

    public NativeImage(int i, int j, boolean bl) {
        this(Format.RGBA, i, j, bl);
    }

    public NativeImage(Format format, int i, int j, boolean bl) {
        if (i <= 0 || j <= 0) {
            throw new IllegalArgumentException("Invalid texture size: " + i + "x" + j);
        }
        this.format = format;
        this.width = i;
        this.height = j;
        this.size = (long)i * (long)j * (long)format.components();
        this.useStbFree = false;
        this.pixels = bl ? MemoryUtil.nmemCalloc(1L, this.size) : MemoryUtil.nmemAlloc(this.size);
    }

    private NativeImage(Format format, int i, int j, boolean bl, long l) {
        if (i <= 0 || j <= 0) {
            throw new IllegalArgumentException("Invalid texture size: " + i + "x" + j);
        }
        this.format = format;
        this.width = i;
        this.height = j;
        this.useStbFree = bl;
        this.pixels = l;
        this.size = (long)i * (long)j * (long)format.components();
    }

    public String toString() {
        return "NativeImage[" + this.format + " " + this.width + "x" + this.height + "@" + this.pixels + (this.useStbFree ? "S" : "N") + "]";
    }

    private boolean isOutsideBounds(int i, int j) {
        return i < 0 || i >= this.width || j < 0 || j >= this.height;
    }

    public static NativeImage read(InputStream inputStream) throws IOException {
        return NativeImage.read(Format.RGBA, inputStream);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static NativeImage read(@Nullable Format format, InputStream inputStream) throws IOException {
        ByteBuffer byteBuffer = null;
        try {
            byteBuffer = TextureUtil.readResource(inputStream);
            byteBuffer.rewind();
            NativeImage nativeImage = NativeImage.read(format, byteBuffer);
            return nativeImage;
        } finally {
            MemoryUtil.memFree(byteBuffer);
            IOUtils.closeQuietly(inputStream);
        }
    }

    public static NativeImage read(ByteBuffer byteBuffer) throws IOException {
        return NativeImage.read(Format.RGBA, byteBuffer);
    }

    public static NativeImage read(byte[] bs) throws IOException {
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = memoryStack.malloc(bs.length);
            byteBuffer.put(bs);
            byteBuffer.rewind();
            NativeImage nativeImage = NativeImage.read(byteBuffer);
            return nativeImage;
        }
    }

    public static NativeImage read(@Nullable Format format, ByteBuffer byteBuffer) throws IOException {
        if (format != null && !format.supportedByStb()) {
            throw new UnsupportedOperationException("Don't know how to read format " + format);
        }
        if (MemoryUtil.memAddress(byteBuffer) == 0L) {
            throw new IllegalArgumentException("Invalid buffer");
        }
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            IntBuffer intBuffer = memoryStack.mallocInt(1);
            IntBuffer intBuffer2 = memoryStack.mallocInt(1);
            IntBuffer intBuffer3 = memoryStack.mallocInt(1);
            ByteBuffer byteBuffer2 = STBImage.stbi_load_from_memory(byteBuffer, intBuffer, intBuffer2, intBuffer3, format == null ? 0 : format.components);
            if (byteBuffer2 == null) {
                throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
            }
            NativeImage nativeImage = new NativeImage(format == null ? Format.getStbFormat(intBuffer3.get(0)) : format, intBuffer.get(0), intBuffer2.get(0), true, MemoryUtil.memAddress(byteBuffer2));
            return nativeImage;
        }
    }

    private static void setFilter(boolean bl, boolean bl2) {
        RenderSystem.assertOnRenderThreadOrInit();
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

    @Override
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

    public Format format() {
        return this.format;
    }

    public int getPixelRGBA(int i, int j) {
        if (this.format != Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "getPixelRGBA only works on RGBA images; have %s", new Object[]{this.format}));
        }
        if (this.isOutsideBounds(i, j)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", i, j, this.width, this.height));
        }
        this.checkAllocated();
        long l = ((long)i + (long)j * (long)this.width) * 4L;
        return MemoryUtil.memGetInt(this.pixels + l);
    }

    public void setPixelRGBA(int i, int j, int k) {
        if (this.format != Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "getPixelRGBA only works on RGBA images; have %s", new Object[]{this.format}));
        }
        if (this.isOutsideBounds(i, j)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", i, j, this.width, this.height));
        }
        this.checkAllocated();
        long l = ((long)i + (long)j * (long)this.width) * 4L;
        MemoryUtil.memPutInt(this.pixels + l, k);
    }

    public NativeImage mappedCopy(IntUnaryOperator intUnaryOperator) {
        if (this.format != Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "function application only works on RGBA images; have %s", new Object[]{this.format}));
        }
        this.checkAllocated();
        NativeImage nativeImage = new NativeImage(this.width, this.height, false);
        int i = this.width * this.height;
        IntBuffer intBuffer = MemoryUtil.memIntBuffer(this.pixels, i);
        IntBuffer intBuffer2 = MemoryUtil.memIntBuffer(nativeImage.pixels, i);
        for (int j = 0; j < i; ++j) {
            intBuffer2.put(j, intUnaryOperator.applyAsInt(intBuffer.get(j)));
        }
        return nativeImage;
    }

    public int[] getPixelsRGBA() {
        if (this.format != Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "getPixelsRGBA only works on RGBA images; have %s", new Object[]{this.format}));
        }
        this.checkAllocated();
        int[] is = new int[this.width * this.height];
        MemoryUtil.memIntBuffer(this.pixels, this.width * this.height).get(is);
        return is;
    }

    public void setPixelLuminance(int i, int j, byte b) {
        RenderSystem.assertOnRenderThread();
        if (!this.format.hasLuminance()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "setPixelLuminance only works on image with luminance; have %s", new Object[]{this.format}));
        }
        if (this.isOutsideBounds(i, j)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", i, j, this.width, this.height));
        }
        this.checkAllocated();
        long l = ((long)i + (long)j * (long)this.width) * (long)this.format.components() + (long)(this.format.luminanceOffset() / 8);
        MemoryUtil.memPutByte(this.pixels + l, b);
    }

    public byte getRedOrLuminance(int i, int j) {
        RenderSystem.assertOnRenderThread();
        if (!this.format.hasLuminanceOrRed()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "no red or luminance in %s", new Object[]{this.format}));
        }
        if (this.isOutsideBounds(i, j)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", i, j, this.width, this.height));
        }
        int k = (i + j * this.width) * this.format.components() + this.format.luminanceOrRedOffset() / 8;
        return MemoryUtil.memGetByte(this.pixels + (long)k);
    }

    public byte getGreenOrLuminance(int i, int j) {
        RenderSystem.assertOnRenderThread();
        if (!this.format.hasLuminanceOrGreen()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "no green or luminance in %s", new Object[]{this.format}));
        }
        if (this.isOutsideBounds(i, j)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", i, j, this.width, this.height));
        }
        int k = (i + j * this.width) * this.format.components() + this.format.luminanceOrGreenOffset() / 8;
        return MemoryUtil.memGetByte(this.pixels + (long)k);
    }

    public byte getBlueOrLuminance(int i, int j) {
        RenderSystem.assertOnRenderThread();
        if (!this.format.hasLuminanceOrBlue()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "no blue or luminance in %s", new Object[]{this.format}));
        }
        if (this.isOutsideBounds(i, j)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", i, j, this.width, this.height));
        }
        int k = (i + j * this.width) * this.format.components() + this.format.luminanceOrBlueOffset() / 8;
        return MemoryUtil.memGetByte(this.pixels + (long)k);
    }

    public byte getLuminanceOrAlpha(int i, int j) {
        if (!this.format.hasLuminanceOrAlpha()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "no luminance or alpha in %s", new Object[]{this.format}));
        }
        if (this.isOutsideBounds(i, j)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", i, j, this.width, this.height));
        }
        int k = (i + j * this.width) * this.format.components() + this.format.luminanceOrAlphaOffset() / 8;
        return MemoryUtil.memGetByte(this.pixels + (long)k);
    }

    public void blendPixel(int i, int j, int k) {
        if (this.format != Format.RGBA) {
            throw new UnsupportedOperationException("Can only call blendPixel with RGBA format");
        }
        int l = this.getPixelRGBA(i, j);
        float f = (float)FastColor.ABGR32.alpha(k) / 255.0f;
        float g = (float)FastColor.ABGR32.blue(k) / 255.0f;
        float h = (float)FastColor.ABGR32.green(k) / 255.0f;
        float m = (float)FastColor.ABGR32.red(k) / 255.0f;
        float n = (float)FastColor.ABGR32.alpha(l) / 255.0f;
        float o = (float)FastColor.ABGR32.blue(l) / 255.0f;
        float p = (float)FastColor.ABGR32.green(l) / 255.0f;
        float q = (float)FastColor.ABGR32.red(l) / 255.0f;
        float r = f;
        float s = 1.0f - f;
        float t = f * r + n * s;
        float u = g * r + o * s;
        float v = h * r + p * s;
        float w = m * r + q * s;
        if (t > 1.0f) {
            t = 1.0f;
        }
        if (u > 1.0f) {
            u = 1.0f;
        }
        if (v > 1.0f) {
            v = 1.0f;
        }
        if (w > 1.0f) {
            w = 1.0f;
        }
        int x = (int)(t * 255.0f);
        int y = (int)(u * 255.0f);
        int z = (int)(v * 255.0f);
        int aa = (int)(w * 255.0f);
        this.setPixelRGBA(i, j, FastColor.ABGR32.color(x, y, z, aa));
    }

    @Deprecated
    public int[] makePixelArray() {
        if (this.format != Format.RGBA) {
            throw new UnsupportedOperationException("can only call makePixelArray for RGBA images.");
        }
        this.checkAllocated();
        int[] is = new int[this.getWidth() * this.getHeight()];
        for (int i = 0; i < this.getHeight(); ++i) {
            for (int j = 0; j < this.getWidth(); ++j) {
                int k = this.getPixelRGBA(j, i);
                is[j + i * this.getWidth()] = FastColor.ARGB32.color(FastColor.ABGR32.alpha(k), FastColor.ABGR32.red(k), FastColor.ABGR32.green(k), FastColor.ABGR32.blue(k));
            }
        }
        return is;
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
        RenderSystem.assertOnRenderThreadOrInit();
        this.checkAllocated();
        NativeImage.setFilter(bl, bl3);
        if (n == this.getWidth()) {
            GlStateManager._pixelStore(3314, 0);
        } else {
            GlStateManager._pixelStore(3314, this.getWidth());
        }
        GlStateManager._pixelStore(3316, l);
        GlStateManager._pixelStore(3315, m);
        this.format.setUnpackPixelStoreState();
        GlStateManager._texSubImage2D(3553, i, j, k, n, o, this.format.glFormat(), 5121, this.pixels);
        if (bl2) {
            GlStateManager._texParameter(3553, 10242, 33071);
            GlStateManager._texParameter(3553, 10243, 33071);
        }
        if (bl4) {
            this.close();
        }
    }

    public void downloadTexture(int i, boolean bl) {
        RenderSystem.assertOnRenderThread();
        this.checkAllocated();
        this.format.setPackPixelStoreState();
        GlStateManager._getTexImage(3553, i, this.format.glFormat(), 5121, this.pixels);
        if (bl && this.format.hasAlpha()) {
            for (int j = 0; j < this.getHeight(); ++j) {
                for (int k = 0; k < this.getWidth(); ++k) {
                    this.setPixelRGBA(k, j, this.getPixelRGBA(k, j) | 255 << this.format.alphaOffset());
                }
            }
        }
    }

    public void downloadDepthBuffer(float f) {
        RenderSystem.assertOnRenderThread();
        if (this.format.components() != 1) {
            throw new IllegalStateException("Depth buffer must be stored in NativeImage with 1 component.");
        }
        this.checkAllocated();
        this.format.setPackPixelStoreState();
        GlStateManager._readPixels(0, 0, this.width, this.height, 6402, 5121, this.pixels);
    }

    public void drawPixels() {
        RenderSystem.assertOnRenderThread();
        this.format.setUnpackPixelStoreState();
        GlStateManager._glDrawPixels(this.width, this.height, this.format.glFormat(), 5121, this.pixels);
    }

    public void writeToFile(File file) throws IOException {
        this.writeToFile(file.toPath());
    }

    public void copyFromFont(STBTTFontinfo sTBTTFontinfo, int i, int j, int k, float f, float g, float h, float l, int m, int n) {
        if (m < 0 || m + j > this.getWidth() || n < 0 || n + k > this.getHeight()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Out of bounds: start: (%s, %s) (size: %sx%s); size: %sx%s", m, n, j, k, this.getWidth(), this.getHeight()));
        }
        if (this.format.components() != 1) {
            throw new IllegalArgumentException("Can only write fonts into 1-component images.");
        }
        STBTruetype.nstbtt_MakeGlyphBitmapSubpixel(sTBTTFontinfo.address(), this.pixels + (long)m + (long)(n * this.getWidth()), j, k, this.getWidth(), f, g, h, l, i);
    }

    public void writeToFile(Path path) throws IOException {
        if (!this.format.supportedByStb()) {
            throw new UnsupportedOperationException("Don't know how to write format " + this.format);
        }
        this.checkAllocated();
        try (SeekableByteChannel writableByteChannel = Files.newByteChannel(path, OPEN_OPTIONS, new FileAttribute[0]);){
            if (!this.writeToChannel(writableByteChannel)) {
                throw new IOException("Could not write image to the PNG file \"" + path.toAbsolutePath() + "\": " + STBImage.stbi_failure_reason());
            }
        }
    }

    public byte[] asByteArray() throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();){
            byte[] byArray;
            block12: {
                WritableByteChannel writableByteChannel = Channels.newChannel(byteArrayOutputStream);
                try {
                    if (!this.writeToChannel(writableByteChannel)) {
                        throw new IOException("Could not write image to byte array: " + STBImage.stbi_failure_reason());
                    }
                    byArray = byteArrayOutputStream.toByteArray();
                    if (writableByteChannel == null) break block12;
                } catch (Throwable throwable) {
                    if (writableByteChannel != null) {
                        try {
                            writableByteChannel.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                writableByteChannel.close();
            }
            return byArray;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean writeToChannel(WritableByteChannel writableByteChannel) throws IOException {
        WriteCallback writeCallback = new WriteCallback(writableByteChannel);
        try {
            int i = Math.min(this.getHeight(), Integer.MAX_VALUE / this.getWidth() / this.format.components());
            if (i < this.getHeight()) {
                LOGGER.warn("Dropping image height from {} to {} to fit the size into 32-bit signed int", (Object)this.getHeight(), (Object)i);
            }
            if (STBImageWrite.nstbi_write_png_to_func(writeCallback.address(), 0L, this.getWidth(), i, this.format.components(), this.pixels, 0) == 0) {
                boolean bl = false;
                return bl;
            }
            writeCallback.throwIfException();
            boolean bl = true;
            return bl;
        } finally {
            writeCallback.free();
        }
    }

    public void copyFrom(NativeImage nativeImage) {
        if (nativeImage.format() != this.format) {
            throw new UnsupportedOperationException("Image formats don't match.");
        }
        int i = this.format.components();
        this.checkAllocated();
        nativeImage.checkAllocated();
        if (this.width == nativeImage.width) {
            MemoryUtil.memCopy(nativeImage.pixels, this.pixels, Math.min(this.size, nativeImage.size));
        } else {
            int j = Math.min(this.getWidth(), nativeImage.getWidth());
            int k = Math.min(this.getHeight(), nativeImage.getHeight());
            for (int l = 0; l < k; ++l) {
                int m = l * nativeImage.getWidth() * i;
                int n = l * this.getWidth() * i;
                MemoryUtil.memCopy(nativeImage.pixels + (long)m, this.pixels + (long)n, j);
            }
        }
    }

    public void fillRect(int i, int j, int k, int l, int m) {
        for (int n = j; n < j + l; ++n) {
            for (int o = i; o < i + k; ++o) {
                this.setPixelRGBA(o, n, m);
            }
        }
    }

    public void copyRect(int i, int j, int k, int l, int m, int n, boolean bl, boolean bl2) {
        this.copyRect(this, i, j, i + k, j + l, m, n, bl, bl2);
    }

    public void copyRect(NativeImage nativeImage, int i, int j, int k, int l, int m, int n, boolean bl, boolean bl2) {
        for (int o = 0; o < n; ++o) {
            for (int p = 0; p < m; ++p) {
                int q = bl ? m - 1 - p : p;
                int r = bl2 ? n - 1 - o : o;
                int s = this.getPixelRGBA(i + p, j + o);
                nativeImage.setPixelRGBA(k + q, l + r, s);
            }
        }
    }

    public void flipY() {
        this.checkAllocated();
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            int i = this.format.components();
            int j = this.getWidth() * i;
            long l = memoryStack.nmalloc(j);
            for (int k = 0; k < this.getHeight() / 2; ++k) {
                int m = k * this.getWidth() * i;
                int n = (this.getHeight() - 1 - k) * this.getWidth() * i;
                MemoryUtil.memCopy(this.pixels + (long)m, l, j);
                MemoryUtil.memCopy(this.pixels + (long)n, this.pixels + (long)m, j);
                MemoryUtil.memCopy(l, this.pixels + (long)n, j);
            }
        }
    }

    public void resizeSubRectTo(int i, int j, int k, int l, NativeImage nativeImage) {
        this.checkAllocated();
        if (nativeImage.format() != this.format) {
            throw new UnsupportedOperationException("resizeSubRectTo only works for images of the same format.");
        }
        int m = this.format.components();
        STBImageResize.nstbir_resize_uint8(this.pixels + (long)((i + j * this.getWidth()) * m), k, l, this.getWidth() * m, nativeImage.pixels, nativeImage.getWidth(), nativeImage.getHeight(), 0, m);
    }

    public void untrack() {
        DebugMemoryUntracker.untrack(this.pixels);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Format {
        RGBA(4, 6408, true, true, true, false, true, 0, 8, 16, 255, 24, true),
        RGB(3, 6407, true, true, true, false, false, 0, 8, 16, 255, 255, true),
        LUMINANCE_ALPHA(2, 33319, false, false, false, true, true, 255, 255, 255, 0, 8, true),
        LUMINANCE(1, 6403, false, false, false, true, false, 0, 0, 0, 0, 255, true);

        final int components;
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
            RenderSystem.assertOnRenderThread();
            GlStateManager._pixelStore(3333, this.components());
        }

        public void setUnpackPixelStoreState() {
            RenderSystem.assertOnRenderThreadOrInit();
            GlStateManager._pixelStore(3317, this.components());
        }

        public int glFormat() {
            return this.glFormat;
        }

        public boolean hasRed() {
            return this.hasRed;
        }

        public boolean hasGreen() {
            return this.hasGreen;
        }

        public boolean hasBlue() {
            return this.hasBlue;
        }

        public boolean hasLuminance() {
            return this.hasLuminance;
        }

        public boolean hasAlpha() {
            return this.hasAlpha;
        }

        public int redOffset() {
            return this.redOffset;
        }

        public int greenOffset() {
            return this.greenOffset;
        }

        public int blueOffset() {
            return this.blueOffset;
        }

        public int luminanceOffset() {
            return this.luminanceOffset;
        }

        public int alphaOffset() {
            return this.alphaOffset;
        }

        public boolean hasLuminanceOrRed() {
            return this.hasLuminance || this.hasRed;
        }

        public boolean hasLuminanceOrGreen() {
            return this.hasLuminance || this.hasGreen;
        }

        public boolean hasLuminanceOrBlue() {
            return this.hasLuminance || this.hasBlue;
        }

        public boolean hasLuminanceOrAlpha() {
            return this.hasLuminance || this.hasAlpha;
        }

        public int luminanceOrRedOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.redOffset;
        }

        public int luminanceOrGreenOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.greenOffset;
        }

        public int luminanceOrBlueOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.blueOffset;
        }

        public int luminanceOrAlphaOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.alphaOffset;
        }

        public boolean supportedByStb() {
            return this.supportedByStb;
        }

        static Format getStbFormat(int i) {
            switch (i) {
                case 1: {
                    return LUMINANCE;
                }
                case 2: {
                    return LUMINANCE_ALPHA;
                }
                case 3: {
                    return RGB;
                }
            }
            return RGBA;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class WriteCallback
    extends STBIWriteCallback {
        private final WritableByteChannel output;
        @Nullable
        private IOException exception;

        WriteCallback(WritableByteChannel writableByteChannel) {
            this.output = writableByteChannel;
        }

        @Override
        public void invoke(long l, long m, int i) {
            ByteBuffer byteBuffer = WriteCallback.getData(m, i);
            try {
                this.output.write(byteBuffer);
            } catch (IOException iOException) {
                this.exception = iOException;
            }
        }

        public void throwIfException() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum InternalGlFormat {
        RGBA(6408),
        RGB(6407),
        RG(33319),
        RED(6403);

        private final int glFormat;

        private InternalGlFormat(int j) {
            this.glFormat = j;
        }

        public int glFormat() {
            return this.glFormat;
        }
    }
}

