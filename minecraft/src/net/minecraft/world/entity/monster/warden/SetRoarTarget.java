package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class SetRoarTarget<E extends Warden> extends Behavior<E> {
	private final Function<E, Optional<? extends LivingEntity>> targetFinderFunction;

	public SetRoarTarget(Function<E, Optional<? extends LivingEntity>> function) {
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
		this.targetFinderFunction = function;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E warden) {
		return ((Optional)this.targetFinderFunction.apply(warden)).filter(Warden::canTargetEntity).isPresent();
	}

	protected void start(ServerLevel serverLevel, E warden, long l) {
		((Optional)this.targetFinderFunction.apply(warden)).ifPresent(livingEntity -> {
			warden.getBrain().setMemory(MemoryModuleType.ROAR_TARGET, livingEntity);
			warden.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		});
	}
}
