package net.minecraft.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public interface OptionEnum {
	int getId();

	String getKey();

	default Component getCaption() {
		return new TranslatableComponent(this.getKey());
	}
}
