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

    public static <T> InteractionResultHolder<T> success(T object) {
        return new InteractionResultHolder<T>(InteractionResult.SUCCESS, object);
    }

    public static <T> InteractionResultHolder<T> consume(T object) {
        return new InteractionResultHolder<T>(InteractionResult.CONSUME, object);
    }

    public static <T> InteractionResultHolder<T> pass(T object) {
        return new InteractionResultHolder<T>(InteractionResult.PASS, object);
    }

    public static <T> InteractionResultHolder<T> fail(T object) {
        return new InteractionResultHolder<T>(InteractionResult.FAIL, object);
    }

    public static <T> InteractionResultHolder<T> sidedSuccess(T object, boolean bl) {
        return bl ? InteractionResultHolder.success(object) : InteractionResultHolder.consume(object);
    }
}

