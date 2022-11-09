package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.GameRules;

public class StopBeingAngryIfTargetDead {
	public static BehaviorControl<LivingEntity> create() {
		return BehaviorBuilder.create(
			instance -> instance.group(instance.present(MemoryModuleType.ANGRY_AT))
					.apply(
						instance,
						memoryAccessor -> (serverLevel, livingEntity, l) -> {
								Optional.ofNullable(serverLevel.getEntity(instance.get(memoryAccessor)))
									.map(entity -> entity instanceof LivingEntity livingEntityx ? livingEntityx : null)
									.filter(LivingEntity::isDeadOrDying)
									.filter(livingEntityx -> livingEntityx.getType() != EntityType.PLAYER || serverLevel.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS))
									.ifPresent(livingEntityx -> memoryAccessor.erase());
								return true;
							}
					)
		);
	}
}
