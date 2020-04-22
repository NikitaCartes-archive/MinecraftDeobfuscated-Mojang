package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class Button extends AbstractButton {
	protected final Button.OnPress onPress;

	public Button(int i, int j, int k, int l, Component component, Button.OnPress onPress) {
		super(i, j, k, l, component);
		this.onPress = onPress;
	}

	@Override
	public void onPress() {
		this.onPress.onPress(this);
	}

	@Environment(EnvType.CLIENT)
	public interface OnPress {
		void onPress(Button button);
	}
}
