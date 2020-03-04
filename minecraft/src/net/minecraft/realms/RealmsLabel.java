package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public class RealmsLabel implements GuiEventListener {
	private final String text;
	private final int x;
	private final int y;
	private final int color;

	public RealmsLabel(String string, int i, int j, int k) {
		this.text = string;
		this.x = i;
		this.y = j;
		this.color = k;
	}

	public void render(Screen screen) {
		screen.drawCenteredString(Minecraft.getInstance().font, this.text, this.x, this.y, this.color);
	}

	public String getText() {
		return this.text;
	}
}
