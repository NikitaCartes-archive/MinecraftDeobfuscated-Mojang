package net.minecraft.world.entity.ai.behavior.warden;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class SetWardenLookTarget {
	public static BehaviorControl<LivingEntity> create() {
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.registered(MemoryModuleType.LOOK_TARGET),
						instance.registered(MemoryModuleType.DISTURBANCE_LOCATION),
						instance.registered(MemoryModuleType.ROAR_TARGET),
						instance.absent(MemoryModuleType.ATTACK_TARGET)
					)
					.apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> (serverLevel, livingEntity, l) -> {
							Optional<BlockPos> optional = instance.tryGet(memoryAccessor3).map(Entity::blockPosition).or(() -> instance.tryGet(memoryAccessor2));
							if (optional.isEmpty()) {
								return false;
							} else {
								memoryAccessor.set(new BlockPosTracker((BlockPos)optional.get()));
								return true;
							}
						})
		);
	}
}
