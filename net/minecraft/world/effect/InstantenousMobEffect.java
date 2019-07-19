/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class InstantenousMobEffect
extends MobEffect {
    public InstantenousMobEffect(MobEffectCategory mobEffectCategory, int i) {
        super(mobEffectCategory, i);
    }

    @Override
    public boolean isInstantenous() {
        return true;
    }

    @Override
    public boolean isDurationEffectTick(int i, int j) {
        return i >= 1;
    }
}

