package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class SetRoarTarget<E extends Warden> extends Behavior<E> {
	private final Predicate<E> canRoarPredicate;
	private final Function<E, Optional<? extends LivingEntity>> targetFinderFunction;

	public SetRoarTarget(Predicate<E> predicate, Function<E, Optional<? extends LivingEntity>> function) {
		super(
			ImmutableMap.of(
				MemoryModuleType.ROAR_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
				MemoryStatus.REGISTERED
			)
		);
		this.canRoarPredicate = predicate;
		this.targetFinderFunction = function;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E warden) {
		if (!this.canRoarPredicate.test(warden)) {
			return false;
		} else {
			Optional<? extends LivingEntity> optional = (Optional<? extends LivingEntity>)this.targetFinderFunction.apply(warden);
			return optional.isPresent() && ((LivingEntity)optional.get()).isAlive() && WardenAi.isValidRoarTarget((LivingEntity)optional.get());
		}
	}

	protected void start(ServerLevel serverLevel, E warden, long l) {
		((Optional)this.targetFinderFunction.apply(warden)).ifPresent(livingEntity -> {
			warden.getBrain().setMemory(MemoryModuleType.ROAR_TARGET, livingEntity);
			warden.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		});
	}
}
