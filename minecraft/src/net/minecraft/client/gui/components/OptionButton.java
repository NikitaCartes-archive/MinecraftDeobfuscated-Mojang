package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Option;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class OptionButton extends Button {
	private final Option option;

	public OptionButton(int i, int j, int k, int l, Option option, Component component, Button.OnPress onPress) {
		super(i, j, k, l, component, onPress);
		this.option = option;
	}

	public Option getOption() {
		return this.option;
	}
}
