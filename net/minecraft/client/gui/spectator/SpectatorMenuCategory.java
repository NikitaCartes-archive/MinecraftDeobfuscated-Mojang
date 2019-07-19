/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.spectator;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public interface SpectatorMenuCategory {
    public List<SpectatorMenuItem> getItems();

    public Component getPrompt();
}

