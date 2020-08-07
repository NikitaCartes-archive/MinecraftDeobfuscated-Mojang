package net.minecraft.realms;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class RealmsLabel implements GuiEventListener {
	private final Component text;
	private final int x;
	private final int y;
	private final int color;

	public RealmsLabel(Component component, int i, int j, int k) {
		this.text = component;
		this.x = i;
		this.y = j;
		this.color = k;
	}

	public void render(Screen screen, PoseStack poseStack) {
		Screen.drawCenteredString(poseStack, Minecraft.getInstance().font, this.text, this.x, this.y, this.color);
	}

	public String getText() {
		return this.text.getString();
	}
}
