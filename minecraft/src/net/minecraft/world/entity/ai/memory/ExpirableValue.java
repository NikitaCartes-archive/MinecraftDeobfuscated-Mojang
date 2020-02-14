package net.minecraft.world.entity.ai.memory;

import com.google.common.collect.Maps;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.util.Serializable;

public class ExpirableValue<T> implements Serializable {
	private final T value;
	private final long expireAtGameTime;

	public ExpirableValue(T object, long l) {
		this.value = object;
		this.expireAtGameTime = l;
	}

	public ExpirableValue(T object) {
		this(object, Long.MAX_VALUE);
	}

	public ExpirableValue(Function<Dynamic<?>, T> function, Dynamic<?> dynamic) {
		this((T)function.apply(dynamic.get("value").get().orElseThrow(RuntimeException::new)), dynamic.get("expiry").asLong(Long.MAX_VALUE));
	}

	public static <T> ExpirableValue<T> of(T object) {
		return new ExpirableValue<>(object);
	}

	public static <T> ExpirableValue<T> of(T object, long l) {
		return new ExpirableValue<>(object, l);
	}

	public long getExpireAtGameTime() {
		return this.expireAtGameTime;
	}

	public T getValue() {
		return this.value;
	}

	public boolean hasExpired(long l) {
		return this.getRemainingTime(l) <= 0L;
	}

	public long getRemainingTime(long l) {
		return this.expireAtGameTime - l;
	}

	public String toString() {
		return this.value.toString() + (this.getExpireAtGameTime() != Long.MAX_VALUE ? " (expiry: " + this.expireAtGameTime + ")" : "");
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		Map<T, T> map = Maps.<T, T>newHashMap();
		map.put(dynamicOps.createString("value"), ((Serializable)this.value).serialize(dynamicOps));
		if (this.expireAtGameTime != Long.MAX_VALUE) {
			map.put(dynamicOps.createString("expiry"), dynamicOps.createLong(this.expireAtGameTime));
		}

		return dynamicOps.createMap(map);
	}
}
