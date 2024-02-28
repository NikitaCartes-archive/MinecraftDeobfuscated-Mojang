package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

@Environment(EnvType.CLIENT)
public class InBedChatScreen extends ChatScreen {
	private Button leaveBedButton;

	public InBedChatScreen() {
		super("");
	}

	@Override
	protected void init() {
		super.init();
		this.leaveBedButton = Button.builder(Component.translatable("multiplayer.stopSleeping"), button -> this.sendWakeUp())
			.bounds(this.width / 2 - 100, this.height - 40, 200, 20)
			.build();
		this.addRenderableWidget(this.leaveBedButton);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		if (!this.minecraft.getChatStatus().isChatAllowed(this.minecraft.isLocalServer())) {
			this.leaveBedButton.render(guiGraphics, i, j, f);
		} else {
			super.render(guiGraphics, i, j, f);
		}
	}

	@Override
	public void onClose() {
		this.sendWakeUp();
	}

	@Override
	public boolean charTyped(char c, int i) {
		return !this.minecraft.getChatStatus().isChatAllowed(this.minecraft.isLocalServer()) ? true : super.charTyped(c, i);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.sendWakeUp();
		}

		if (!this.minecraft.getChatStatus().isChatAllowed(this.minecraft.isLocalServer())) {
			return true;
		} else if (i != 257 && i != 335) {
			return super.keyPressed(i, j, k);
		} else {
			this.handleChatInput(this.input.getValue(), true);
			this.input.setValue("");
			this.minecraft.gui.getChat().resetChatScroll();
			return true;
		}
	}

	private void sendWakeUp() {
		ClientPacketListener clientPacketListener = this.minecraft.player.connection;
		clientPacketListener.send(new ServerboundPlayerCommandPacket(this.minecraft.player, ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));
	}

	public void onPlayerWokeUp() {
		if (this.input.getValue().isEmpty()) {
			this.minecraft.setScreen(null);
		} else {
			this.minecraft.setScreen(new ChatScreen(this.input.getValue()));
		}
	}
}
