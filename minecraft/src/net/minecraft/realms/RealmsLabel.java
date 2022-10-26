package net.minecraft.realms;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class RealmsLabel implements Renderable {
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

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		GuiComponent.drawCenteredString(poseStack, Minecraft.getInstance().font, this.text, this.x, this.y, this.color);
	}

	public Component getText() {
		return this.text;
	}
}
