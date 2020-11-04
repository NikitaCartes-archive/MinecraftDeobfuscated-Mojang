/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.entity;

import net.minecraft.world.entity.Entity;

public interface EntityInLevelCallback {
    public static final EntityInLevelCallback NULL = new EntityInLevelCallback(){

        @Override
        public void onMove() {
        }

        @Override
        public void onRemove(Entity.RemovalReason removalReason) {
        }
    };

    public void onMove();

    public void onRemove(Entity.RemovalReason var1);
}

