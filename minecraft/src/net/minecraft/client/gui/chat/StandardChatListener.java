package net.minecraft.client.gui.chat;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class StandardChatListener implements ChatListener {
	private final Minecraft minecraft;

	public StandardChatListener(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void handle(ChatType chatType, Component component, UUID uUID) {
		if (!this.minecraft.isBlocked(uUID)) {
			if (chatType != ChatType.CHAT) {
				this.minecraft.gui.getChat().addMessage(component);
			} else {
				this.minecraft.gui.getChat().enqueueMessage(component);
			}
		}
	}
}
