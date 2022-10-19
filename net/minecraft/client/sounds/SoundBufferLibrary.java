/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.sounds;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.audio.OggAudioStream;
import com.mojang.blaze3d.audio.SoundBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.LoopingAudioStream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

@Environment(value=EnvType.CLIENT)
public class SoundBufferLibrary {
    private final ResourceProvider resourceManager;
    private final Map<ResourceLocation, CompletableFuture<SoundBuffer>> cache = Maps.newHashMap();

    public SoundBufferLibrary(ResourceProvider resourceProvider) {
        this.resourceManager = resourceProvider;
    }

    public CompletableFuture<SoundBuffer> getCompleteBuffer(ResourceLocation resourceLocation2) {
        return this.cache.computeIfAbsent(resourceLocation2, resourceLocation -> CompletableFuture.supplyAsync(() -> {
            try (InputStream inputStream = this.resourceManager.open((ResourceLocation)resourceLocation);){
                SoundBuffer soundBuffer;
                try (OggAudioStream oggAudioStream = new OggAudioStream(inputStream);){
                    ByteBuffer byteBuffer = oggAudioStream.readAll();
                    soundBuffer = new SoundBuffer(byteBuffer, oggAudioStream.getFormat());
                }
                return soundBuffer;
            } catch (IOException iOException) {
                throw new CompletionException(iOException);
            }
        }, Util.backgroundExecutor()));
    }

    public CompletableFuture<AudioStream> getStream(ResourceLocation resourceLocation, boolean bl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                InputStream inputStream = this.resourceManager.open(resourceLocation);
                return bl ? new LoopingAudioStream(OggAudioStream::new, inputStream) : new OggAudioStream(inputStream);
            } catch (IOException iOException) {
                throw new CompletionException(iOException);
            }
        }, Util.backgroundExecutor());
    }

    public void clear() {
        this.cache.values().forEach(completableFuture -> completableFuture.thenAccept(SoundBuffer::discardAlBuffer));
        this.cache.clear();
    }

    public CompletableFuture<?> preload(Collection<Sound> collection) {
        return CompletableFuture.allOf((CompletableFuture[])collection.stream().map(sound -> this.getCompleteBuffer(sound.getPath())).toArray(CompletableFuture[]::new));
    }
}

