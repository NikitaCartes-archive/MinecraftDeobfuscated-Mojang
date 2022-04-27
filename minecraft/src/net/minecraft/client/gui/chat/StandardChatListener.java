package net.minecraft.client.gui.chat;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class StandardChatListener implements ChatListener {
	private final Minecraft minecraft;

	public StandardChatListener(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void handle(ChatType chatType, Component component, @Nullable ChatSender chatSender) {
		if (chatType != ChatType.CHAT) {
			this.minecraft.gui.getChat().addMessage(component);
		} else {
			Component component2 = chatSender != null ? decorateMessage(component, chatSender) : component;
			this.minecraft.gui.getChat().enqueueMessage(component2);
		}
	}

	private static Component decorateMessage(Component component, ChatSender chatSender) {
		return Component.translatable("chat.type.text", chatSender.name(), component);
	}
}
