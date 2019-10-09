/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.monster.Giant;

@Environment(value=EnvType.CLIENT)
public class GiantZombieModel
extends AbstractZombieModel<Giant> {
    public GiantZombieModel() {
        this(0.0f, false);
    }

    public GiantZombieModel(float f, boolean bl) {
        super(RenderType::entitySolid, f, 0.0f, 64, bl ? 32 : 64);
    }

    @Override
    public boolean isAggressive(Giant giant) {
        return false;
    }
}

