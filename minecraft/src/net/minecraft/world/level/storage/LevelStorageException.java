package net.minecraft.world.level.storage;

import net.minecraft.network.chat.Component;

public class LevelStorageException extends RuntimeException {
	private final Component messageComponent;

	public LevelStorageException(Component component) {
		super(component.getString());
		this.messageComponent = component;
	}

	public Component getMessageComponent() {
		return this.messageComponent;
	}
}
