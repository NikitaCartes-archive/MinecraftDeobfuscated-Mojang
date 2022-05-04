/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.ChatListener;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class StandardChatListener
implements ChatListener {
    private final Minecraft minecraft;

    public StandardChatListener(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void handle(ChatType chatType, Component component, @Nullable ChatSender chatSender) {
        chatType.chat().ifPresent(textDisplay -> {
            Component component2 = textDisplay.decorate(component, chatSender);
            if (chatSender == null) {
                this.minecraft.gui.getChat().addMessage(component2);
            } else {
                this.minecraft.gui.getChat().enqueueMessage(component2);
            }
        });
    }
}

