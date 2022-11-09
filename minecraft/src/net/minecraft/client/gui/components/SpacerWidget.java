package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class SpacerWidget extends AbstractWidget {
	public SpacerWidget(int i, int j) {
		this(0, 0, i, j);
	}

	public SpacerWidget(int i, int j, int k, int l) {
		super(i, j, k, l, Component.empty());
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}

	public static AbstractWidget width(int i) {
		return new SpacerWidget(i, 0);
	}

	public static AbstractWidget height(int i) {
		return new SpacerWidget(0, i);
	}
}
