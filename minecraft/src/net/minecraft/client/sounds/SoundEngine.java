package net.minecraft.client.sounds;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.Listener;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Environment(EnvType.CLIENT)
public class SoundEngine {
	private static final Marker MARKER = MarkerFactory.getMarker("SOUNDS");
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final float PITCH_MIN = 0.5F;
	private static final float PITCH_MAX = 2.0F;
	private static final float VOLUME_MIN = 0.0F;
	private static final float VOLUME_MAX = 1.0F;
	private static final int MIN_SOURCE_LIFETIME = 20;
	private static final Set<ResourceLocation> ONLY_WARN_ONCE = Sets.<ResourceLocation>newHashSet();
	private static final long DEFAULT_DEVICE_CHECK_INTERVAL_MS = 1000L;
	public static final String MISSING_SOUND = "FOR THE DEBUG!";
	public static final String OPEN_AL_SOFT_PREFIX = "OpenAL Soft on ";
	public static final int OPEN_AL_SOFT_PREFIX_LENGTH = "OpenAL Soft on ".length();
	private final SoundManager soundManager;
	private final Options options;
	private boolean loaded;
	private final Library library = new Library();
	private final Listener listener = this.library.getListener();
	private final SoundBufferLibrary soundBuffers;
	private final SoundEngineExecutor executor = new SoundEngineExecutor();
	private final ChannelAccess channelAccess = new ChannelAccess(this.library, this.executor);
	private int tickCount;
	private long lastDeviceCheckTime;
	private final AtomicReference<SoundEngine.DeviceCheckState> devicePoolState = new AtomicReference(SoundEngine.DeviceCheckState.NO_CHANGE);
	private final Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel = Maps.<SoundInstance, ChannelAccess.ChannelHandle>newHashMap();
	private final Multimap<SoundSource, SoundInstance> instanceBySource = HashMultimap.create();
	private final List<TickableSoundInstance> tickingSounds = Lists.<TickableSoundInstance>newArrayList();
	private final Map<SoundInstance, Integer> queuedSounds = Maps.<SoundInstance, Integer>newHashMap();
	private final Map<SoundInstance, Integer> soundDeleteTime = Maps.<SoundInstance, Integer>newHashMap();
	private final List<SoundEventListener> listeners = Lists.<SoundEventListener>newArrayList();
	private final List<TickableSoundInstance> queuedTickableSounds = Lists.<TickableSoundInstance>newArrayList();
	private final List<Sound> preloadQueue = Lists.<Sound>newArrayList();

	public SoundEngine(SoundManager soundManager, Options options, ResourceProvider resourceProvider) {
		this.soundManager = soundManager;
		this.options = options;
		this.soundBuffers = new SoundBufferLibrary(resourceProvider);
	}

	public void reload() {
		ONLY_WARN_ONCE.clear();

		for (SoundEvent soundEvent : BuiltInRegistries.SOUND_EVENT) {
			if (soundEvent != SoundEvents.EMPTY) {
				ResourceLocation resourceLocation = soundEvent.getLocation();
				if (this.soundManager.getSoundEvent(resourceLocation) == null) {
					LOGGER.warn("Missing sound for event: {}", BuiltInRegistries.SOUND_EVENT.getKey(soundEvent));
					ONLY_WARN_ONCE.add(resourceLocation);
				}
			}
		}

		this.destroy();
		this.loadLibrary();
	}

	private synchronized void loadLibrary() {
		if (!this.loaded) {
			try {
				String string = this.options.soundDevice().get();
				this.library.init("".equals(string) ? null : string, this.options.directionalAudio().get());
				this.listener.reset();
				this.listener.setGain(this.options.getSoundSourceVolume(SoundSource.MASTER));
				this.soundBuffers.preload(this.preloadQueue).thenRun(this.preloadQueue::clear);
				this.loaded = true;
				LOGGER.info(MARKER, "Sound engine started");
			} catch (RuntimeException var2) {
				LOGGER.error(MARKER, "Error starting SoundSystem. Turning off sounds & music", (Throwable)var2);
			}
		}
	}

	private float getVolume(@Nullable SoundSource soundSource) {
		return soundSource != null && soundSource != SoundSource.MASTER ? this.options.getSoundSourceVolume(soundSource) : 1.0F;
	}

	public void updateCategoryVolume(SoundSource soundSource, float f) {
		if (this.loaded) {
			if (soundSource == SoundSource.MASTER) {
				this.listener.setGain(f);
			} else {
				this.instanceToChannel.forEach((soundInstance, channelHandle) -> {
					float fx = this.calculateVolume(soundInstance);
					channelHandle.execute(channel -> {
						if (fx <= 0.0F) {
							channel.stop();
						} else {
							channel.setVolume(fx);
						}
					});
				});
			}
		}
	}

	public void destroy() {
		if (this.loaded) {
			this.stopAll();
			this.soundBuffers.clear();
			this.library.cleanup();
			this.loaded = false;
		}
	}

	public void stop(SoundInstance soundInstance) {
		if (this.loaded) {
			ChannelAccess.ChannelHandle channelHandle = (ChannelAccess.ChannelHandle)this.instanceToChannel.get(soundInstance);
			if (channelHandle != null) {
				channelHandle.execute(Channel::stop);
			}
		}
	}

	public void stopAll() {
		if (this.loaded) {
			this.executor.flush();
			this.instanceToChannel.values().forEach(channelHandle -> channelHandle.execute(Channel::stop));
			this.instanceToChannel.clear();
			this.channelAccess.clear();
			this.queuedSounds.clear();
			this.tickingSounds.clear();
			this.instanceBySource.clear();
			this.soundDeleteTime.clear();
			this.queuedTickableSounds.clear();
		}
	}

	public void addEventListener(SoundEventListener soundEventListener) {
		this.listeners.add(soundEventListener);
	}

	public void removeEventListener(SoundEventListener soundEventListener) {
		this.listeners.remove(soundEventListener);
	}

	private boolean shouldChangeDevice() {
		if (this.library.isCurrentDeviceDisconnected()) {
			LOGGER.info("Audio device was lost!");
			return true;
		} else {
			long l = Util.getMillis();
			boolean bl = l - this.lastDeviceCheckTime >= 1000L;
			if (bl) {
				this.lastDeviceCheckTime = l;
				if (this.devicePoolState.compareAndSet(SoundEngine.DeviceCheckState.NO_CHANGE, SoundEngine.DeviceCheckState.ONGOING)) {
					String string = this.options.soundDevice().get();
					Util.ioPool().execute(() -> {
						if ("".equals(string)) {
							if (this.library.hasDefaultDeviceChanged()) {
								LOGGER.info("System default audio device has changed!");
								this.devicePoolState.compareAndSet(SoundEngine.DeviceCheckState.ONGOING, SoundEngine.DeviceCheckState.CHANGE_DETECTED);
							}
						} else if (!this.library.getCurrentDeviceName().equals(string) && this.library.getAvailableSoundDevices().contains(string)) {
							LOGGER.info("Preferred audio device has become available!");
							this.devicePoolState.compareAndSet(SoundEngine.DeviceCheckState.ONGOING, SoundEngine.DeviceCheckState.CHANGE_DETECTED);
						}

						this.devicePoolState.compareAndSet(SoundEngine.DeviceCheckState.ONGOING, SoundEngine.DeviceCheckState.NO_CHANGE);
					});
				}
			}

			return this.devicePoolState.compareAndSet(SoundEngine.DeviceCheckState.CHANGE_DETECTED, SoundEngine.DeviceCheckState.NO_CHANGE);
		}
	}

	public void tick(boolean bl) {
		if (this.shouldChangeDevice()) {
			this.reload();
		}

		if (!bl) {
			this.tickNonPaused();
		}

		this.channelAccess.scheduleTick();
	}

	private void tickNonPaused() {
		this.tickCount++;
		this.queuedTickableSounds.stream().filter(SoundInstance::canPlaySound).forEach(this::play);
		this.queuedTickableSounds.clear();

		for (TickableSoundInstance tickableSoundInstance : this.tickingSounds) {
			if (!tickableSoundInstance.canPlaySound()) {
				this.stop(tickableSoundInstance);
			}

			tickableSoundInstance.tick();
			if (tickableSoundInstance.isStopped()) {
				this.stop(tickableSoundInstance);
			} else {
				float f = this.calculateVolume(tickableSoundInstance);
				float g = this.calculatePitch(tickableSoundInstance);
				Vec3 vec3 = new Vec3(tickableSoundInstance.getX(), tickableSoundInstance.getY(), tickableSoundInstance.getZ());
				ChannelAccess.ChannelHandle channelHandle = (ChannelAccess.ChannelHandle)this.instanceToChannel.get(tickableSoundInstance);
				if (channelHandle != null) {
					channelHandle.execute(channel -> {
						channel.setVolume(f);
						channel.setPitch(g);
						channel.setSelfPosition(vec3);
					});
				}
			}
		}

		Iterator<Entry<SoundInstance, ChannelAccess.ChannelHandle>> iterator = this.instanceToChannel.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<SoundInstance, ChannelAccess.ChannelHandle> entry = (Entry<SoundInstance, ChannelAccess.ChannelHandle>)iterator.next();
			ChannelAccess.ChannelHandle channelHandle2 = (ChannelAccess.ChannelHandle)entry.getValue();
			SoundInstance soundInstance = (SoundInstance)entry.getKey();
			float h = this.options.getSoundSourceVolume(soundInstance.getSource());
			if (h <= 0.0F) {
				channelHandle2.execute(Channel::stop);
				iterator.remove();
			} else if (channelHandle2.isStopped()) {
				int i = (Integer)this.soundDeleteTime.get(soundInstance);
				if (i <= this.tickCount) {
					if (shouldLoopManually(soundInstance)) {
						this.queuedSounds.put(soundInstance, this.tickCount + soundInstance.getDelay());
					}

					iterator.remove();
					LOGGER.debug(MARKER, "Removed channel {} because it's not playing anymore", channelHandle2);
					this.soundDeleteTime.remove(soundInstance);

					try {
						this.instanceBySource.remove(soundInstance.getSource(), soundInstance);
					} catch (RuntimeException var8) {
					}

					if (soundInstance instanceof TickableSoundInstance) {
						this.tickingSounds.remove(soundInstance);
					}
				}
			}
		}

		Iterator<Entry<SoundInstance, Integer>> iterator2 = this.queuedSounds.entrySet().iterator();

		while (iterator2.hasNext()) {
			Entry<SoundInstance, Integer> entry2 = (Entry<SoundInstance, Integer>)iterator2.next();
			if (this.tickCount >= (Integer)entry2.getValue()) {
				SoundInstance soundInstance = (SoundInstance)entry2.getKey();
				if (soundInstance instanceof TickableSoundInstance) {
					((TickableSoundInstance)soundInstance).tick();
				}

				this.play(soundInstance);
				iterator2.remove();
			}
		}
	}

	private static boolean requiresManualLooping(SoundInstance soundInstance) {
		return soundInstance.getDelay() > 0;
	}

	private static boolean shouldLoopManually(SoundInstance soundInstance) {
		return soundInstance.isLooping() && requiresManualLooping(soundInstance);
	}

	private static boolean shouldLoopAutomatically(SoundInstance soundInstance) {
		return soundInstance.isLooping() && !requiresManualLooping(soundInstance);
	}

	public boolean isActive(SoundInstance soundInstance) {
		if (!this.loaded) {
			return false;
		} else {
			return this.soundDeleteTime.containsKey(soundInstance) && this.soundDeleteTime.get(soundInstance) <= this.tickCount
				? true
				: this.instanceToChannel.containsKey(soundInstance);
		}
	}

	public void play(SoundInstance soundInstance) {
		if (this.loaded) {
			if (soundInstance.canPlaySound()) {
				WeighedSoundEvents weighedSoundEvents = soundInstance.resolve(this.soundManager);
				ResourceLocation resourceLocation = soundInstance.getLocation();
				if (weighedSoundEvents == null) {
					if (ONLY_WARN_ONCE.add(resourceLocation)) {
						LOGGER.warn(MARKER, "Unable to play unknown soundEvent: {}", resourceLocation);
					}
				} else {
					Sound sound = soundInstance.getSound();
					if (sound != SoundManager.INTENTIONALLY_EMPTY_SOUND) {
						if (sound == SoundManager.EMPTY_SOUND) {
							if (ONLY_WARN_ONCE.add(resourceLocation)) {
								LOGGER.warn(MARKER, "Unable to play empty soundEvent: {}", resourceLocation);
							}
						} else {
							float f = soundInstance.getVolume();
							float g = Math.max(f, 1.0F) * (float)sound.getAttenuationDistance();
							SoundSource soundSource = soundInstance.getSource();
							float h = this.calculateVolume(f, soundSource);
							float i = this.calculatePitch(soundInstance);
							SoundInstance.Attenuation attenuation = soundInstance.getAttenuation();
							boolean bl = soundInstance.isRelative();
							if (h == 0.0F && !soundInstance.canStartSilent()) {
								LOGGER.debug(MARKER, "Skipped playing sound {}, volume was zero.", sound.getLocation());
							} else {
								Vec3 vec3 = new Vec3(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());
								if (!this.listeners.isEmpty()) {
									boolean bl2 = bl || attenuation == SoundInstance.Attenuation.NONE || this.listener.getListenerPosition().distanceToSqr(vec3) < (double)(g * g);
									if (bl2) {
										for (SoundEventListener soundEventListener : this.listeners) {
											soundEventListener.onPlaySound(soundInstance, weighedSoundEvents);
										}
									} else {
										LOGGER.debug(MARKER, "Did not notify listeners of soundEvent: {}, it is too far away to hear", resourceLocation);
									}
								}

								if (this.listener.getGain() <= 0.0F) {
									LOGGER.debug(MARKER, "Skipped playing soundEvent: {}, master volume was zero", resourceLocation);
								} else {
									boolean bl2 = shouldLoopAutomatically(soundInstance);
									boolean bl3 = sound.shouldStream();
									CompletableFuture<ChannelAccess.ChannelHandle> completableFuture = this.channelAccess
										.createHandle(sound.shouldStream() ? Library.Pool.STREAMING : Library.Pool.STATIC);
									ChannelAccess.ChannelHandle channelHandle = (ChannelAccess.ChannelHandle)completableFuture.join();
									if (channelHandle == null) {
										if (SharedConstants.IS_RUNNING_IN_IDE) {
											LOGGER.warn("Failed to create new sound handle");
										}
									} else {
										LOGGER.debug(MARKER, "Playing sound {} for event {}", sound.getLocation(), resourceLocation);
										this.soundDeleteTime.put(soundInstance, this.tickCount + 20);
										this.instanceToChannel.put(soundInstance, channelHandle);
										this.instanceBySource.put(soundSource, soundInstance);
										channelHandle.execute(channel -> {
											channel.setPitch(i);
											channel.setVolume(h);
											if (attenuation == SoundInstance.Attenuation.LINEAR) {
												channel.linearAttenuation(g);
											} else {
												channel.disableAttenuation();
											}

											channel.setLooping(bl2 && !bl3);
											channel.setSelfPosition(vec3);
											channel.setRelative(bl);
										});
										if (!bl3) {
											this.soundBuffers.getCompleteBuffer(sound.getPath()).thenAccept(soundBuffer -> channelHandle.execute(channel -> {
													channel.attachStaticBuffer(soundBuffer);
													channel.play();
												}));
										} else {
											this.soundBuffers.getStream(sound.getPath(), bl2).thenAccept(audioStream -> channelHandle.execute(channel -> {
													channel.attachBufferStream(audioStream);
													channel.play();
												}));
										}

										if (soundInstance instanceof TickableSoundInstance) {
											this.tickingSounds.add((TickableSoundInstance)soundInstance);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public void queueTickingSound(TickableSoundInstance tickableSoundInstance) {
		this.queuedTickableSounds.add(tickableSoundInstance);
	}

	public void requestPreload(Sound sound) {
		this.preloadQueue.add(sound);
	}

	private float calculatePitch(SoundInstance soundInstance) {
		return Mth.clamp(soundInstance.getPitch(), 0.5F, 2.0F) * (Float)Rules.GLOBAL_PITCH.get();
	}

	private float calculateVolume(SoundInstance soundInstance) {
		return this.calculateVolume(soundInstance.getVolume(), soundInstance.getSource());
	}

	private float calculateVolume(float f, SoundSource soundSource) {
		return Mth.clamp(f * this.getVolume(soundSource), 0.0F, 1.0F);
	}

	public void pause() {
		if (this.loaded) {
			this.channelAccess.executeOnChannels(stream -> stream.forEach(Channel::pause));
		}
	}

	public void resume() {
		if (this.loaded) {
			this.channelAccess.executeOnChannels(stream -> stream.forEach(Channel::unpause));
		}
	}

	public void playDelayed(SoundInstance soundInstance, int i) {
		this.queuedSounds.put(soundInstance, this.tickCount + i);
	}

	public void updateSource(Camera camera) {
		if (this.loaded && camera.isInitialized()) {
			Vec3 vec3 = camera.getPosition();
			Vector3f vector3f = camera.getLookVector();
			Vector3f vector3f2 = camera.getUpVector();
			this.executor.execute(() -> {
				this.listener.setListenerPosition(vec3);
				this.listener.setListenerOrientation(vector3f, vector3f2);
			});
		}
	}

	public void stop(@Nullable ResourceLocation resourceLocation, @Nullable SoundSource soundSource) {
		if (soundSource != null) {
			for (SoundInstance soundInstance : this.instanceBySource.get(soundSource)) {
				if (resourceLocation == null || soundInstance.getLocation().equals(resourceLocation)) {
					this.stop(soundInstance);
				}
			}
		} else if (resourceLocation == null) {
			this.stopAll();
		} else {
			for (SoundInstance soundInstancex : this.instanceToChannel.keySet()) {
				if (soundInstancex.getLocation().equals(resourceLocation)) {
					this.stop(soundInstancex);
				}
			}
		}
	}

	public String getDebugString() {
		return this.library.getDebugString();
	}

	public List<String> getAvailableSoundDevices() {
		return this.library.getAvailableSoundDevices();
	}

	@Environment(EnvType.CLIENT)
	static enum DeviceCheckState {
		ONGOING,
		CHANGE_DETECTED,
		NO_CHANGE;
	}
}
