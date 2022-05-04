package net.minecraft.client.gui.chat;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class OverlayChatListener implements ChatListener {
	private final Minecraft minecraft;

	public OverlayChatListener(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void handle(ChatType chatType, Component component, @Nullable ChatSender chatSender) {
		chatType.overlay().ifPresent(textDisplay -> {
			Component component2 = textDisplay.decorate(component, chatSender);
			this.minecraft.gui.setOverlayMessage(component2, false);
		});
	}
}
