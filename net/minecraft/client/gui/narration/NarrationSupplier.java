/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.narration;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.narration.NarrationElementOutput;

@Environment(value=EnvType.CLIENT)
public interface NarrationSupplier {
    public void updateNarration(NarrationElementOutput var1);
}

