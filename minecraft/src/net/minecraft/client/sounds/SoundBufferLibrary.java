package net.minecraft.client.sounds;

import com.google.common.collect.Maps;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

@Environment(EnvType.CLIENT)
public class SoundBufferLibrary {
	private final ResourceProvider resourceManager;
	private final Map<ResourceLocation, CompletableFuture<SoundBuffer>> cache = Maps.<ResourceLocation, CompletableFuture<SoundBuffer>>newHashMap();

	public SoundBufferLibrary(ResourceProvider resourceProvider) {
		this.resourceManager = resourceProvider;
	}

	public CompletableFuture<SoundBuffer> getCompleteBuffer(ResourceLocation resourceLocation) {
		return (CompletableFuture<SoundBuffer>)this.cache.computeIfAbsent(resourceLocation, resourceLocationx -> CompletableFuture.supplyAsync(() -> {
				try {
					InputStream inputStream = this.resourceManager.open(resourceLocationx);

					SoundBuffer var5;
					try {
						FiniteAudioStream finiteAudioStream = new JOrbisAudioStream(inputStream);

						try {
							ByteBuffer byteBuffer = finiteAudioStream.readAll();
							var5 = new SoundBuffer(byteBuffer, finiteAudioStream.getFormat());
						} catch (Throwable var8) {
							try {
								finiteAudioStream.close();
							} catch (Throwable var7) {
								var8.addSuppressed(var7);
							}

							throw var8;
						}

						finiteAudioStream.close();
					} catch (Throwable var9) {
						if (inputStream != null) {
							try {
								inputStream.close();
							} catch (Throwable var6) {
								var9.addSuppressed(var6);
							}
						}

						throw var9;
					}

					if (inputStream != null) {
						inputStream.close();
					}

					return var5;
				} catch (IOException var10) {
					throw new CompletionException(var10);
				}
			}, Util.nonCriticalIoPool()));
	}

	public CompletableFuture<AudioStream> getStream(ResourceLocation resourceLocation, boolean bl) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				InputStream inputStream = this.resourceManager.open(resourceLocation);
				return (AudioStream)(bl ? new LoopingAudioStream(JOrbisAudioStream::new, inputStream) : new JOrbisAudioStream(inputStream));
			} catch (IOException var4) {
				throw new CompletionException(var4);
			}
		}, Util.nonCriticalIoPool());
	}

	public void clear() {
		this.cache.values().forEach(completableFuture -> completableFuture.thenAccept(SoundBuffer::discardAlBuffer));
		this.cache.clear();
	}

	public CompletableFuture<?> preload(Collection<Sound> collection) {
		return CompletableFuture.allOf(
			(CompletableFuture[])collection.stream().map(sound -> this.getCompleteBuffer(sound.getPath())).toArray(CompletableFuture[]::new)
		);
	}
}
