/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public interface ChatListener {
    public void handle(ChatType var1, Component var2);
}

