package net.minecraft.world.entity.ai.behavior;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

public class BehaviorUtils {
	private BehaviorUtils() {
	}

	public static void lockGazeAndWalkToEachOther(LivingEntity livingEntity, LivingEntity livingEntity2, float f, int i) {
		lookAtEachOther(livingEntity, livingEntity2);
		setWalkAndLookTargetMemoriesToEachOther(livingEntity, livingEntity2, f, i);
	}

	public static boolean entityIsVisible(Brain<?> brain, LivingEntity livingEntity) {
		Optional<NearestVisibleLivingEntities> optional = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
		return optional.isPresent() && ((NearestVisibleLivingEntities)optional.get()).contains(livingEntity);
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

	private static void setWalkAndLookTargetMemoriesToEachOther(LivingEntity livingEntity, LivingEntity livingEntity2, float f, int i) {
		setWalkAndLookTargetMemories(livingEntity, livingEntity2, f, i);
		setWalkAndLookTargetMemories(livingEntity2, livingEntity, f, i);
	}

	public static void setWalkAndLookTargetMemories(LivingEntity livingEntity, Entity entity, float f, int i) {
		setWalkAndLookTargetMemories(livingEntity, new EntityTracker(entity, true), f, i);
	}

	public static void setWalkAndLookTargetMemories(LivingEntity livingEntity, BlockPos blockPos, float f, int i) {
		setWalkAndLookTargetMemories(livingEntity, new BlockPosTracker(blockPos), f, i);
	}

	public static void setWalkAndLookTargetMemories(LivingEntity livingEntity, PositionTracker positionTracker, float f, int i) {
		WalkTarget walkTarget = new WalkTarget(positionTracker, f, i);
		livingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, positionTracker);
		livingEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
	}

	public static void throwItem(LivingEntity livingEntity, ItemStack itemStack, Vec3 vec3) {
		Vec3 vec32 = new Vec3(0.3F, 0.3F, 0.3F);
		throwItem(livingEntity, itemStack, vec3, vec32, 0.3F);
	}

	public static void throwItem(LivingEntity livingEntity, ItemStack itemStack, Vec3 vec3, Vec3 vec32, float f) {
		double d = livingEntity.getEyeY() - (double)f;
		ItemEntity itemEntity = new ItemEntity(livingEntity.level(), livingEntity.getX(), d, livingEntity.getZ(), itemStack);
		itemEntity.setThrower(livingEntity);
		Vec3 vec33 = vec3.subtract(livingEntity.position());
		vec33 = vec33.normalize().multiply(vec32.x, vec32.y, vec32.z);
		itemEntity.setDeltaMovement(vec33);
		itemEntity.setDefaultPickUpDelay();
		livingEntity.level().addFreshEntity(itemEntity);
	}

	public static SectionPos findSectionClosestToVillage(ServerLevel serverLevel, SectionPos sectionPos, int i) {
		int j = serverLevel.sectionsToVillage(sectionPos);
		return (SectionPos)SectionPos.cube(sectionPos, i)
			.filter(sectionPosx -> serverLevel.sectionsToVillage(sectionPosx) < j)
			.min(Comparator.comparingInt(serverLevel::sectionsToVillage))
			.orElse(sectionPos);
	}

	public static boolean isWithinAttackRange(Mob mob, LivingEntity livingEntity, int i) {
		if (mob.getMainHandItem().getItem() instanceof ProjectileWeaponItem projectileWeaponItem && mob.canFireProjectileWeapon(projectileWeaponItem)) {
			int j = projectileWeaponItem.getDefaultProjectileRange() - i;
			return mob.closerThan(livingEntity, (double)j);
		}

		return mob.isWithinMeleeAttackRange(livingEntity);
	}

	public static boolean isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(LivingEntity livingEntity, LivingEntity livingEntity2, double d) {
		Optional<LivingEntity> optional = livingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
		if (optional.isEmpty()) {
			return false;
		} else {
			double e = livingEntity.distanceToSqr(((LivingEntity)optional.get()).position());
			double f = livingEntity.distanceToSqr(livingEntity2.position());
			return f > e + d * d;
		}
	}

	public static boolean canSee(LivingEntity livingEntity, LivingEntity livingEntity2) {
		Brain<?> brain = livingEntity.getBrain();
		return !brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
			? false
			: ((NearestVisibleLivingEntities)brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get()).contains(livingEntity2);
	}

	public static LivingEntity getNearestTarget(LivingEntity livingEntity, Optional<LivingEntity> optional, LivingEntity livingEntity2) {
		return optional.isEmpty() ? livingEntity2 : getTargetNearestMe(livingEntity, (LivingEntity)optional.get(), livingEntity2);
	}

	public static LivingEntity getTargetNearestMe(LivingEntity livingEntity, LivingEntity livingEntity2, LivingEntity livingEntity3) {
		Vec3 vec3 = livingEntity2.position();
		Vec3 vec32 = livingEntity3.position();
		return livingEntity.distanceToSqr(vec3) < livingEntity.distanceToSqr(vec32) ? livingEntity2 : livingEntity3;
	}

	public static Optional<LivingEntity> getLivingEntityFromUUIDMemory(LivingEntity livingEntity, MemoryModuleType<UUID> memoryModuleType) {
		Optional<UUID> optional = livingEntity.getBrain().getMemory(memoryModuleType);
		return optional.map(uUID -> ((ServerLevel)livingEntity.level()).getEntity(uUID))
			.map(entity -> entity instanceof LivingEntity livingEntityx ? livingEntityx : null);
	}

	@Nullable
	public static Vec3 getRandomSwimmablePos(PathfinderMob pathfinderMob, int i, int j) {
		Vec3 vec3 = DefaultRandomPos.getPos(pathfinderMob, i, j);
		int k = 0;

		while (vec3 != null && !pathfinderMob.level().getBlockState(BlockPos.containing(vec3)).isPathfindable(PathComputationType.WATER) && k++ < 10) {
			vec3 = DefaultRandomPos.getPos(pathfinderMob, i, j);
		}

		return vec3;
	}

	public static boolean isBreeding(LivingEntity livingEntity) {
		return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
	}
}
