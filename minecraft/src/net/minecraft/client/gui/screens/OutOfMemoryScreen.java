package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class OutOfMemoryScreen extends Screen {
	private MultiLineLabel message = MultiLineLabel.EMPTY;

	public OutOfMemoryScreen() {
		super(Component.translatable("outOfMemory.error"));
	}

	@Override
	protected void init() {
		this.addRenderableWidget(
			Button.builder(Component.translatable("gui.toTitle"), button -> this.minecraft.setScreen(new TitleScreen()))
				.bounds(this.width / 2 - 155, this.height / 4 + 120 + 12, 150, 20)
				.build()
		);
		this.addRenderableWidget(
			Button.builder(Component.translatable("menu.quit"), button -> this.minecraft.stop())
				.bounds(this.width / 2 - 155 + 160, this.height / 4 + 120 + 12, 150, 20)
				.build()
		);
		this.message = MultiLineLabel.create(this.font, Component.translatable("outOfMemory.message"), 295);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, this.height / 4 - 60 + 20, 16777215);
		this.message.renderLeftAligned(poseStack, this.width / 2 - 145, this.height / 4, 9, 10526880);
		super.render(poseStack, i, j, f);
	}
}
