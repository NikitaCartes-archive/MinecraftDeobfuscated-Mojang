/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.RealmsScreen;

@Environment(value=EnvType.CLIENT)
public abstract class RealmsScreenWithCallback<T>
extends RealmsScreen {
    abstract void callback(T var1);
}

