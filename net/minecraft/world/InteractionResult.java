/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world;

public enum InteractionResult {
    SUCCESS,
    CONSUME,
    PASS,
    FAIL;


    public boolean consumesAction() {
        return this == SUCCESS || this == CONSUME;
    }

    public boolean shouldSwing() {
        return this == SUCCESS;
    }
}

