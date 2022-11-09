package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;

public class TriggerGate {
	public static <E extends LivingEntity> OneShot<E> triggerOneShuffled(List<Pair<? extends Trigger<? super E>, Integer>> list) {
		return triggerGate(list, GateBehavior.OrderPolicy.SHUFFLED, GateBehavior.RunningPolicy.RUN_ONE);
	}

	public static <E extends LivingEntity> OneShot<E> triggerGate(
		List<Pair<? extends Trigger<? super E>, Integer>> list, GateBehavior.OrderPolicy orderPolicy, GateBehavior.RunningPolicy runningPolicy
	) {
		ShufflingList<Trigger<? super E>> shufflingList = new ShufflingList<>();
		list.forEach(pair -> shufflingList.add((Trigger<? super E>)pair.getFirst(), (Integer)pair.getSecond()));
		return BehaviorBuilder.create(instance -> instance.point((serverLevel, livingEntity, l) -> {
				if (orderPolicy == GateBehavior.OrderPolicy.SHUFFLED) {
					shufflingList.shuffle();
				}

				for (Trigger<? super E> trigger : shufflingList) {
					if (trigger.trigger(serverLevel, (E)livingEntity, l) && runningPolicy == GateBehavior.RunningPolicy.RUN_ONE) {
						break;
					}
				}

				return true;
			}));
	}
}
