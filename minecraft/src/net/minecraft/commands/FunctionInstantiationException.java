package net.minecraft.commands;

import net.minecraft.network.chat.Component;

public class FunctionInstantiationException extends Exception {
	private final Component messageComponent;

	public FunctionInstantiationException(Component component) {
		super(component.getString());
		this.messageComponent = component;
	}

	public Component messageComponent() {
		return this.messageComponent;
	}
}
