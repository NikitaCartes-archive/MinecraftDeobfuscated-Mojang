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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public class SoundBufferLibrary {
	private final ResourceManager resourceManager;
	private final Map<ResourceLocation, CompletableFuture<SoundBuffer>> cache = Maps.<ResourceLocation, CompletableFuture<SoundBuffer>>newHashMap();

	public SoundBufferLibrary(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}

	public CompletableFuture<SoundBuffer> getCompleteBuffer(ResourceLocation resourceLocation) {
		return (CompletableFuture<SoundBuffer>)this.cache.computeIfAbsent(resourceLocation, resourceLocationx -> CompletableFuture.supplyAsync(() -> {
				try {
					Resource resource = this.resourceManager.getResource(resourceLocationx);

					SoundBuffer var6;
					try {
						InputStream inputStream = resource.getInputStream();

						try {
							OggAudioStream oggAudioStream = new OggAudioStream(inputStream);

							try {
								ByteBuffer byteBuffer = oggAudioStream.readAll();
								var6 = new SoundBuffer(byteBuffer, oggAudioStream.getFormat());
							} catch (Throwable var10) {
								try {
									oggAudioStream.close();
								} catch (Throwable var9) {
									var10.addSuppressed(var9);
								}

								throw var10;
							}

							oggAudioStream.close();
						} catch (Throwable var11) {
							if (inputStream != null) {
								try {
									inputStream.close();
								} catch (Throwable var8) {
									var11.addSuppressed(var8);
								}
							}

							throw var11;
						}

						if (inputStream != null) {
							inputStream.close();
						}
					} catch (Throwable var12) {
						if (resource != null) {
							try {
								resource.close();
							} catch (Throwable var7) {
								var12.addSuppressed(var7);
							}
						}

						throw var12;
					}

					if (resource != null) {
						resource.close();
					}

					return var6;
				} catch (IOException var13) {
					throw new CompletionException(var13);
				}
			}, Util.backgroundExecutor()));
	}

	public CompletableFuture<AudioStream> getStream(ResourceLocation resourceLocation, boolean bl) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				Resource resource = this.resourceManager.getResource(resourceLocation);
				InputStream inputStream = resource.getInputStream();
				return (AudioStream)(bl ? new LoopingAudioStream(OggAudioStream::new, inputStream) : new OggAudioStream(inputStream));
			} catch (IOException var5) {
				throw new CompletionException(var5);
			}
		}, Util.backgroundExecutor());
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
