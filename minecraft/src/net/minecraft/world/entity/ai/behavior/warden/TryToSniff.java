package net.minecraft.world.entity.ai.behavior.warden;

import net.minecraft.util.Unit;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class TryToSniff {
	private static final IntProvider SNIFF_COOLDOWN = UniformInt.of(100, 200);

	public static BehaviorControl<LivingEntity> create() {
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.registered(MemoryModuleType.IS_SNIFFING),
						instance.registered(MemoryModuleType.WALK_TARGET),
						instance.absent(MemoryModuleType.SNIFF_COOLDOWN),
						instance.present(MemoryModuleType.NEAREST_ATTACKABLE),
						instance.absent(MemoryModuleType.DISTURBANCE_LOCATION)
					)
					.apply(instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4, memoryAccessor5) -> (serverLevel, livingEntity, l) -> {
							memoryAccessor.set(Unit.INSTANCE);
							memoryAccessor3.setWithExpiry(Unit.INSTANCE, (long)SNIFF_COOLDOWN.sample(serverLevel.getRandom()));
							memoryAccessor2.erase();
							livingEntity.setPose(Pose.SNIFFING);
							return true;
						})
		);
	}
}
