package net.minecraft.client.gui.chat;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public interface ChatListener {
	void handle(ChatType chatType, Component component, @Nullable ChatSender chatSender);
}
