/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.spectator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.spectator.SpectatorMenu;

@Environment(value=EnvType.CLIENT)
public interface SpectatorMenuListener {
    public void onSpectatorMenuClosed(SpectatorMenu var1);
}

