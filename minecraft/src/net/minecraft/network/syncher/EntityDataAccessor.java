package net.minecraft.network.syncher;

public class EntityDataAccessor<T> {
	private final int id;
	private final EntityDataSerializer<T> serializer;

	public EntityDataAccessor(int i, EntityDataSerializer<T> entityDataSerializer) {
		this.id = i;
		this.serializer = entityDataSerializer;
	}

	public int getId() {
		return this.id;
	}

	public EntityDataSerializer<T> getSerializer() {
		return this.serializer;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			EntityDataAccessor<?> entityDataAccessor = (EntityDataAccessor<?>)object;
			return this.id == entityDataAccessor.id;
		} else {
			return false;
		}
	}

	public int hashCode() {
		return this.id;
	}

	public String toString() {
		return "<entity data: " + this.id + ">";
	}
}
