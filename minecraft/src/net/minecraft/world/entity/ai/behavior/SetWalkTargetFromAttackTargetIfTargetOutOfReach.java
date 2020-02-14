package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

public class SetWalkTargetFromAttackTargetIfTargetOutOfReach extends Behavior<LivingEntity> {
	private final float speed;

	public SetWalkTargetFromAttackTargetIfTargetOutOfReach(float f) {
		super(
			ImmutableMap.of(
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.VISIBLE_LIVING_ENTITIES,
				MemoryStatus.REGISTERED
			)
		);
		this.speed = f;
	}

	@Override
	protected void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		if (BehaviorUtils.isAttackTargetVisibleAndInRange(livingEntity, this.getAttackRange(livingEntity))) {
			this.clearWalkTarget(livingEntity);
		} else {
			this.setWalkTarget(livingEntity, getAttackTarget(livingEntity));
		}
	}

	private void setWalkTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
		PositionWrapper positionWrapper = new EntityPosWrapper(livingEntity2);
		livingEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(positionWrapper, this.speed, 0));
	}

	private void clearWalkTarget(LivingEntity livingEntity) {
		livingEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
	}

	private static LivingEntity getAttackTarget(LivingEntity livingEntity) {
		return (LivingEntity)livingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
	}

	private double getAttackRange(LivingEntity livingEntity) {
		return Math.max(this.getAttackRange(livingEntity.getMainHandItem()), this.getAttackRange(livingEntity.getOffhandItem()));
	}

	private double getAttackRange(ItemStack itemStack) {
		Item item = itemStack.getItem();
		return item instanceof ProjectileWeaponItem ? (double)((ProjectileWeaponItem)item).getDefaultProjectileRange() - 1.0 : 1.5;
	}
}
