package net.minecraft.core;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.UUID;
import net.minecraft.util.Serializable;

public final class SerializableUUID implements Serializable {
	private final UUID value;

	public SerializableUUID(UUID uUID) {
		this.value = uUID;
	}

	public UUID value() {
		return this.value;
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		return dynamicOps.createString(this.value.toString());
	}

	public static SerializableUUID of(Dynamic<?> dynamic) {
		String string = (String)dynamic.asString().orElseThrow(() -> new IllegalArgumentException("Could not parse UUID"));
		return new SerializableUUID(UUID.fromString(string));
	}

	public String toString() {
		return this.value.toString();
	}
}
