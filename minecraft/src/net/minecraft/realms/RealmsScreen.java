package net.minecraft.realms;

import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public abstract class RealmsScreen extends Screen {
	public RealmsScreen() {
		super(NarratorChatListener.NO_TITLE);
	}

	protected static int row(int i) {
		return 40 + i * 13;
	}

	@Override
	public void tick() {
		for (AbstractWidget abstractWidget : this.buttons) {
			if (abstractWidget instanceof TickableWidget) {
				((TickableWidget)abstractWidget).tick();
			}
		}
	}

	public void narrateLabels() {
		List<String> list = (List<String>)this.children
			.stream()
			.filter(RealmsLabel.class::isInstance)
			.map(RealmsLabel.class::cast)
			.map(RealmsLabel::getText)
			.collect(Collectors.toList());
		NarrationHelper.now(list);
	}
}
