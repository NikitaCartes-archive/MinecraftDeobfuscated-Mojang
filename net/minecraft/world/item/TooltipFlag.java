/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface TooltipFlag {
    public boolean isAdvanced();

    @Environment(value=EnvType.CLIENT)
    public static enum Default implements TooltipFlag
    {
        NORMAL(false),
        ADVANCED(true);

        private final boolean advanced;

        private Default(boolean bl) {
            this.advanced = bl;
        }

        @Override
        public boolean isAdvanced() {
            return this.advanced;
        }
    }
}

