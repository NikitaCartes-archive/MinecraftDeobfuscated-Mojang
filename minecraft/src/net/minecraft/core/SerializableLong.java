package net.minecraft.core;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.util.Serializable;

public final class SerializableLong implements Serializable {
	private final long value;

	private SerializableLong(long l) {
		this.value = l;
	}

	public long value() {
		return this.value;
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		return dynamicOps.createLong(this.value);
	}

	public static SerializableLong of(Dynamic<?> dynamic) {
		return new SerializableLong(dynamic.asNumber(Integer.valueOf(0)).longValue());
	}

	public static SerializableLong of(long l) {
		return new SerializableLong(l);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			SerializableLong serializableLong = (SerializableLong)object;
			return this.value == serializableLong.value;
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Long.hashCode(this.value);
	}

	public String toString() {
		return Long.toString(this.value);
	}
}
