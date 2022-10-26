package net.minecraft.realms;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class DisconnectedRealmsScreen extends RealmsScreen {
	private final Component reason;
	private MultiLineLabel message = MultiLineLabel.EMPTY;
	private final Screen parent;
	private int textHeight;

	public DisconnectedRealmsScreen(Screen screen, Component component, Component component2) {
		super(component);
		this.parent = screen;
		this.reason = component2;
	}

	@Override
	public void init() {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.setConnectedToRealms(false);
		minecraft.getDownloadedPackSource().clearServerPack();
		this.message = MultiLineLabel.create(this.font, this.reason, this.width - 50);
		this.textHeight = this.message.getLineCount() * 9;
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_BACK, button -> minecraft.setScreen(this.parent))
				.bounds(this.width / 2 - 100, this.height / 2 + this.textHeight / 2 + 9, 200, 20)
				.build()
		);
	}

	@Override
	public Component getNarrationMessage() {
		return Component.empty().append(this.title).append(": ").append(this.reason);
	}

	@Override
	public void onClose() {
		Minecraft.getInstance().setScreen(this.parent);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, this.height / 2 - this.textHeight / 2 - 9 * 2, 11184810);
		this.message.renderCentered(poseStack, this.width / 2, this.height / 2 - this.textHeight / 2);
		super.render(poseStack, i, j, f);
	}
}
