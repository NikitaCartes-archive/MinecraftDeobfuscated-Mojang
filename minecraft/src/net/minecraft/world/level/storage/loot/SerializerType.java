package net.minecraft.world.level.storage.loot;

public class SerializerType<T> {
	private final Serializer<? extends T> serializer;

	public SerializerType(Serializer<? extends T> serializer) {
		this.serializer = serializer;
	}

	public Serializer<? extends T> getSerializer() {
		return this.serializer;
	}
}
