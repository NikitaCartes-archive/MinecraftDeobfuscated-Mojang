/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.sounds;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.Listener;
import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.logging.LogUtils;
import com.mojang.math.Vector3f;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngineExecutor;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Environment(value=EnvType.CLIENT)
public class SoundEngine {
    private static final Marker MARKER = MarkerFactory.getMarker("SOUNDS");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float PITCH_MIN = 0.5f;
    private static final float PITCH_MAX = 2.0f;
    private static final float VOLUME_MIN = 0.0f;
    private static final float VOLUME_MAX = 1.0f;
    private static final int MIN_SOURCE_LIFETIME = 20;
    private static final Set<ResourceLocation> ONLY_WARN_ONCE = Sets.newHashSet();
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
    private final AtomicReference<DeviceCheckState> devicePoolState = new AtomicReference<DeviceCheckState>(DeviceCheckState.NO_CHANGE);
    private final Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel = Maps.newHashMap();
    private final Multimap<SoundSource, SoundInstance> instanceBySource = HashMultimap.create();
    private final List<TickableSoundInstance> tickingSounds = Lists.newArrayList();
    private final Map<SoundInstance, Integer> queuedSounds = Maps.newHashMap();
    private final Map<SoundInstance, Integer> soundDeleteTime = Maps.newHashMap();
    private final List<SoundEventListener> listeners = Lists.newArrayList();
    private final List<TickableSoundInstance> queuedTickableSounds = Lists.newArrayList();
    private final List<Sound> preloadQueue = Lists.newArrayList();

    public SoundEngine(SoundManager soundManager, Options options, ResourceProvider resourceProvider) {
        this.soundManager = soundManager;
        this.options = options;
        this.soundBuffers = new SoundBufferLibrary(resourceProvider);
    }

    public void reload() {
        ONLY_WARN_ONCE.clear();
        for (SoundEvent soundEvent : Registry.SOUND_EVENT) {
            ResourceLocation resourceLocation = soundEvent.getLocation();
            if (this.soundManager.getSoundEvent(resourceLocation) != null) continue;
            LOGGER.warn("Missing sound for event: {}", (Object)Registry.SOUND_EVENT.getKey(soundEvent));
            ONLY_WARN_ONCE.add(resourceLocation);
        }
        this.destroy();
        this.loadLibrary();
    }

    private synchronized void loadLibrary() {
        if (this.loaded) {
            return;
        }
        try {
            String string = this.options.soundDevice().get();
            this.library.init("".equals(string) ? null : string, this.options.directionalAudio().get());
            this.listener.reset();
            this.listener.setGain(this.options.getSoundSourceVolume(SoundSource.MASTER));
            this.soundBuffers.preload(this.preloadQueue).thenRun(this.preloadQueue::clear);
            this.loaded = true;
            LOGGER.info(MARKER, "Sound engine started");
        } catch (RuntimeException runtimeException) {
            LOGGER.error(MARKER, "Error starting SoundSystem. Turning off sounds & music", runtimeException);
        }
    }

    private float getVolume(@Nullable SoundSource soundSource) {
        if (soundSource == null || soundSource == SoundSource.MASTER) {
            return 1.0f;
        }
        return this.options.getSoundSourceVolume(soundSource);
    }

    public void updateCategoryVolume(SoundSource soundSource, float f) {
        if (!this.loaded) {
            return;
        }
        if (soundSource == SoundSource.MASTER) {
            this.listener.setGain(f);
            return;
        }
        this.instanceToChannel.forEach((soundInstance, channelHandle) -> {
            float f = this.calculateVolume((SoundInstance)soundInstance);
            channelHandle.execute(channel -> {
                if (f <= 0.0f) {
                    channel.stop();
                } else {
                    channel.setVolume(f);
                }
            });
        });
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
        ChannelAccess.ChannelHandle channelHandle;
        if (this.loaded && (channelHandle = this.instanceToChannel.get(soundInstance)) != null) {
            channelHandle.execute(Channel::stop);
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
        boolean bl;
        if (this.library.isCurrentDeviceDisconnected()) {
            LOGGER.info("Audio device was lost!");
            return true;
        }
        long l = Util.getMillis();
        boolean bl2 = bl = l - this.lastDeviceCheckTime >= 1000L;
        if (bl) {
            this.lastDeviceCheckTime = l;
            if (this.devicePoolState.compareAndSet(DeviceCheckState.NO_CHANGE, DeviceCheckState.ONGOING)) {
                String string = this.options.soundDevice().get();
                Util.ioPool().execute(() -> {
                    if ("".equals(string)) {
                        if (this.library.hasDefaultDeviceChanged()) {
                            LOGGER.info("System default audio device has changed!");
                            this.devicePoolState.compareAndSet(DeviceCheckState.ONGOING, DeviceCheckState.CHANGE_DETECTED);
                        }
                    } else if (!this.library.getCurrentDeviceName().equals(string) && this.library.getAvailableSoundDevices().contains(string)) {
                        LOGGER.info("Preferred audio device has become available!");
                        this.devicePoolState.compareAndSet(DeviceCheckState.ONGOING, DeviceCheckState.CHANGE_DETECTED);
                    }
                    this.devicePoolState.compareAndSet(DeviceCheckState.ONGOING, DeviceCheckState.NO_CHANGE);
                });
            }
        }
        return this.devicePoolState.compareAndSet(DeviceCheckState.CHANGE_DETECTED, DeviceCheckState.NO_CHANGE);
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
        ++this.tickCount;
        this.queuedTickableSounds.stream().filter(SoundInstance::canPlaySound).forEach(this::play);
        this.queuedTickableSounds.clear();
        for (TickableSoundInstance tickableSoundInstance : this.tickingSounds) {
            if (!tickableSoundInstance.canPlaySound()) {
                this.stop(tickableSoundInstance);
            }
            tickableSoundInstance.tick();
            if (tickableSoundInstance.isStopped()) {
                this.stop(tickableSoundInstance);
                continue;
            }
            float f = this.calculateVolume(tickableSoundInstance);
            float g = this.calculatePitch(tickableSoundInstance);
            Vec3 vec3 = new Vec3(tickableSoundInstance.getX(), tickableSoundInstance.getY(), tickableSoundInstance.getZ());
            ChannelAccess.ChannelHandle channelHandle = this.instanceToChannel.get(tickableSoundInstance);
            if (channelHandle == null) continue;
            channelHandle.execute(channel -> {
                channel.setVolume(f);
                channel.setPitch(g);
                channel.setSelfPosition(vec3);
            });
        }
        Iterator<Map.Entry<SoundInstance, ChannelAccess.ChannelHandle>> iterator = this.instanceToChannel.entrySet().iterator();
        while (iterator.hasNext()) {
            int i;
            Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry = iterator.next();
            ChannelAccess.ChannelHandle channelHandle2 = entry.getValue();
            SoundInstance soundInstance = entry.getKey();
            float h = this.options.getSoundSourceVolume(soundInstance.getSource());
            if (h <= 0.0f) {
                channelHandle2.execute(Channel::stop);
                iterator.remove();
                continue;
            }
            if (!channelHandle2.isStopped() || (i = this.soundDeleteTime.get(soundInstance).intValue()) > this.tickCount) continue;
            if (SoundEngine.shouldLoopManually(soundInstance)) {
                this.queuedSounds.put(soundInstance, this.tickCount + soundInstance.getDelay());
            }
            iterator.remove();
            LOGGER.debug(MARKER, "Removed channel {} because it's not playing anymore", (Object)channelHandle2);
            this.soundDeleteTime.remove(soundInstance);
            try {
                this.instanceBySource.remove((Object)soundInstance.getSource(), soundInstance);
            } catch (RuntimeException runtimeException) {
                // empty catch block
            }
            if (!(soundInstance instanceof TickableSoundInstance)) continue;
            this.tickingSounds.remove(soundInstance);
        }
        Iterator<Map.Entry<SoundInstance, Integer>> iterator2 = this.queuedSounds.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry<SoundInstance, Integer> entry2 = iterator2.next();
            if (this.tickCount < entry2.getValue()) continue;
            SoundInstance soundInstance = entry2.getKey();
            if (soundInstance instanceof TickableSoundInstance) {
                ((TickableSoundInstance)soundInstance).tick();
            }
            this.play(soundInstance);
            iterator2.remove();
        }
    }

    private static boolean requiresManualLooping(SoundInstance soundInstance) {
        return soundInstance.getDelay() > 0;
    }

    private static boolean shouldLoopManually(SoundInstance soundInstance) {
        return soundInstance.isLooping() && SoundEngine.requiresManualLooping(soundInstance);
    }

    private static boolean shouldLoopAutomatically(SoundInstance soundInstance) {
        return soundInstance.isLooping() && !SoundEngine.requiresManualLooping(soundInstance);
    }

    public boolean isActive(SoundInstance soundInstance) {
        if (!this.loaded) {
            return false;
        }
        if (this.soundDeleteTime.containsKey(soundInstance) && this.soundDeleteTime.get(soundInstance) <= this.tickCount) {
            return true;
        }
        return this.instanceToChannel.containsKey(soundInstance);
    }

    public void play(SoundInstance soundInstance) {
        boolean bl2;
        if (!this.loaded) {
            return;
        }
        if (!soundInstance.canPlaySound()) {
            return;
        }
        WeighedSoundEvents weighedSoundEvents = soundInstance.resolve(this.soundManager);
        ResourceLocation resourceLocation = soundInstance.getLocation();
        if (weighedSoundEvents == null) {
            if (ONLY_WARN_ONCE.add(resourceLocation)) {
                LOGGER.warn(MARKER, "Unable to play unknown soundEvent: {}", (Object)resourceLocation);
            }
            return;
        }
        Sound sound = soundInstance.getSound();
        if (sound == SoundManager.EMPTY_SOUND) {
            if (ONLY_WARN_ONCE.add(resourceLocation)) {
                LOGGER.warn(MARKER, "Unable to play empty soundEvent: {}", (Object)resourceLocation);
            }
            return;
        }
        float f = soundInstance.getVolume();
        float g = Math.max(f, 1.0f) * (float)sound.getAttenuationDistance();
        SoundSource soundSource = soundInstance.getSource();
        float h = this.calculateVolume(f, soundSource);
        float i = this.calculatePitch(soundInstance);
        SoundInstance.Attenuation attenuation = soundInstance.getAttenuation();
        boolean bl = soundInstance.isRelative();
        if (h == 0.0f && !soundInstance.canStartSilent()) {
            LOGGER.debug(MARKER, "Skipped playing sound {}, volume was zero.", (Object)sound.getLocation());
            return;
        }
        Vec3 vec3 = new Vec3(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());
        if (!this.listeners.isEmpty()) {
            boolean bl3 = bl2 = bl || attenuation == SoundInstance.Attenuation.NONE || this.listener.getListenerPosition().distanceToSqr(vec3) < (double)(g * g);
            if (bl2) {
                for (SoundEventListener soundEventListener : this.listeners) {
                    soundEventListener.onPlaySound(soundInstance, weighedSoundEvents);
                }
            } else {
                LOGGER.debug(MARKER, "Did not notify listeners of soundEvent: {}, it is too far away to hear", (Object)resourceLocation);
            }
        }
        if (this.listener.getGain() <= 0.0f) {
            LOGGER.debug(MARKER, "Skipped playing soundEvent: {}, master volume was zero", (Object)resourceLocation);
            return;
        }
        bl2 = SoundEngine.shouldLoopAutomatically(soundInstance);
        boolean bl3 = sound.shouldStream();
        CompletableFuture<ChannelAccess.ChannelHandle> completableFuture = this.channelAccess.createHandle(sound.shouldStream() ? Library.Pool.STREAMING : Library.Pool.STATIC);
        ChannelAccess.ChannelHandle channelHandle = completableFuture.join();
        if (channelHandle == null) {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                LOGGER.warn("Failed to create new sound handle");
            }
            return;
        }
        LOGGER.debug(MARKER, "Playing sound {} for event {}", (Object)sound.getLocation(), (Object)resourceLocation);
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
                channel.attachStaticBuffer((SoundBuffer)soundBuffer);
                channel.play();
            }));
        } else {
            this.soundBuffers.getStream(sound.getPath(), bl2).thenAccept(audioStream -> channelHandle.execute(channel -> {
                channel.attachBufferStream((AudioStream)audioStream);
                channel.play();
            }));
        }
        if (soundInstance instanceof TickableSoundInstance) {
            this.tickingSounds.add((TickableSoundInstance)soundInstance);
        }
    }

    public void queueTickingSound(TickableSoundInstance tickableSoundInstance) {
        this.queuedTickableSounds.add(tickableSoundInstance);
    }

    public void requestPreload(Sound sound) {
        this.preloadQueue.add(sound);
    }

    private float calculatePitch(SoundInstance soundInstance) {
        return Mth.clamp(soundInstance.getPitch(), 0.5f, 2.0f);
    }

    private float calculateVolume(SoundInstance soundInstance) {
        return this.calculateVolume(soundInstance.getVolume(), soundInstance.getSource());
    }

    private float calculateVolume(float f, SoundSource soundSource) {
        return Mth.clamp(f * this.getVolume(soundSource), 0.0f, 1.0f);
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
        if (!this.loaded || !camera.isInitialized()) {
            return;
        }
        Vec3 vec3 = camera.getPosition();
        Vector3f vector3f = camera.getLookVector();
        Vector3f vector3f2 = camera.getUpVector();
        this.executor.execute(() -> {
            this.listener.setListenerPosition(vec3);
            this.listener.setListenerOrientation(vector3f, vector3f2);
        });
    }

    public void stop(@Nullable ResourceLocation resourceLocation, @Nullable SoundSource soundSource) {
        if (soundSource != null) {
            for (SoundInstance soundInstance : this.instanceBySource.get(soundSource)) {
                if (resourceLocation != null && !soundInstance.getLocation().equals(resourceLocation)) continue;
                this.stop(soundInstance);
            }
        } else if (resourceLocation == null) {
            this.stopAll();
        } else {
            for (SoundInstance soundInstance : this.instanceToChannel.keySet()) {
                if (!soundInstance.getLocation().equals(resourceLocation)) continue;
                this.stop(soundInstance);
            }
        }
    }

    public String getDebugString() {
        return this.library.getDebugString();
    }

    public List<String> getAvailableSoundDevices() {
        return this.library.getAvailableSoundDevices();
    }

    @Environment(value=EnvType.CLIENT)
    static enum DeviceCheckState {
        ONGOING,
        CHANGE_DETECTED,
        NO_CHANGE;

    }
}

