package net.minecraft.core;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Arrays;
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
		return serialize(dynamicOps, this.value);
	}

	public static SerializableUUID of(Dynamic<?> dynamic) {
		return new SerializableUUID(readUUID(dynamic));
	}

	public String toString() {
		return this.value.toString();
	}

	public static UUID uuidFromIntArray(int[] is) {
		return new UUID((long)is[0] << 32 | (long)is[1] & 4294967295L, (long)is[2] << 32 | (long)is[3] & 4294967295L);
	}

	public static int[] uuidToIntArray(UUID uUID) {
		long l = uUID.getMostSignificantBits();
		long m = uUID.getLeastSignificantBits();
		return leastMostToIntArray(l, m);
	}

	public static int[] leastMostToIntArray(long l, long m) {
		return new int[]{(int)(l >> 32), (int)l, (int)(m >> 32), (int)m};
	}

	public static UUID readUUID(Dynamic<?> dynamic) {
		int[] is = dynamic.asIntStream().toArray();
		if (is.length != 4) {
			throw new IllegalArgumentException("Could not read UUID. Expected int-array of length 4, got " + is.length + ".");
		} else {
			return uuidFromIntArray(is);
		}
	}

	public static <T> T serialize(DynamicOps<T> dynamicOps, UUID uUID) {
		return dynamicOps.createIntList(Arrays.stream(uuidToIntArray(uUID)));
	}
}
