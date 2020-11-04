/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Giant;

@Environment(value=EnvType.CLIENT)
public class GiantZombieModel
extends AbstractZombieModel<Giant> {
    public GiantZombieModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public boolean isAggressive(Giant giant) {
        return false;
    }
}

