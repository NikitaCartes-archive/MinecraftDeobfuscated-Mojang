/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.sounds;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.sound.sampled.AudioFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.AudioStream;

@Environment(value=EnvType.CLIENT)
public class LoopingAudioStream
implements AudioStream {
    private final AudioStreamProvider provider;
    private AudioStream stream;
    private final BufferedInputStream bufferedInputStream;

    public LoopingAudioStream(AudioStreamProvider audioStreamProvider, InputStream inputStream) throws IOException {
        this.provider = audioStreamProvider;
        this.bufferedInputStream = new BufferedInputStream(inputStream);
        this.bufferedInputStream.mark(Integer.MAX_VALUE);
        this.stream = audioStreamProvider.create(new NoCloseBuffer(this.bufferedInputStream));
    }

    @Override
    public AudioFormat getFormat() {
        return this.stream.getFormat();
    }

    @Override
    public ByteBuffer read(int i) throws IOException {
        ByteBuffer byteBuffer = this.stream.read(i);
        if (!byteBuffer.hasRemaining()) {
            this.stream.close();
            this.bufferedInputStream.reset();
            this.stream = this.provider.create(new NoCloseBuffer(this.bufferedInputStream));
            byteBuffer = this.stream.read(i);
        }
        return byteBuffer;
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
        this.bufferedInputStream.close();
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface AudioStreamProvider {
        public AudioStream create(InputStream var1) throws IOException;
    }

    @Environment(value=EnvType.CLIENT)
    static class NoCloseBuffer
    extends FilterInputStream {
        NoCloseBuffer(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public void close() {
        }
    }
}

