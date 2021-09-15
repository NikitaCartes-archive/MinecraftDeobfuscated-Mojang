/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.sounds;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SoundManager
extends SimplePreparableReloadListener<Preparations> {
    public static final Sound EMPTY_SOUND = new Sound("meta:missing_sound", 1.0f, 1.0f, 1, Sound.Type.FILE, false, false, 16);
    static final Logger LOGGER = LogManager.getLogger();
    private static final String SOUNDS_PATH = "sounds.json";
    private static final Gson GSON = new GsonBuilder().registerTypeHierarchyAdapter(Component.class, new Component.Serializer()).registerTypeAdapter((Type)((Object)SoundEventRegistration.class), new SoundEventRegistrationSerializer()).create();
    private static final TypeToken<Map<String, SoundEventRegistration>> SOUND_EVENT_REGISTRATION_TYPE = new TypeToken<Map<String, SoundEventRegistration>>(){};
    private final Map<ResourceLocation, WeighedSoundEvents> registry = Maps.newHashMap();
    private final SoundEngine soundEngine;

    public SoundManager(ResourceManager resourceManager, Options options) {
        this.soundEngine = new SoundEngine(this, options, resourceManager);
    }

    @Override
    protected Preparations prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Preparations preparations = new Preparations();
        profilerFiller.startTick();
        for (String string : resourceManager.getNamespaces()) {
            profilerFiller.push(string);
            try {
                List<Resource> list = resourceManager.getResources(new ResourceLocation(string, SOUNDS_PATH));
                for (Resource resource : list) {
                    profilerFiller.push(resource.getSourceName());
                    try (InputStream inputStream = resource.getInputStream();
                         InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);){
                        profilerFiller.push("parse");
                        Map<String, SoundEventRegistration> map = GsonHelper.fromJson(GSON, (Reader)reader, SOUND_EVENT_REGISTRATION_TYPE);
                        profilerFiller.popPush("register");
                        for (Map.Entry<String, SoundEventRegistration> entry : map.entrySet()) {
                            preparations.handleRegistration(new ResourceLocation(string, entry.getKey()), entry.getValue(), resourceManager);
                        }
                        profilerFiller.pop();
                    } catch (RuntimeException runtimeException) {
                        LOGGER.warn("Invalid {} in resourcepack: '{}'", (Object)SOUNDS_PATH, (Object)resource.getSourceName(), (Object)runtimeException);
                    }
                    profilerFiller.pop();
                }
            } catch (IOException iOException) {
                // empty catch block
            }
            profilerFiller.pop();
        }
        profilerFiller.endTick();
        return preparations;
    }

    @Override
    protected void apply(Preparations preparations, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        preparations.apply(this.registry, this.soundEngine);
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            for (ResourceLocation resourceLocation : this.registry.keySet()) {
                String string;
                WeighedSoundEvents weighedSoundEvents = this.registry.get(resourceLocation);
                if (!(weighedSoundEvents.getSubtitle() instanceof TranslatableComponent) || I18n.exists(string = ((TranslatableComponent)weighedSoundEvents.getSubtitle()).getKey()) || !Registry.SOUND_EVENT.containsKey(resourceLocation)) continue;
                LOGGER.error("Missing subtitle {} for sound event: {}", (Object)string, (Object)resourceLocation);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            for (ResourceLocation resourceLocation : this.registry.keySet()) {
                if (Registry.SOUND_EVENT.containsKey(resourceLocation)) continue;
                LOGGER.debug("Not having sound event for: {}", (Object)resourceLocation);
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
            LOGGER.warn("File {} does not exist, cannot add it to event {}", (Object)resourceLocation2, (Object)resourceLocation);
            return false;
        }
        return true;
    }

    @Nullable
    public WeighedSoundEvents getSoundEvent(ResourceLocation resourceLocation) {
        return this.registry.get(resourceLocation);
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
        if (soundSource == SoundSource.MASTER && f <= 0.0f) {
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

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Preparations {
        final Map<ResourceLocation, WeighedSoundEvents> registry = Maps.newHashMap();

        protected Preparations() {
        }

        void handleRegistration(ResourceLocation resourceLocation, SoundEventRegistration soundEventRegistration, ResourceManager resourceManager) {
            boolean bl;
            WeighedSoundEvents weighedSoundEvents = this.registry.get(resourceLocation);
            boolean bl2 = bl = weighedSoundEvents == null;
            if (bl || soundEventRegistration.isReplace()) {
                if (!bl) {
                    LOGGER.debug("Replaced sound event location {}", (Object)resourceLocation);
                }
                weighedSoundEvents = new WeighedSoundEvents(resourceLocation, soundEventRegistration.getSubtitle());
                this.registry.put(resourceLocation, weighedSoundEvents);
            }
            block4: for (final Sound sound : soundEventRegistration.getSounds()) {
                final ResourceLocation resourceLocation2 = sound.getLocation();
                weighedSoundEvents.addSound(switch (sound.getType()) {
                    case Sound.Type.FILE -> {
                        if (!SoundManager.validateSoundResource(sound, resourceLocation, resourceManager)) continue block4;
                        yield sound;
                    }
                    case Sound.Type.SOUND_EVENT -> new Weighted<Sound>(){

                        @Override
                        public int getWeight() {
                            WeighedSoundEvents weighedSoundEvents = registry.get(resourceLocation2);
                            return weighedSoundEvents == null ? 0 : weighedSoundEvents.getWeight();
                        }

                        @Override
                        public Sound getSound() {
                            WeighedSoundEvents weighedSoundEvents = registry.get(resourceLocation2);
                            if (weighedSoundEvents == null) {
                                return EMPTY_SOUND;
                            }
                            Sound sound2 = weighedSoundEvents.getSound();
                            return new Sound(sound2.getLocation().toString(), sound2.getVolume() * sound.getVolume(), sound2.getPitch() * sound.getPitch(), sound.getWeight(), Sound.Type.FILE, sound2.shouldStream() || sound.shouldStream(), sound2.shouldPreload(), sound2.getAttenuationDistance());
                        }

                        @Override
                        public void preloadIfRequired(SoundEngine soundEngine) {
                            WeighedSoundEvents weighedSoundEvents = registry.get(resourceLocation2);
                            if (weighedSoundEvents == null) {
                                return;
                            }
                            weighedSoundEvents.preloadIfRequired(soundEngine);
                        }

                        @Override
                        public /* synthetic */ Object getSound() {
                            return this.getSound();
                        }
                    };
                    default -> throw new IllegalStateException("Unknown SoundEventRegistration type: " + sound.getType());
                });
            }
        }

        public void apply(Map<ResourceLocation, WeighedSoundEvents> map, SoundEngine soundEngine) {
            map.clear();
            for (Map.Entry<ResourceLocation, WeighedSoundEvents> entry : this.registry.entrySet()) {
                map.put(entry.getKey(), entry.getValue());
                entry.getValue().preloadIfRequired(soundEngine);
            }
        }
    }
}

