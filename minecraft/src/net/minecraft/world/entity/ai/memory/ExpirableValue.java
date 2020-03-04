package net.minecraft.world.entity.ai.memory;

import com.google.common.collect.Maps;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.util.Serializable;

public class ExpirableValue<T> implements Serializable {
	private final T value;
	private long timeToLive;

	public ExpirableValue(T object, long l) {
		this.value = object;
		this.timeToLive = l;
	}

	public ExpirableValue(T object) {
		this(object, Long.MAX_VALUE);
	}

	public ExpirableValue(Function<Dynamic<?>, T> function, Dynamic<?> dynamic) {
		this((T)function.apply(dynamic.get("value").get().orElseThrow(RuntimeException::new)), dynamic.get("ttl").asLong(Long.MAX_VALUE));
	}

	public void tick() {
		if (this.canExpire()) {
			this.timeToLive--;
		}
	}

	public static <T> ExpirableValue<T> of(T object) {
		return new ExpirableValue<>(object);
	}

	public static <T> ExpirableValue<T> of(T object, long l) {
		return new ExpirableValue<>(object, l);
	}

	public T getValue() {
		return this.value;
	}

	public boolean hasExpired() {
		return this.timeToLive <= 0L;
	}

	public String toString() {
		return this.value.toString() + (this.canExpire() ? " (ttl: " + this.timeToLive + ")" : "");
	}

	public boolean canExpire() {
		return this.timeToLive != Long.MAX_VALUE;
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		Map<T, T> map = Maps.<T, T>newHashMap();
		map.put(dynamicOps.createString("value"), ((Serializable)this.value).serialize(dynamicOps));
		if (this.canExpire()) {
			map.put(dynamicOps.createString("ttl"), dynamicOps.createLong(this.timeToLive));
		}

		return dynamicOps.createMap(map);
	}
}
