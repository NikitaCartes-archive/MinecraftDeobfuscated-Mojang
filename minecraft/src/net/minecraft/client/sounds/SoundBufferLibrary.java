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
					Throwable var3 = null;

					SoundBuffer var9;
					try {
						InputStream inputStream = resource.getInputStream();
						Throwable var5 = null;

						try {
							OggAudioStream oggAudioStream = new OggAudioStream(inputStream);
							Throwable var7 = null;

							try {
								ByteBuffer byteBuffer = oggAudioStream.readAll();
								var9 = new SoundBuffer(byteBuffer, oggAudioStream.getFormat());
							} catch (Throwable var56) {
								var7 = var56;
								throw var56;
							} finally {
								if (oggAudioStream != null) {
									if (var7 != null) {
										try {
											oggAudioStream.close();
										} catch (Throwable var55) {
											var7.addSuppressed(var55);
										}
									} else {
										oggAudioStream.close();
									}
								}
							}
						} catch (Throwable var58) {
							var5 = var58;
							throw var58;
						} finally {
							if (inputStream != null) {
								if (var5 != null) {
									try {
										inputStream.close();
									} catch (Throwable var54) {
										var5.addSuppressed(var54);
									}
								} else {
									inputStream.close();
								}
							}
						}
					} catch (Throwable var60) {
						var3 = var60;
						throw var60;
					} finally {
						if (resource != null) {
							if (var3 != null) {
								try {
									resource.close();
								} catch (Throwable var53) {
									var3.addSuppressed(var53);
								}
							} else {
								resource.close();
							}
						}
					}

					return var9;
				} catch (IOException var62) {
					throw new CompletionException(var62);
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
