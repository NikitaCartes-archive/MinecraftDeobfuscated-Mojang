package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class OutOfMemoryScreen extends Screen {
	public OutOfMemoryScreen() {
		super(new TextComponent("Out of memory!"));
	}

	@Override
	protected void init() {
		this.addButton(
			new Button(
				this.width / 2 - 155, this.height / 4 + 120 + 12, 150, 20, new TranslatableComponent("gui.toTitle"), button -> this.minecraft.setScreen(new TitleScreen())
			)
		);
		this.addButton(
			new Button(this.width / 2 - 155 + 160, this.height / 4 + 120 + 12, 150, 20, new TranslatableComponent("menu.quit"), button -> this.minecraft.stop())
		);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, this.height / 4 - 60 + 20, 16777215);
		this.drawString(poseStack, this.font, "Minecraft has run out of memory.", this.width / 2 - 140, this.height / 4 - 60 + 60 + 0, 10526880);
		this.drawString(poseStack, this.font, "This could be caused by a bug in the game or by the", this.width / 2 - 140, this.height / 4 - 60 + 60 + 18, 10526880);
		this.drawString(poseStack, this.font, "Java Virtual Machine not being allocated enough", this.width / 2 - 140, this.height / 4 - 60 + 60 + 27, 10526880);
		this.drawString(poseStack, this.font, "memory.", this.width / 2 - 140, this.height / 4 - 60 + 60 + 36, 10526880);
		this.drawString(
			poseStack, this.font, "To prevent level corruption, the current game has quit.", this.width / 2 - 140, this.height / 4 - 60 + 60 + 54, 10526880
		);
		this.drawString(
			poseStack, this.font, "We've tried to free up enough memory to let you go back to", this.width / 2 - 140, this.height / 4 - 60 + 60 + 63, 10526880
		);
		this.drawString(
			poseStack, this.font, "the main menu and back to playing, but this may not have worked.", this.width / 2 - 140, this.height / 4 - 60 + 60 + 72, 10526880
		);
		this.drawString(
			poseStack, this.font, "Please restart the game if you see this message again.", this.width / 2 - 140, this.height / 4 - 60 + 60 + 81, 10526880
		);
		super.render(poseStack, i, j, f);
	}
}
