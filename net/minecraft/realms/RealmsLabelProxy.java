/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.realms.RealmsLabel;

@Environment(value=EnvType.CLIENT)
public class RealmsLabelProxy
implements GuiEventListener {
    private final RealmsLabel label;

    public RealmsLabelProxy(RealmsLabel realmsLabel) {
        this.label = realmsLabel;
    }

    public RealmsLabel getLabel() {
        return this.label;
    }
}

