/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;

@Environment(value=EnvType.CLIENT)
public class ZombieModel<T extends Zombie>
extends AbstractZombieModel<T> {
    public ZombieModel(Function<ResourceLocation, RenderType> function, float f, boolean bl) {
        this(function, f, 0.0f, 64, bl ? 32 : 64);
    }

    protected ZombieModel(Function<ResourceLocation, RenderType> function, float f, float g, int i, int j) {
        super(function, f, g, i, j);
    }

    @Override
    public boolean isAggressive(T zombie) {
        return ((Mob)zombie).isAggressive();
    }
}

