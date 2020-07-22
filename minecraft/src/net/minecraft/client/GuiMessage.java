package net.minecraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class GuiMessage<T> {
	private final int addedTime;
	private final T message;
	private final int id;

	public GuiMessage(int i, T object, int j) {
		this.message = object;
		this.addedTime = i;
		this.id = j;
	}

	public T getMessage() {
		return this.message;
	}

	public int getAddedTime() {
		return this.addedTime;
	}

	public int getId() {
		return this.id;
	}
}
