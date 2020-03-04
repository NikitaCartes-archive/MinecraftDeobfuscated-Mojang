/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.realms.RealmsScreen;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsBridge
extends RealmsScreen {
    private Screen previousScreen;

    public void switchToRealms(Screen screen) {
        this.previousScreen = screen;
        Minecraft.getInstance().setScreen(new RealmsMainScreen(this));
    }

    @Nullable
    public RealmsScreen getNotificationScreen(Screen screen) {
        this.previousScreen = screen;
        return new RealmsNotificationsScreen();
    }

    @Override
    public void init() {
        Minecraft.getInstance().setScreen(this.previousScreen);
    }
}

