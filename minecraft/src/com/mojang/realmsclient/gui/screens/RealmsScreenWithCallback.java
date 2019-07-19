package com.mojang.realmsclient.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public abstract class RealmsScreenWithCallback<T> extends RealmsScreen {
	abstract void callback(T object);
}
