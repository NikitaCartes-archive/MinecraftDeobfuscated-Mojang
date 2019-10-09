package net.minecraft.world.entity.ai.behavior;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class BehaviorUtils {
	public static void lockGazeAndWalkToEachOther(LivingEntity livingEntity, LivingEntity livingEntity2) {
		lookAtEachOther(livingEntity, livingEntity2);
		walkToEachOther(livingEntity, livingEntity2);
	}

	public static boolean entityIsVisible(Brain<?> brain, LivingEntity livingEntity) {
		return brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).filter(list -> list.contains(livingEntity)).isPresent();
	}

	public static boolean targetIsValid(Brain<?> brain, MemoryModuleType<? extends LivingEntity> memoryModuleType, EntityType<?> entityType) {
		return brain.getMemory(memoryModuleType)
			.filter(livingEntity -> livingEntity.getType() == entityType)
			.filter(LivingEntity::isAlive)
			.filter(livingEntity -> entityIsVisible(brain, livingEntity))
			.isPresent();
	}

	public static void lookAtEachOther(LivingEntity livingEntity, LivingEntity livingEntity2) {
		lookAtEntity(livingEntity, livingEntity2);
		lookAtEntity(livingEntity2, livingEntity);
	}

	public static void lookAtEntity(LivingEntity livingEntity, LivingEntity livingEntity2) {
		livingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(livingEntity2));
	}

	public static void walkToEachOther(LivingEntity livingEntity, LivingEntity livingEntity2) {
		int i = 2;
		walkToEntity(livingEntity, livingEntity2, 2);
		walkToEntity(livingEntity2, livingEntity, 2);
	}

	public static void walkToEntity(LivingEntity livingEntity, LivingEntity livingEntity2, int i) {
		float f = (float)livingEntity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
		EntityPosWrapper entityPosWrapper = new EntityPosWrapper(livingEntity2);
		WalkTarget walkTarget = new WalkTarget(entityPosWrapper, f, i);
		livingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, entityPosWrapper);
		livingEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
	}

	public static void throwItem(LivingEntity livingEntity, ItemStack itemStack, LivingEntity livingEntity2) {
		double d = livingEntity.getEyeY() - 0.3F;
		ItemEntity itemEntity = new ItemEntity(livingEntity.level, livingEntity.getX(), d, livingEntity.getZ(), itemStack);
		BlockPos blockPos = new BlockPos(livingEntity2);
		BlockPos blockPos2 = new BlockPos(livingEntity);
		float f = 0.3F;
		Vec3 vec3 = new Vec3(blockPos.subtract(blockPos2));
		vec3 = vec3.normalize().scale(0.3F);
		itemEntity.setDeltaMovement(vec3);
		itemEntity.setDefaultPickUpDelay();
		livingEntity.level.addFreshEntity(itemEntity);
	}

	public static SectionPos findSectionClosestToVillage(ServerLevel serverLevel, SectionPos sectionPos, int i) {
		int j = serverLevel.sectionsToVillage(sectionPos);
		return (SectionPos)SectionPos.cube(sectionPos, i)
			.filter(sectionPosx -> serverLevel.sectionsToVillage(sectionPosx) < j)
			.min(Comparator.comparingInt(serverLevel::sectionsToVillage))
			.orElse(sectionPos);
	}
}
