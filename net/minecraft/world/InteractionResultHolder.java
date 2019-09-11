/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.Nullable;

public class InteractionResultHolder<T> {
    private final InteractionResult result;
    private final T object;
    private final boolean swingOnSuccess;

    public InteractionResultHolder(InteractionResult interactionResult, T object, boolean bl) {
        this.result = interactionResult;
        this.object = object;
        this.swingOnSuccess = bl;
    }

    public InteractionResult getResult() {
        return this.result;
    }

    public T getObject() {
        return this.object;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean shouldSwingOnSuccess() {
        return this.swingOnSuccess;
    }

    public static <T> InteractionResultHolder<T> success(T object) {
        return new InteractionResultHolder<T>(InteractionResult.SUCCESS, object, true);
    }

    public static <T> InteractionResultHolder<T> successNoSwing(T object) {
        return new InteractionResultHolder<T>(InteractionResult.SUCCESS, object, false);
    }

    public static <T> InteractionResultHolder<T> pass(@Nullable T object) {
        return new InteractionResultHolder<T>(InteractionResult.PASS, object, false);
    }

    public static <T> InteractionResultHolder<T> fail(@Nullable T object) {
        return new InteractionResultHolder<T>(InteractionResult.FAIL, object, false);
    }
}

