package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

@Environment(EnvType.CLIENT)
public class InBedChatScreen extends ChatScreen {
	public InBedChatScreen() {
		super("");
	}

	@Override
	protected void init() {
		super.init();
		this.addButton(
			new Button(this.width / 2 - 100, this.height - 40, 200, 20, new TranslatableComponent("multiplayer.stopSleeping"), button -> this.sendWakeUp())
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
			String string = this.input.getValue().trim();
			if (!string.isEmpty()) {
				this.sendMessage(string);
			}

			this.input.setValue("");
			this.minecraft.gui.getChat().resetChatScroll();
			return true;
		}

		return super.keyPressed(i, j, k);
	}

	private void sendWakeUp() {
		ClientPacketListener clientPacketListener = this.minecraft.player.connection;
		clientPacketListener.send(new ServerboundPlayerCommandPacket(this.minecraft.player, ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));
	}
}
