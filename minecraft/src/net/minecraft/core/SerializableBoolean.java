package net.minecraft.core;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.util.Serializable;

public final class SerializableBoolean implements Serializable {
	private final boolean value;

	private SerializableBoolean(boolean bl) {
		this.value = bl;
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		return dynamicOps.createBoolean(this.value);
	}

	public static SerializableBoolean of(Dynamic<?> dynamic) {
		return new SerializableBoolean(dynamic.asBoolean(false));
	}

	public static SerializableBoolean of(boolean bl) {
		return new SerializableBoolean(bl);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			SerializableBoolean serializableBoolean = (SerializableBoolean)object;
			return this.value == serializableBoolean.value;
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Boolean.hashCode(this.value);
	}

	public String toString() {
		return Boolean.toString(this.value);
	}
}
