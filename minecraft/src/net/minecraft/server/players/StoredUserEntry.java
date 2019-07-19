package net.minecraft.server.players;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;

public class StoredUserEntry<T> {
	@Nullable
	private final T user;

	public StoredUserEntry(T object) {
		this.user = object;
	}

	protected StoredUserEntry(@Nullable T object, JsonObject jsonObject) {
		this.user = object;
	}

	@Nullable
	T getUser() {
		return this.user;
	}

	boolean hasExpired() {
		return false;
	}

	protected void serialize(JsonObject jsonObject) {
	}
}
