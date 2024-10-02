package net.minecraft.world.entity.ai.behavior;

import java.util.function.Predicate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;

public class MeleeAttack {
	public static <T extends Mob> OneShot<T> create(int i) {
		return create(mob -> true, i);
	}

	public static <T extends Mob> OneShot<T> create(Predicate<T> predicate, int i) {
		return BehaviorBuilder.create(
			instance -> instance.group(
						instance.registered(MemoryModuleType.LOOK_TARGET),
						instance.present(MemoryModuleType.ATTACK_TARGET),
						instance.absent(MemoryModuleType.ATTACK_COOLING_DOWN),
						instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
					)
					.apply(
						instance,
						(memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> (serverLevel, mob, l) -> {
								LivingEntity livingEntity = instance.get(memoryAccessor2);
								if (predicate.test(mob)
									&& !isHoldingUsableProjectileWeapon(mob)
									&& mob.isWithinMeleeAttackRange(livingEntity)
									&& instance.<NearestVisibleLivingEntities>get(memoryAccessor4).contains(livingEntity)) {
									memoryAccessor.set(new EntityTracker(livingEntity, true));
									mob.swing(InteractionHand.MAIN_HAND);
									mob.doHurtTarget(serverLevel, livingEntity);
									memoryAccessor3.setWithExpiry(true, (long)i);
									return true;
								} else {
									return false;
								}
							}
					)
		);
	}

	private static boolean isHoldingUsableProjectileWeapon(Mob mob) {
		return mob.isHolding(itemStack -> {
			Item item = itemStack.getItem();
			return item instanceof ProjectileWeaponItem && mob.canFireProjectileWeapon((ProjectileWeaponItem)item);
		});
	}
}
