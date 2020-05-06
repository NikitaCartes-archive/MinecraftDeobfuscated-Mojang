/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core.dispenser;

import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;

public abstract class OptionalDispenseItemBehavior
extends DefaultDispenseItemBehavior {
    private boolean success = true;

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean bl) {
        this.success = bl;
    }

    @Override
    protected void playSound(BlockSource blockSource) {
        blockSource.getLevel().levelEvent(this.isSuccess() ? 1000 : 1001, blockSource.getPos(), 0);
    }
}

