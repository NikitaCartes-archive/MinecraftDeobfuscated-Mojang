package net.minecraft.world.level.entity;

public interface LevelCallback<T> {
	void onCreated(T object);

	void onDestroyed(T object);

	void onTickingStart(T object);

	void onTickingEnd(T object);

	void onTrackingStart(T object);

	void onTrackingEnd(T object);

	void onSectionChange(T object);
}
