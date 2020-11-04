/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;

@Environment(value=EnvType.CLIENT)
public class ZombieModel<T extends Zombie>
extends AbstractZombieModel<T> {
    public ZombieModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public boolean isAggressive(T zombie) {
        return ((Mob)zombie).isAggressive();
    }
}

