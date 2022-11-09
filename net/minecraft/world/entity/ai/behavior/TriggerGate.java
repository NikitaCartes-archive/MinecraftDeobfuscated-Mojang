/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;

public class TriggerGate {
    public static <E extends LivingEntity> OneShot<E> triggerOneShuffled(List<Pair<? extends Trigger<? super E>, Integer>> list) {
        return TriggerGate.triggerGate(list, GateBehavior.OrderPolicy.SHUFFLED, GateBehavior.RunningPolicy.RUN_ONE);
    }

    public static <E extends LivingEntity> OneShot<E> triggerGate(List<Pair<? extends Trigger<? super E>, Integer>> list, GateBehavior.OrderPolicy orderPolicy, GateBehavior.RunningPolicy runningPolicy) {
        ShufflingList shufflingList = new ShufflingList();
        list.forEach(pair -> shufflingList.add((Trigger)pair.getFirst(), (Integer)pair.getSecond()));
        return BehaviorBuilder.create(instance -> instance.point((serverLevel, livingEntity, l) -> {
            Trigger trigger;
            if (orderPolicy == GateBehavior.OrderPolicy.SHUFFLED) {
                shufflingList.shuffle();
            }
            Iterator iterator = shufflingList.iterator();
            while (iterator.hasNext() && (!(trigger = (Trigger)iterator.next()).trigger(serverLevel, livingEntity, l) || runningPolicy != GateBehavior.RunningPolicy.RUN_ONE)) {
            }
            return true;
        }));
    }
}

