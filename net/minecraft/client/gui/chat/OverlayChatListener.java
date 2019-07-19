/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class OverlayChatListener
implements ChatListener {
    private final Minecraft minecraft;

    public OverlayChatListener(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void handle(ChatType chatType, Component component) {
        this.minecraft.gui.setOverlayMessage(component, false);
    }
}

