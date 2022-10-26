package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

@Environment(EnvType.CLIENT)
public class InBedChatScreen extends ChatScreen {
	public InBedChatScreen() {
		super("");
	}

	@Override
	protected void init() {
		super.init();
		this.addRenderableWidget(
			Button.builder(Component.translatable("multiplayer.stopSleeping"), button -> this.sendWakeUp())
				.bounds(this.width / 2 - 100, this.height - 40, 200, 20)
				.build()
		);
	}

	@Override
	public void onClose() {
		this.sendWakeUp();
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.sendWakeUp();
		} else if (i == 257 || i == 335) {
			if (this.handleChatInput(this.input.getValue(), true)) {
				this.minecraft.setScreen(null);
				this.input.setValue("");
				this.minecraft.gui.getChat().resetChatScroll();
			}

			return true;
		}

		return super.keyPressed(i, j, k);
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
