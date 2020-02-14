/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;

public class AdmireHeldItem<E extends PathfinderMob>
extends RunOne<E> {
    public AdmireHeldItem(float f) {
        super(ImmutableList.of(Pair.of(new RandomStroll(f, 1, 0), 1), Pair.of(new DoNothing(10, 20), 1)));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E pathfinderMob) {
        return !((LivingEntity)pathfinderMob).getOffhandItem().isEmpty();
    }
}

