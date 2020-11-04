/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.entity;

import org.jetbrains.annotations.Nullable;

public interface EntityTypeTest<B, T extends B> {
    public static <B, T extends B> EntityTypeTest<B, T> forClass(final Class<T> class_) {
        return new EntityTypeTest<B, T>(){

            @Override
            @Nullable
            public T tryCast(B object) {
                return class_.isInstance(object) ? object : null;
            }

            @Override
            public Class<? extends B> getBaseClass() {
                return class_;
            }
        };
    }

    @Nullable
    public T tryCast(B var1);

    public Class<? extends B> getBaseClass();
}

