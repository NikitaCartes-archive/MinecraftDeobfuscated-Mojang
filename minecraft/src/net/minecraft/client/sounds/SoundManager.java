package net.minecraft.client.sounds;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundEventRegistration;
import net.minecraft.client.resources.sounds.SoundEventRegistrationSerializer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.MultipliedFloats;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SoundManager extends SimplePreparableReloadListener<SoundManager.Preparations> {
	public static final Sound EMPTY_SOUND = new Sound("minecraft:empty", ConstantFloat.of(1.0F), ConstantFloat.of(1.0F), 1, Sound.Type.FILE, false, false, 16);
	public static final ResourceLocation INTENTIONALLY_EMPTY_SOUND_LOCATION = new ResourceLocation("minecraft", "intentionally_empty");
	public static final WeighedSoundEvents INTENTIONALLY_EMPTY_SOUND_EVENT = new WeighedSoundEvents(INTENTIONALLY_EMPTY_SOUND_LOCATION, null);
	public static final Sound INTENTIONALLY_EMPTY_SOUND = new Sound(
		INTENTIONALLY_EMPTY_SOUND_LOCATION.toString(), ConstantFloat.of(1.0F), ConstantFloat.of(1.0F), 1, Sound.Type.FILE, false, false, 16
	);
	static final Logger LOGGER = LogUtils.getLogger();
	private static final String SOUNDS_PATH = "sounds.json";
	private static final Gson GSON = new GsonBuilder()
		.registerTypeHierarchyAdapter(Component.class, new Component.SerializerAdapter())
		.registerTypeAdapter(SoundEventRegistration.class, new SoundEventRegistrationSerializer())
		.create();
	private static final TypeToken<Map<String, SoundEventRegistration>> SOUND_EVENT_REGISTRATION_TYPE = new TypeToken<Map<String, SoundEventRegistration>>() {
	};
	private final Map<ResourceLocation, WeighedSoundEvents> registry = Maps.<ResourceLocation, WeighedSoundEvents>newHashMap();
	private final SoundEngine soundEngine;
	private final Map<ResourceLocation, Resource> soundCache = new HashMap();

	public SoundManager(Options options) {
		this.soundEngine = new SoundEngine(this, options, ResourceProvider.fromMap(this.soundCache));
	}

	protected SoundManager.Preparations prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		SoundManager.Preparations preparations = new SoundManager.Preparations();
		profilerFiller.startTick();
		profilerFiller.push("list");
		preparations.listResources(resourceManager);
		profilerFiller.pop();

		for (String string : resourceManager.getNamespaces()) {
			profilerFiller.push(string);

			try {
				for (Resource resource : resourceManager.getResourceStack(new ResourceLocation(string, "sounds.json"))) {
					profilerFiller.push(resource.sourcePackId());

					try {
						Reader reader = resource.openAsReader();

						try {
							profilerFiller.push("parse");
							Map<String, SoundEventRegistration> map = GsonHelper.fromJson(GSON, reader, SOUND_EVENT_REGISTRATION_TYPE);
							profilerFiller.popPush("register");

							for (Entry<String, SoundEventRegistration> entry : map.entrySet()) {
								preparations.handleRegistration(new ResourceLocation(string, (String)entry.getKey()), (SoundEventRegistration)entry.getValue());
							}

							profilerFiller.pop();
						} catch (Throwable var14) {
							if (reader != null) {
								try {
									reader.close();
								} catch (Throwable var13) {
									var14.addSuppressed(var13);
								}
							}

							throw var14;
						}

						if (reader != null) {
							reader.close();
						}
					} catch (RuntimeException var15) {
						LOGGER.warn("Invalid {} in resourcepack: '{}'", "sounds.json", resource.sourcePackId(), var15);
					}

					profilerFiller.pop();
				}
			} catch (IOException var16) {
			}

			profilerFiller.pop();
		}

		profilerFiller.endTick();
		return preparations;
	}

	protected void apply(SoundManager.Preparations preparations, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		preparations.apply(this.registry, this.soundCache, this.soundEngine);
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			for (ResourceLocation resourceLocation : this.registry.keySet()) {
				WeighedSoundEvents weighedSoundEvents = (WeighedSoundEvents)this.registry.get(resourceLocation);
				if (!ComponentUtils.isTranslationResolvable(weighedSoundEvents.getSubtitle()) && BuiltInRegistries.SOUND_EVENT.containsKey(resourceLocation)) {
					LOGGER.error("Missing subtitle {} for sound event: {}", weighedSoundEvents.getSubtitle(), resourceLocation);
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			for (ResourceLocation resourceLocationx : this.registry.keySet()) {
				if (!BuiltInRegistries.SOUND_EVENT.containsKey(resourceLocationx)) {
					LOGGER.debug("Not having sound event for: {}", resourceLocationx);
				}
			}
		}

		this.soundEngine.reload();
	}

	public List<String> getAvailableSoundDevices() {
		return this.soundEngine.getAvailableSoundDevices();
	}

	static boolean validateSoundResource(Sound sound, ResourceLocation resourceLocation, ResourceProvider resourceProvider) {
		ResourceLocation resourceLocation2 = sound.getPath();
		if (resourceProvider.getResource(resourceLocation2).isEmpty()) {
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
		private Map<ResourceLocation, Resource> soundCache = Map.of();

		void listResources(ResourceManager resourceManager) {
			this.soundCache = Sound.SOUND_LISTER.listMatchingResources(resourceManager);
		}

		void handleRegistration(ResourceLocation resourceLocation, SoundEventRegistration soundEventRegistration) {
			WeighedSoundEvents weighedSoundEvents = (WeighedSoundEvents)this.registry.get(resourceLocation);
			boolean bl = weighedSoundEvents == null;
			if (bl || soundEventRegistration.isReplace()) {
				if (!bl) {
					SoundManager.LOGGER.debug("Replaced sound event location {}", resourceLocation);
				}

				weighedSoundEvents = new WeighedSoundEvents(resourceLocation, soundEventRegistration.getSubtitle());
				this.registry.put(resourceLocation, weighedSoundEvents);
			}

			ResourceProvider resourceProvider = ResourceProvider.fromMap(this.soundCache);

			for (final Sound sound : soundEventRegistration.getSounds()) {
				final ResourceLocation resourceLocation2 = sound.getLocation();
				Weighted<Sound> weighted;
				switch (sound.getType()) {
					case FILE:
						if (!SoundManager.validateSoundResource(sound, resourceLocation, resourceProvider)) {
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

							public Sound getSound(RandomSource randomSource) {
								WeighedSoundEvents weighedSoundEvents = (WeighedSoundEvents)Preparations.this.registry.get(resourceLocation2);
								if (weighedSoundEvents == null) {
									return SoundManager.EMPTY_SOUND;
								} else {
									Sound sound = weighedSoundEvents.getSound(randomSource);
									return new Sound(
										sound.getLocation().toString(),
										new MultipliedFloats(sound.getVolume(), sound.getVolume()),
										new MultipliedFloats(sound.getPitch(), sound.getPitch()),
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

		public void apply(Map<ResourceLocation, WeighedSoundEvents> map, Map<ResourceLocation, Resource> map2, SoundEngine soundEngine) {
			map.clear();
			map2.clear();
			map2.putAll(this.soundCache);

			for (Entry<ResourceLocation, WeighedSoundEvents> entry : this.registry.entrySet()) {
				map.put((ResourceLocation)entry.getKey(), (WeighedSoundEvents)entry.getValue());
				((WeighedSoundEvents)entry.getValue()).preloadIfRequired(soundEngine);
			}
		}
	}
}
