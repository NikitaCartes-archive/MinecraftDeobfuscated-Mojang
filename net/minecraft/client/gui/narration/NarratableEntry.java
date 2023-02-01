/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.narration;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.TabOrderedElement;
import net.minecraft.client.gui.narration.NarrationSupplier;

@Environment(value=EnvType.CLIENT)
public interface NarratableEntry
extends TabOrderedElement,
NarrationSupplier {
    public NarrationPriority narrationPriority();

    default public boolean isActive() {
        return true;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum NarrationPriority {
        NONE,
        HOVERED,
        FOCUSED;


        public boolean isTerminal() {
            return this == FOCUSED;
        }
    }
}

