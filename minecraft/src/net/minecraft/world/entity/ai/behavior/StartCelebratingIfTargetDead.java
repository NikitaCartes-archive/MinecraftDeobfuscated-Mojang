package net.minecraft.world.entity.ai.behavior;

import java.util.function.BiPredicate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.GameRules;

public class StartCelebratingIfTargetDead {
	public static BehaviorControl<LivingEntity> create(int i, BiPredicate<LivingEntity, LivingEntity> biPredicate) {
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.present(MemoryModuleType.ATTACK_TARGET),
						instance.registered(MemoryModuleType.ANGRY_AT),
						instance.absent(MemoryModuleType.CELEBRATE_LOCATION),
						instance.registered(MemoryModuleType.DANCING)
					)
					.apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> (serverLevel, livingEntity, l) -> {
							LivingEntity livingEntity2 = instance.get(memoryAccessor);
							if (!livingEntity2.isDeadOrDying()) {
								return false;
							} else {
								if (biPredicate.test(livingEntity, livingEntity2)) {
									memoryAccessor4.setWithExpiry(true, (long)i);
								}

								memoryAccessor3.setWithExpiry(livingEntity2.blockPosition(), (long)i);
								if (livingEntity2.getType() != EntityType.PLAYER || serverLevel.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
									memoryAccessor.erase();
									memoryAccessor2.erase();
								}

								return true;
							}
						})
		);
	}
}
