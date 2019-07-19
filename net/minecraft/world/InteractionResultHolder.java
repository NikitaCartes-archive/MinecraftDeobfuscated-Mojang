/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world;

import net.minecraft.world.InteractionResult;

public class InteractionResultHolder<T> {
    private final InteractionResult result;
    private final T object;

    public InteractionResultHolder(InteractionResult interactionResult, T object) {
        this.result = interactionResult;
        this.object = object;
    }

    public InteractionResult getResult() {
        return this.result;
    }

    public T getObject() {
        return this.object;
    }
}

