/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ambient;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class AmbientCreature
extends Mob {
    protected AmbientCreature(EntityType<? extends AmbientCreature> entityType, Level level) {
        super((EntityType<? extends Mob>)entityType, level);
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }
}

