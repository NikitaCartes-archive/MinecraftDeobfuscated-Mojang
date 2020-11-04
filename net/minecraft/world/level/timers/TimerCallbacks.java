/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.timers;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.timers.FunctionCallback;
import net.minecraft.world.level.timers.FunctionTagCallback;
import net.minecraft.world.level.timers.TimerCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class TimerCallbacks<C> {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final TimerCallbacks<MinecraftServer> SERVER_CALLBACKS = new TimerCallbacks<MinecraftServer>().register(new FunctionCallback.Serializer()).register(new FunctionTagCallback.Serializer());
    private final Map<ResourceLocation, TimerCallback.Serializer<C, ?>> idToSerializer = Maps.newHashMap();
    private final Map<Class<?>, TimerCallback.Serializer<C, ?>> classToSerializer = Maps.newHashMap();

    @VisibleForTesting
    public TimerCallbacks() {
    }

    public TimerCallbacks<C> register(TimerCallback.Serializer<C, ?> serializer) {
        this.idToSerializer.put(serializer.getId(), serializer);
        this.classToSerializer.put(serializer.getCls(), serializer);
        return this;
    }

    private <T extends TimerCallback<C>> TimerCallback.Serializer<C, T> getSerializer(Class<?> class_) {
        return this.classToSerializer.get(class_);
    }

    public <T extends TimerCallback<C>> CompoundTag serialize(T timerCallback) {
        TimerCallback.Serializer<T, T> serializer = this.getSerializer(timerCallback.getClass());
        CompoundTag compoundTag = new CompoundTag();
        serializer.serialize(compoundTag, timerCallback);
        compoundTag.putString("Type", serializer.getId().toString());
        return compoundTag;
    }

    @Nullable
    public TimerCallback<C> deserialize(CompoundTag compoundTag) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(compoundTag.getString("Type"));
        TimerCallback.Serializer<C, ?> serializer = this.idToSerializer.get(resourceLocation);
        if (serializer == null) {
            LOGGER.error("Failed to deserialize timer callback: {}", (Object)compoundTag);
            return null;
        }
        try {
            return serializer.deserialize(compoundTag);
        } catch (Exception exception) {
            LOGGER.error("Failed to deserialize timer callback: {}", (Object)compoundTag, (Object)exception);
            return null;
        }
    }
}

