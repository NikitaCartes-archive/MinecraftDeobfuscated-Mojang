package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Renderable;

@Environment(EnvType.CLIENT)
public abstract class Overlay extends GuiComponent implements Renderable {
	public boolean isPauseScreen() {
		return true;
	}
}
