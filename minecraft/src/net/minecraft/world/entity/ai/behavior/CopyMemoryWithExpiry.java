package net.minecraft.world.entity.ai.behavior;

import java.util.function.Predicate;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class CopyMemoryWithExpiry {
	public static <E extends LivingEntity, T> BehaviorControl<E> create(
		Predicate<E> predicate, MemoryModuleType<? extends T> memoryModuleType, MemoryModuleType<T> memoryModuleType2, UniformInt uniformInt
	) {
		return BehaviorBuilder.create(
			instance -> instance.group(instance.present(memoryModuleType), instance.absent(memoryModuleType2))
					.apply(instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, livingEntity, l) -> {
							if (!predicate.test(livingEntity)) {
								return false;
							} else {
								memoryAccessor2.setWithExpiry(instance.get(memoryAccessor), (long)uniformInt.sample(serverLevel.random));
								return true;
							}
						})
		);
	}
}
