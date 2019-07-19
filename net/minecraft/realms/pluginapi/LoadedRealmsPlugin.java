/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms.pluginapi;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.RealmsScreen;

@Environment(value=EnvType.CLIENT)
public interface LoadedRealmsPlugin {
    public RealmsScreen getMainScreen(RealmsScreen var1);

    public RealmsScreen getNotificationsScreen(RealmsScreen var1);
}

