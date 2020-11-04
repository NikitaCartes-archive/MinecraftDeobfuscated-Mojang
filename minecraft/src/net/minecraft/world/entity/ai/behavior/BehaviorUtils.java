package net.minecraft.world.entity.ai.behavior;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.phys.Vec3;

public class BehaviorUtils {
	public static void lockGazeAndWalkToEachOther(LivingEntity livingEntity, LivingEntity livingEntity2, float f) {
		lookAtEachOther(livingEntity, livingEntity2);
		setWalkAndLookTargetMemoriesToEachOther(livingEntity, livingEntity2, f);
	}

	public static boolean entityIsVisible(Brain<?> brain, LivingEntity livingEntity) {
		return brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).filter(list -> list.contains(livingEntity)).isPresent();
	}

	public static boolean targetIsValid(Brain<?> brain, MemoryModuleType<? extends LivingEntity> memoryModuleType, EntityType<?> entityType) {
		return targetIsValid(brain, memoryModuleType, livingEntity -> livingEntity.getType() == entityType);
	}

	private static boolean targetIsValid(Brain<?> brain, MemoryModuleType<? extends LivingEntity> memoryModuleType, Predicate<LivingEntity> predicate) {
		return brain.getMemory(memoryModuleType)
			.filter(predicate)
			.filter(LivingEntity::isAlive)
			.filter(livingEntity -> entityIsVisible(brain, livingEntity))
			.isPresent();
	}

	private static void lookAtEachOther(LivingEntity livingEntity, LivingEntity livingEntity2) {
		lookAtEntity(livingEntity, livingEntity2);
		lookAtEntity(livingEntity2, livingEntity);
	}

	public static void lookAtEntity(LivingEntity livingEntity, LivingEntity livingEntity2) {
		livingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingEntity2, true));
	}

	private static void setWalkAndLookTargetMemoriesToEachOther(LivingEntity livingEntity, LivingEntity livingEntity2, float f) {
		int i = 2;
		setWalkAndLookTargetMemories(livingEntity, livingEntity2, f, 2);
		setWalkAndLookTargetMemories(livingEntity2, livingEntity, f, 2);
	}

	public static void setWalkAndLookTargetMemories(LivingEntity livingEntity, Entity entity, float f, int i) {
		WalkTarget walkTarget = new WalkTarget(new EntityTracker(entity, false), f, i);
		livingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(entity, true));
		livingEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
	}

	public static void setWalkAndLookTargetMemories(LivingEntity livingEntity, BlockPos blockPos, float f, int i) {
		WalkTarget walkTarget = new WalkTarget(new BlockPosTracker(blockPos), f, i);
		livingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(blockPos));
		livingEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
	}

	public static void throwItem(LivingEntity livingEntity, ItemStack itemStack, Vec3 vec3) {
		double d = livingEntity.getEyeY() - 0.3F;
		ItemEntity itemEntity = new ItemEntity(livingEntity.level, livingEntity.getX(), d, livingEntity.getZ(), itemStack);
		float f = 0.3F;
		Vec3 vec32 = vec3.subtract(livingEntity.position());
		vec32 = vec32.normalize().scale(0.3F);
		itemEntity.setDeltaMovement(vec32);
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

	public static boolean isWithinAttackRange(Mob mob, LivingEntity livingEntity, int i) {
		Item item = mob.getMainHandItem().getItem();
		if (item instanceof ProjectileWeaponItem && mob.canFireProjectileWeapon((ProjectileWeaponItem)item)) {
			int j = ((ProjectileWeaponItem)item).getDefaultProjectileRange() - i;
			return mob.closerThan(livingEntity, (double)j);
		} else {
			return isWithinMeleeAttackRange(mob, livingEntity);
		}
	}

	public static boolean isWithinMeleeAttackRange(LivingEntity livingEntity, LivingEntity livingEntity2) {
		double d = livingEntity.distanceToSqr(livingEntity2.getX(), livingEntity2.getY(), livingEntity2.getZ());
		double e = (double)(livingEntity.getBbWidth() * 2.0F * livingEntity.getBbWidth() * 2.0F + livingEntity2.getBbWidth());
		return d <= e;
	}

	public static boolean isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(LivingEntity livingEntity, LivingEntity livingEntity2, double d) {
		Optional<LivingEntity> optional = livingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
		if (!optional.isPresent()) {
			return false;
		} else {
			double e = livingEntity.distanceToSqr(((LivingEntity)optional.get()).position());
			double f = livingEntity.distanceToSqr(livingEntity2.position());
			return f > e + d * d;
		}
	}

	public static boolean canSee(LivingEntity livingEntity, LivingEntity livingEntity2) {
		Brain<?> brain = livingEntity.getBrain();
		return !brain.hasMemoryValue(MemoryModuleType.VISIBLE_LIVING_ENTITIES)
			? false
			: ((List)brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get()).contains(livingEntity2);
	}

	public static LivingEntity getNearestTarget(LivingEntity livingEntity, Optional<LivingEntity> optional, LivingEntity livingEntity2) {
		return !optional.isPresent() ? livingEntity2 : getTargetNearestMe(livingEntity, (LivingEntity)optional.get(), livingEntity2);
	}

	public static LivingEntity getTargetNearestMe(LivingEntity livingEntity, LivingEntity livingEntity2, LivingEntity livingEntity3) {
		Vec3 vec3 = livingEntity2.position();
		Vec3 vec32 = livingEntity3.position();
		return livingEntity.distanceToSqr(vec3) < livingEntity.distanceToSqr(vec32) ? livingEntity2 : livingEntity3;
	}

	public static Optional<LivingEntity> getLivingEntityFromUUIDMemory(LivingEntity livingEntity, MemoryModuleType<UUID> memoryModuleType) {
		Optional<UUID> optional = livingEntity.getBrain().getMemory(memoryModuleType);
		return optional.map(uUID -> ((ServerLevel)livingEntity.level).getEntity(uUID)).map(entity -> entity instanceof LivingEntity ? (LivingEntity)entity : null);
	}

	public static Stream<Villager> getNearbyVillagersWithCondition(Villager villager, Predicate<Villager> predicate) {
		return (Stream<Villager>)villager.getBrain()
			.getMemory(MemoryModuleType.LIVING_ENTITIES)
			.map(
				list -> list.stream()
						.filter(livingEntity -> livingEntity instanceof Villager && livingEntity != villager)
						.map(livingEntity -> (Villager)livingEntity)
						.filter(LivingEntity::isAlive)
						.filter(predicate)
			)
			.orElseGet(Stream::empty);
	}
}
