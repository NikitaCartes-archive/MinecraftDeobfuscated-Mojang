/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BehaviorUtils {
    private BehaviorUtils() {
    }

    public static void lockGazeAndWalkToEachOther(LivingEntity livingEntity, LivingEntity livingEntity2, float f) {
        BehaviorUtils.lookAtEachOther(livingEntity, livingEntity2);
        BehaviorUtils.setWalkAndLookTargetMemoriesToEachOther(livingEntity, livingEntity2, f);
    }

    public static boolean entityIsVisible(Brain<?> brain, LivingEntity livingEntity) {
        Optional<NearestVisibleLivingEntities> optional = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        return optional.isPresent() && optional.get().contains(livingEntity);
    }

    public static boolean targetIsValid(Brain<?> brain, MemoryModuleType<? extends LivingEntity> memoryModuleType, EntityType<?> entityType) {
        return BehaviorUtils.targetIsValid(brain, memoryModuleType, (LivingEntity livingEntity) -> livingEntity.getType() == entityType);
    }

    private static boolean targetIsValid(Brain<?> brain, MemoryModuleType<? extends LivingEntity> memoryModuleType, Predicate<LivingEntity> predicate) {
        return brain.getMemory(memoryModuleType).filter(predicate).filter(LivingEntity::isAlive).filter(livingEntity -> BehaviorUtils.entityIsVisible(brain, livingEntity)).isPresent();
    }

    private static void lookAtEachOther(LivingEntity livingEntity, LivingEntity livingEntity2) {
        BehaviorUtils.lookAtEntity(livingEntity, livingEntity2);
        BehaviorUtils.lookAtEntity(livingEntity2, livingEntity);
    }

    public static void lookAtEntity(LivingEntity livingEntity, LivingEntity livingEntity2) {
        livingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingEntity2, true));
    }

    private static void setWalkAndLookTargetMemoriesToEachOther(LivingEntity livingEntity, LivingEntity livingEntity2, float f) {
        int i = 2;
        BehaviorUtils.setWalkAndLookTargetMemories(livingEntity, livingEntity2, f, 2);
        BehaviorUtils.setWalkAndLookTargetMemories(livingEntity2, livingEntity, f, 2);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity livingEntity, Entity entity, float f, int i) {
        BehaviorUtils.setWalkAndLookTargetMemories(livingEntity, new EntityTracker(entity, true), f, i);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity livingEntity, BlockPos blockPos, float f, int i) {
        BehaviorUtils.setWalkAndLookTargetMemories(livingEntity, new BlockPosTracker(blockPos), f, i);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity livingEntity, PositionTracker positionTracker, float f, int i) {
        WalkTarget walkTarget = new WalkTarget(positionTracker, f, i);
        livingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, positionTracker);
        livingEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
    }

    public static void throwItem(LivingEntity livingEntity, ItemStack itemStack, Vec3 vec3) {
        Vec3 vec32 = new Vec3(0.3f, 0.3f, 0.3f);
        BehaviorUtils.throwItem(livingEntity, itemStack, vec3, vec32, 0.3f);
    }

    public static void throwItem(LivingEntity livingEntity, ItemStack itemStack, Vec3 vec3, Vec3 vec32, float f) {
        double d = livingEntity.getEyeY() - (double)f;
        ItemEntity itemEntity = new ItemEntity(livingEntity.level, livingEntity.getX(), d, livingEntity.getZ(), itemStack);
        itemEntity.setThrower(livingEntity.getUUID());
        Vec3 vec33 = vec3.subtract(livingEntity.position());
        vec33 = vec33.normalize().multiply(vec32.x, vec32.y, vec32.z);
        itemEntity.setDeltaMovement(vec33);
        itemEntity.setDefaultPickUpDelay();
        livingEntity.level.addFreshEntity(itemEntity);
    }

    public static SectionPos findSectionClosestToVillage(ServerLevel serverLevel, SectionPos sectionPos2, int i) {
        int j = serverLevel.sectionsToVillage(sectionPos2);
        return SectionPos.cube(sectionPos2, i).filter(sectionPos -> serverLevel.sectionsToVillage((SectionPos)sectionPos) < j).min(Comparator.comparingInt(serverLevel::sectionsToVillage)).orElse(sectionPos2);
    }

    public static boolean isWithinAttackRange(Mob mob, LivingEntity livingEntity, int i) {
        ProjectileWeaponItem projectileWeaponItem;
        Item item = mob.getMainHandItem().getItem();
        if (item instanceof ProjectileWeaponItem && mob.canFireProjectileWeapon(projectileWeaponItem = (ProjectileWeaponItem)item)) {
            int j = projectileWeaponItem.getDefaultProjectileRange() - i;
            return mob.closerThan(livingEntity, j);
        }
        return mob.isWithinMeleeAttackRange(livingEntity);
    }

    public static boolean isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(LivingEntity livingEntity, LivingEntity livingEntity2, double d) {
        Optional<LivingEntity> optional = livingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if (optional.isEmpty()) {
            return false;
        }
        double e = livingEntity.distanceToSqr(optional.get().position());
        double f = livingEntity.distanceToSqr(livingEntity2.position());
        return f > e + d * d;
    }

    public static boolean canSee(LivingEntity livingEntity, LivingEntity livingEntity2) {
        Brain<NearestVisibleLivingEntities> brain = livingEntity.getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)) {
            return false;
        }
        return brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().contains(livingEntity2);
    }

    public static LivingEntity getNearestTarget(LivingEntity livingEntity, Optional<LivingEntity> optional, LivingEntity livingEntity2) {
        if (optional.isEmpty()) {
            return livingEntity2;
        }
        return BehaviorUtils.getTargetNearestMe(livingEntity, optional.get(), livingEntity2);
    }

    public static LivingEntity getTargetNearestMe(LivingEntity livingEntity, LivingEntity livingEntity2, LivingEntity livingEntity3) {
        Vec3 vec3 = livingEntity2.position();
        Vec3 vec32 = livingEntity3.position();
        return livingEntity.distanceToSqr(vec3) < livingEntity.distanceToSqr(vec32) ? livingEntity2 : livingEntity3;
    }

    public static Optional<LivingEntity> getLivingEntityFromUUIDMemory(LivingEntity livingEntity, MemoryModuleType<UUID> memoryModuleType) {
        Optional<UUID> optional = livingEntity.getBrain().getMemory(memoryModuleType);
        return optional.map(uUID -> ((ServerLevel)livingEntity.level).getEntity((UUID)uUID)).map(entity -> {
            LivingEntity livingEntity;
            return entity instanceof LivingEntity ? (livingEntity = (LivingEntity)entity) : null;
        });
    }

    @Nullable
    public static Vec3 getRandomSwimmablePos(PathfinderMob pathfinderMob, int i, int j) {
        Vec3 vec3 = DefaultRandomPos.getPos(pathfinderMob, i, j);
        int k = 0;
        while (vec3 != null && !pathfinderMob.level.getBlockState(BlockPos.containing(vec3)).isPathfindable(pathfinderMob.level, BlockPos.containing(vec3), PathComputationType.WATER) && k++ < 10) {
            vec3 = DefaultRandomPos.getPos(pathfinderMob, i, j);
        }
        return vec3;
    }

    public static boolean isBreeding(LivingEntity livingEntity) {
        return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
    }
}

