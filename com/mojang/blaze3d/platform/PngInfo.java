/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.platform;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.stb.STBIEOFCallback;
import org.lwjgl.stb.STBIIOCallbacks;
import org.lwjgl.stb.STBIReadCallback;
import org.lwjgl.stb.STBISkipCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public class PngInfo {
    public final int width;
    public final int height;

    public PngInfo(String string, InputStream inputStream) throws IOException {
        try (MemoryStack memoryStack = MemoryStack.stackPush();
             StbReader stbReader = PngInfo.createCallbacks(inputStream);
             STBIReadCallback sTBIReadCallback = STBIReadCallback.create(stbReader::read);
             STBISkipCallback sTBISkipCallback = STBISkipCallback.create(stbReader::skip);
             STBIEOFCallback sTBIEOFCallback = STBIEOFCallback.create(stbReader::eof);){
            STBIIOCallbacks sTBIIOCallbacks = STBIIOCallbacks.mallocStack(memoryStack);
            sTBIIOCallbacks.read(sTBIReadCallback);
            sTBIIOCallbacks.skip(sTBISkipCallback);
            sTBIIOCallbacks.eof(sTBIEOFCallback);
            IntBuffer intBuffer = memoryStack.mallocInt(1);
            IntBuffer intBuffer2 = memoryStack.mallocInt(1);
            IntBuffer intBuffer3 = memoryStack.mallocInt(1);
            if (!STBImage.stbi_info_from_callbacks(sTBIIOCallbacks, 0L, intBuffer, intBuffer2, intBuffer3)) {
                throw new IOException("Could not read info from the PNG file " + string + " " + STBImage.stbi_failure_reason());
            }
            this.width = intBuffer.get(0);
            this.height = intBuffer2.get(0);
        }
    }

    private static StbReader createCallbacks(InputStream inputStream) {
        if (inputStream instanceof FileInputStream) {
            return new StbReaderSeekableByteChannel(((FileInputStream)inputStream).getChannel());
        }
        return new StbReaderBufferedChannel(Channels.newChannel(inputStream));
    }

    @Environment(value=EnvType.CLIENT)
    static class StbReaderBufferedChannel
    extends StbReader {
        private static final int START_BUFFER_SIZE = 128;
        private final ReadableByteChannel channel;
        private long readBufferAddress = MemoryUtil.nmemAlloc(128L);
        private int bufferSize = 128;
        private int read;
        private int consumed;

        private StbReaderBufferedChannel(ReadableByteChannel readableByteChannel) {
            this.channel = readableByteChannel;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void fillReadBuffer(int i) throws IOException {
            ByteBuffer byteBuffer = MemoryUtil.memByteBuffer(this.readBufferAddress, this.bufferSize);
            if (i + this.consumed > this.bufferSize) {
                this.bufferSize = i + this.consumed;
                byteBuffer = MemoryUtil.memRealloc(byteBuffer, this.bufferSize);
                this.readBufferAddress = MemoryUtil.memAddress(byteBuffer);
            }
            byteBuffer.position(this.read);
            while (i + this.consumed > this.read) {
                try {
                    int j = this.channel.read(byteBuffer);
                    if (j != -1) continue;
                    break;
                } finally {
                    this.read = byteBuffer.position();
                }
            }
        }

        @Override
        public int read(long l, int i) throws IOException {
            this.fillReadBuffer(i);
            if (i + this.consumed > this.read) {
                i = this.read - this.consumed;
            }
            MemoryUtil.memCopy(this.readBufferAddress + (long)this.consumed, l, i);
            this.consumed += i;
            return i;
        }

        @Override
        public void skip(int i) throws IOException {
            if (i > 0) {
                this.fillReadBuffer(i);
                if (i + this.consumed > this.read) {
                    throw new EOFException("Can't skip past the EOF.");
                }
            }
            if (this.consumed + i < 0) {
                throw new IOException("Can't seek before the beginning: " + (this.consumed + i));
            }
            this.consumed += i;
        }

        @Override
        public void close() throws IOException {
            MemoryUtil.nmemFree(this.readBufferAddress);
            this.channel.close();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class StbReaderSeekableByteChannel
    extends StbReader {
        private final SeekableByteChannel channel;

        private StbReaderSeekableByteChannel(SeekableByteChannel seekableByteChannel) {
            this.channel = seekableByteChannel;
        }

        @Override
        public int read(long l, int i) throws IOException {
            ByteBuffer byteBuffer = MemoryUtil.memByteBuffer(l, i);
            return this.channel.read(byteBuffer);
        }

        @Override
        public void skip(int i) throws IOException {
            this.channel.position(this.channel.position() + (long)i);
        }

        @Override
        public int eof(long l) {
            return super.eof(l) != 0 && this.channel.isOpen() ? 1 : 0;
        }

        @Override
        public void close() throws IOException {
            this.channel.close();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static abstract class StbReader
    implements AutoCloseable {
        protected boolean closed;

        private StbReader() {
        }

        int read(long l, long m, int i) {
            try {
                return this.read(m, i);
            } catch (IOException iOException) {
                this.closed = true;
                return 0;
            }
        }

        void skip(long l, int i) {
            try {
                this.skip(i);
            } catch (IOException iOException) {
                this.closed = true;
            }
        }

        int eof(long l) {
            return this.closed ? 1 : 0;
        }

        protected abstract int read(long var1, int var3) throws IOException;

        protected abstract void skip(int var1) throws IOException;

        @Override
        public abstract void close() throws IOException;
    }
}

