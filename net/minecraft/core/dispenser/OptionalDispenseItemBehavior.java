/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core.dispenser;

import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;

public abstract class OptionalDispenseItemBehavior
extends DefaultDispenseItemBehavior {
    protected boolean success = true;

    @Override
    protected void playSound(BlockSource blockSource) {
        blockSource.getLevel().levelEvent(this.success ? 1000 : 1001, blockSource.getPos(), 0);
    }
}

