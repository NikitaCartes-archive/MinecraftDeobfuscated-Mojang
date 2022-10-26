/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Renderable;

@Environment(value=EnvType.CLIENT)
public abstract class Overlay
extends GuiComponent
implements Renderable {
    public boolean isPauseScreen() {
        return true;
    }
}

