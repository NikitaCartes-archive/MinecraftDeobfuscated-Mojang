package net.minecraft.client.sounds;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.Options;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundEventRegistration;
import net.minecraft.client.resources.sounds.SoundEventRegistrationSerializer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceThunk;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SoundManager extends SimplePreparableReloadListener<SoundManager.Preparations> {
	public static final Sound EMPTY_SOUND = new Sound("meta:missing_sound", 1.0F, 1.0F, 1, Sound.Type.FILE, false, false, 16);
	static final Logger LOGGER = LogUtils.getLogger();
	private static final String SOUNDS_PATH = "sounds.json";
	private static final Gson GSON = new GsonBuilder()
		.registerTypeHierarchyAdapter(Component.class, new Component.Serializer())
		.registerTypeAdapter(SoundEventRegistration.class, new SoundEventRegistrationSerializer())
		.create();
	private static final TypeToken<Map<String, SoundEventRegistration>> SOUND_EVENT_REGISTRATION_TYPE = new TypeToken<Map<String, SoundEventRegistration>>() {
	};
	private final Map<ResourceLocation, WeighedSoundEvents> registry = Maps.<ResourceLocation, WeighedSoundEvents>newHashMap();
	private final SoundEngine soundEngine;

	public SoundManager(ResourceManager resourceManager, Options options) {
		this.soundEngine = new SoundEngine(this, options, resourceManager);
	}

	protected SoundManager.Preparations prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		SoundManager.Preparations preparations = new SoundManager.Preparations();
		profilerFiller.startTick();

		for (String string : resourceManager.getNamespaces()) {
			profilerFiller.push(string);

			try {
				for (ResourceThunk resourceThunk : resourceManager.getResourceStack(new ResourceLocation(string, "sounds.json"))) {
					profilerFiller.push(resourceThunk.sourcePackId());

					try {
						Resource resource = resourceThunk.open();

						try {
							InputStream inputStream = resource.getInputStream();

							try {
								Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

								try {
									profilerFiller.push("parse");
									Map<String, SoundEventRegistration> map = GsonHelper.fromJson(GSON, reader, SOUND_EVENT_REGISTRATION_TYPE);
									profilerFiller.popPush("register");

									for (Entry<String, SoundEventRegistration> entry : map.entrySet()) {
										preparations.handleRegistration(new ResourceLocation(string, (String)entry.getKey()), (SoundEventRegistration)entry.getValue(), resourceManager);
									}

									profilerFiller.pop();
								} catch (Throwable var18) {
									try {
										reader.close();
									} catch (Throwable var17) {
										var18.addSuppressed(var17);
									}

									throw var18;
								}

								reader.close();
							} catch (Throwable var19) {
								if (inputStream != null) {
									try {
										inputStream.close();
									} catch (Throwable var16) {
										var19.addSuppressed(var16);
									}
								}

								throw var19;
							}

							if (inputStream != null) {
								inputStream.close();
							}
						} catch (Throwable var20) {
							if (resource != null) {
								try {
									resource.close();
								} catch (Throwable var15) {
									var20.addSuppressed(var15);
								}
							}

							throw var20;
						}

						if (resource != null) {
							resource.close();
						}
					} catch (RuntimeException var21) {
						LOGGER.warn("Invalid {} in resourcepack: '{}'", "sounds.json", resourceThunk.sourcePackId(), var21);
					}

					profilerFiller.pop();
				}
			} catch (IOException var22) {
			}

			profilerFiller.pop();
		}

		profilerFiller.endTick();
		return preparations;
	}

	protected void apply(SoundManager.Preparations preparations, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		preparations.apply(this.registry, this.soundEngine);
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			for (ResourceLocation resourceLocation : this.registry.keySet()) {
				WeighedSoundEvents weighedSoundEvents = (WeighedSoundEvents)this.registry.get(resourceLocation);
				if (weighedSoundEvents.getSubtitle() instanceof TranslatableComponent) {
					String string = ((TranslatableComponent)weighedSoundEvents.getSubtitle()).getKey();
					if (!I18n.exists(string) && Registry.SOUND_EVENT.containsKey(resourceLocation)) {
						LOGGER.error("Missing subtitle {} for sound event: {}", string, resourceLocation);
					}
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			for (ResourceLocation resourceLocationx : this.registry.keySet()) {
				if (!Registry.SOUND_EVENT.containsKey(resourceLocationx)) {
					LOGGER.debug("Not having sound event for: {}", resourceLocationx);
				}
			}
		}

		this.soundEngine.reload();
	}

	public List<String> getAvailableSoundDevices() {
		return this.soundEngine.getAvailableSoundDevices();
	}

	static boolean validateSoundResource(Sound sound, ResourceLocation resourceLocation, ResourceManager resourceManager) {
		ResourceLocation resourceLocation2 = sound.getPath();
		if (!resourceManager.hasResource(resourceLocation2)) {
			LOGGER.warn("File {} does not exist, cannot add it to event {}", resourceLocation2, resourceLocation);
			return false;
		} else {
			return true;
		}
	}

	@Nullable
	public WeighedSoundEvents getSoundEvent(ResourceLocation resourceLocation) {
		return (WeighedSoundEvents)this.registry.get(resourceLocation);
	}

	public Collection<ResourceLocation> getAvailableSounds() {
		return this.registry.keySet();
	}

	public void queueTickingSound(TickableSoundInstance tickableSoundInstance) {
		this.soundEngine.queueTickingSound(tickableSoundInstance);
	}

	public void play(SoundInstance soundInstance) {
		this.soundEngine.play(soundInstance);
	}

	public void playDelayed(SoundInstance soundInstance, int i) {
		this.soundEngine.playDelayed(soundInstance, i);
	}

	public void updateSource(Camera camera) {
		this.soundEngine.updateSource(camera);
	}

	public void pause() {
		this.soundEngine.pause();
	}

	public void stop() {
		this.soundEngine.stopAll();
	}

	public void destroy() {
		this.soundEngine.destroy();
	}

	public void tick(boolean bl) {
		this.soundEngine.tick(bl);
	}

	public void resume() {
		this.soundEngine.resume();
	}

	public void updateSourceVolume(SoundSource soundSource, float f) {
		if (soundSource == SoundSource.MASTER && f <= 0.0F) {
			this.stop();
		}

		this.soundEngine.updateCategoryVolume(soundSource, f);
	}

	public void stop(SoundInstance soundInstance) {
		this.soundEngine.stop(soundInstance);
	}

	public boolean isActive(SoundInstance soundInstance) {
		return this.soundEngine.isActive(soundInstance);
	}

	public void addListener(SoundEventListener soundEventListener) {
		this.soundEngine.addEventListener(soundEventListener);
	}

	public void removeListener(SoundEventListener soundEventListener) {
		this.soundEngine.removeEventListener(soundEventListener);
	}

	public void stop(@Nullable ResourceLocation resourceLocation, @Nullable SoundSource soundSource) {
		this.soundEngine.stop(resourceLocation, soundSource);
	}

	public String getDebugString() {
		return this.soundEngine.getDebugString();
	}

	public void reload() {
		this.soundEngine.reload();
	}

	@Environment(EnvType.CLIENT)
	protected static class Preparations {
		final Map<ResourceLocation, WeighedSoundEvents> registry = Maps.<ResourceLocation, WeighedSoundEvents>newHashMap();

		void handleRegistration(ResourceLocation resourceLocation, SoundEventRegistration soundEventRegistration, ResourceManager resourceManager) {
			WeighedSoundEvents weighedSoundEvents = (WeighedSoundEvents)this.registry.get(resourceLocation);
			boolean bl = weighedSoundEvents == null;
			if (bl || soundEventRegistration.isReplace()) {
				if (!bl) {
					SoundManager.LOGGER.debug("Replaced sound event location {}", resourceLocation);
				}

				weighedSoundEvents = new WeighedSoundEvents(resourceLocation, soundEventRegistration.getSubtitle());
				this.registry.put(resourceLocation, weighedSoundEvents);
			}

			for (final Sound sound : soundEventRegistration.getSounds()) {
				final ResourceLocation resourceLocation2 = sound.getLocation();
				Weighted<Sound> weighted;
				switch (sound.getType()) {
					case FILE:
						if (!SoundManager.validateSoundResource(sound, resourceLocation, resourceManager)) {
							continue;
						}

						weighted = sound;
						break;
					case SOUND_EVENT:
						weighted = new Weighted<Sound>() {
							@Override
							public int getWeight() {
								WeighedSoundEvents weighedSoundEvents = (WeighedSoundEvents)Preparations.this.registry.get(resourceLocation2);
								return weighedSoundEvents == null ? 0 : weighedSoundEvents.getWeight();
							}

							public Sound getSound() {
								WeighedSoundEvents weighedSoundEvents = (WeighedSoundEvents)Preparations.this.registry.get(resourceLocation2);
								if (weighedSoundEvents == null) {
									return SoundManager.EMPTY_SOUND;
								} else {
									Sound sound = weighedSoundEvents.getSound();
									return new Sound(
										sound.getLocation().toString(),
										sound.getVolume() * sound.getVolume(),
										sound.getPitch() * sound.getPitch(),
										sound.getWeight(),
										Sound.Type.FILE,
										sound.shouldStream() || sound.shouldStream(),
										sound.shouldPreload(),
										sound.getAttenuationDistance()
									);
								}
							}

							@Override
							public void preloadIfRequired(SoundEngine soundEngine) {
								WeighedSoundEvents weighedSoundEvents = (WeighedSoundEvents)Preparations.this.registry.get(resourceLocation2);
								if (weighedSoundEvents != null) {
									weighedSoundEvents.preloadIfRequired(soundEngine);
								}
							}
						};
						break;
					default:
						throw new IllegalStateException("Unknown SoundEventRegistration type: " + sound.getType());
				}

				weighedSoundEvents.addSound(weighted);
			}
		}

		public void apply(Map<ResourceLocation, WeighedSoundEvents> map, SoundEngine soundEngine) {
			map.clear();

			for (Entry<ResourceLocation, WeighedSoundEvents> entry : this.registry.entrySet()) {
				map.put((ResourceLocation)entry.getKey(), (WeighedSoundEvents)entry.getValue());
				((WeighedSoundEvents)entry.getValue()).preloadIfRequired(soundEngine);
			}
		}
	}
}
