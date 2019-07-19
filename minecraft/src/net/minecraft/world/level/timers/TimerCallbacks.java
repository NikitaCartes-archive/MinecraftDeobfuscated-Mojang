package net.minecraft.world.level.timers;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimerCallbacks<C> {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final TimerCallbacks<MinecraftServer> SERVER_CALLBACKS = new TimerCallbacks<MinecraftServer>()
		.register(new FunctionCallback.Serializer())
		.register(new FunctionTagCallback.Serializer());
	private final Map<ResourceLocation, TimerCallback.Serializer<C, ?>> idToSerializer = Maps.<ResourceLocation, TimerCallback.Serializer<C, ?>>newHashMap();
	private final Map<Class<?>, TimerCallback.Serializer<C, ?>> classToSerializer = Maps.<Class<?>, TimerCallback.Serializer<C, ?>>newHashMap();

	public TimerCallbacks<C> register(TimerCallback.Serializer<C, ?> serializer) {
		this.idToSerializer.put(serializer.getId(), serializer);
		this.classToSerializer.put(serializer.getCls(), serializer);
		return this;
	}

	private <T extends TimerCallback<C>> TimerCallback.Serializer<C, T> getSerializer(Class<?> class_) {
		return (TimerCallback.Serializer<C, T>)this.classToSerializer.get(class_);
	}

	public <T extends TimerCallback<C>> CompoundTag serialize(T timerCallback) {
		TimerCallback.Serializer<C, T> serializer = this.getSerializer(timerCallback.getClass());
		CompoundTag compoundTag = new CompoundTag();
		serializer.serialize(compoundTag, timerCallback);
		compoundTag.putString("Type", serializer.getId().toString());
		return compoundTag;
	}

	@Nullable
	public TimerCallback<C> deserialize(CompoundTag compoundTag) {
		ResourceLocation resourceLocation = ResourceLocation.tryParse(compoundTag.getString("Type"));
		TimerCallback.Serializer<C, ?> serializer = (TimerCallback.Serializer<C, ?>)this.idToSerializer.get(resourceLocation);
		if (serializer == null) {
			LOGGER.error("Failed to deserialize timer callback: " + compoundTag);
			return null;
		} else {
			try {
				return serializer.deserialize(compoundTag);
			} catch (Exception var5) {
				LOGGER.error("Failed to deserialize timer callback: " + compoundTag, (Throwable)var5);
				return null;
			}
		}
	}
}
