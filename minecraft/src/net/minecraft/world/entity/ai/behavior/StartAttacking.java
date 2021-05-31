package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StartAttacking<E extends Mob> extends Behavior<E> {
	private final Predicate<E> canAttackPredicate;
	private final Function<E, Optional<? extends LivingEntity>> targetFinderFunction;

	public StartAttacking(Predicate<E> predicate, Function<E, Optional<? extends LivingEntity>> function) {
		super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED));
		this.canAttackPredicate = predicate;
		this.targetFinderFunction = function;
	}

	public StartAttacking(Function<E, Optional<? extends LivingEntity>> function) {
		this(mob -> true, function);
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E mob) {
		if (!this.canAttackPredicate.test(mob)) {
			return false;
		} else {
			Optional<? extends LivingEntity> optional = (Optional<? extends LivingEntity>)this.targetFinderFunction.apply(mob);
			return optional.isPresent() ? mob.canAttack((LivingEntity)optional.get()) : false;
		}
	}

	protected void start(ServerLevel serverLevel, E mob, long l) {
		((Optional)this.targetFinderFunction.apply(mob)).ifPresent(livingEntity -> this.setAttackTarget(mob, livingEntity));
	}

	private void setAttackTarget(E mob, LivingEntity livingEntity) {
		mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, livingEntity);
		mob.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
	}
}
