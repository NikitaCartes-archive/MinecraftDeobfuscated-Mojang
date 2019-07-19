package net.minecraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class GuiMessage {
	private final int addedTime;
	private final Component message;
	private final int id;

	public GuiMessage(int i, Component component, int j) {
		this.message = component;
		this.addedTime = i;
		this.id = j;
	}

	public Component getMessage() {
		return this.message;
	}

	public int getAddedTime() {
		return this.addedTime;
	}

	public int getId() {
		return this.id;
	}
}
