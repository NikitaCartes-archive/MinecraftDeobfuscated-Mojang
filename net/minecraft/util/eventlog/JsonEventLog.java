/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.eventlog;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.Util;
import net.minecraft.util.eventlog.JsonEventLogReader;
import org.jetbrains.annotations.Nullable;

public class JsonEventLog<T>
implements Closeable {
    private static final Gson GSON = new Gson();
    private final Codec<T> codec;
    final FileChannel channel;
    private final AtomicInteger referenceCount = new AtomicInteger(1);

    public JsonEventLog(Codec<T> codec, FileChannel fileChannel) {
        this.codec = codec;
        this.channel = fileChannel;
    }

    public static <T> JsonEventLog<T> open(Codec<T> codec, Path path) throws IOException {
        FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
        return new JsonEventLog<T>(codec, fileChannel);
    }

    public void write(T object) throws IOException, JsonIOException {
        JsonElement jsonElement = Util.getOrThrow(this.codec.encodeStart(JsonOps.INSTANCE, (JsonElement)object), IOException::new);
        this.channel.position(this.channel.size());
        Writer writer = Channels.newWriter((WritableByteChannel)this.channel, StandardCharsets.UTF_8);
        GSON.toJson(jsonElement, (Appendable)writer);
        writer.write(10);
        writer.flush();
    }

    public JsonEventLogReader<T> openReader() throws IOException {
        if (this.referenceCount.get() <= 0) {
            throw new IOException("Event log has already been closed");
        }
        this.referenceCount.incrementAndGet();
        final JsonEventLogReader<T> jsonEventLogReader = JsonEventLogReader.create(this.codec, Channels.newReader((ReadableByteChannel)this.channel, StandardCharsets.UTF_8));
        return new JsonEventLogReader<T>(){
            private volatile long position;

            @Override
            @Nullable
            public T next() throws IOException {
                try {
                    JsonEventLog.this.channel.position(this.position);
                    Object t = jsonEventLogReader.next();
                    return t;
                } finally {
                    this.position = JsonEventLog.this.channel.position();
                }
            }

            @Override
            public void close() throws IOException {
                JsonEventLog.this.releaseReference();
            }
        };
    }

    @Override
    public void close() throws IOException {
        this.releaseReference();
    }

    void releaseReference() throws IOException {
        if (this.referenceCount.decrementAndGet() <= 0) {
            this.channel.close();
        }
    }
}

