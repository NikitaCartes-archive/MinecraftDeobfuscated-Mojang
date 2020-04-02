/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.SerializableUUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.phys.Vec3;

public class BehaviorUtils {
    public static void lockGazeAndWalkToEachOther(LivingEntity livingEntity, LivingEntity livingEntity2, float f) {
        BehaviorUtils.lookAtEachOther(livingEntity, livingEntity2);
        BehaviorUtils.setWalkAndLookTargetMemoriesToEachOther(livingEntity, livingEntity2, f);
    }

    public static boolean entityIsVisible(Brain<?> brain, LivingEntity livingEntity) {
        return brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).filter(list -> list.contains(livingEntity)).isPresent();
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
        livingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingEntity2));
    }

    private static void setWalkAndLookTargetMemoriesToEachOther(LivingEntity livingEntity, LivingEntity livingEntity2, float f) {
        int i = 2;
        BehaviorUtils.setWalkAndLookTargetMemories(livingEntity, livingEntity2, f, 2);
        BehaviorUtils.setWalkAndLookTargetMemories(livingEntity2, livingEntity, f, 2);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity livingEntity, Entity entity, float f, int i) {
        EntityTracker positionTracker = new EntityTracker(entity);
        BehaviorUtils.setWalkAndLookTargetMemories(livingEntity, positionTracker, f, i);
    }

    public static void setWalkAndLookTargetMemories(LivingEntity livingEntity, BlockPos blockPos, float f, int i) {
        BlockPosTracker positionTracker = new BlockPosTracker(blockPos);
        BehaviorUtils.setWalkAndLookTargetMemories(livingEntity, positionTracker, f, i);
    }

    private static void setWalkAndLookTargetMemories(LivingEntity livingEntity, PositionTracker positionTracker, float f, int i) {
        WalkTarget walkTarget = new WalkTarget(positionTracker, f, i);
        livingEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, positionTracker);
        livingEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
    }

    public static void throwItem(LivingEntity livingEntity, ItemStack itemStack, Vec3 vec3) {
        double d = livingEntity.getEyeY() - (double)0.3f;
        ItemEntity itemEntity = new ItemEntity(livingEntity.level, livingEntity.getX(), d, livingEntity.getZ(), itemStack);
        float f = 0.3f;
        Vec3 vec32 = vec3.subtract(livingEntity.position());
        vec32 = vec32.normalize().scale(0.3f);
        itemEntity.setDeltaMovement(vec32);
        itemEntity.setDefaultPickUpDelay();
        livingEntity.level.addFreshEntity(itemEntity);
    }

    public static SectionPos findSectionClosestToVillage(ServerLevel serverLevel, SectionPos sectionPos2, int i) {
        int j = serverLevel.sectionsToVillage(sectionPos2);
        return SectionPos.cube(sectionPos2, i).filter(sectionPos -> serverLevel.sectionsToVillage((SectionPos)sectionPos) < j).min(Comparator.comparingInt(serverLevel::sectionsToVillage)).orElse(sectionPos2);
    }

    public static boolean isWithinAttackRange(Mob mob, LivingEntity livingEntity, int i) {
        Item item = mob.getMainHandItem().getItem();
        if (item instanceof ProjectileWeaponItem && mob.canFireProjectileWeapon((ProjectileWeaponItem)item)) {
            int j = ((ProjectileWeaponItem)item).getDefaultProjectileRange() - i;
            return mob.closerThan(livingEntity, j);
        }
        return BehaviorUtils.isWithinMeleeAttackRange(mob, livingEntity);
    }

    public static boolean isWithinMeleeAttackRange(LivingEntity livingEntity, LivingEntity livingEntity2) {
        double e;
        double d = livingEntity.distanceToSqr(livingEntity2.getX(), livingEntity2.getY(), livingEntity2.getZ());
        return d <= (e = (double)(livingEntity.getBbWidth() * 2.0f * (livingEntity.getBbWidth() * 2.0f) + livingEntity2.getBbWidth()));
    }

    public static boolean isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(LivingEntity livingEntity, LivingEntity livingEntity2, double d) {
        Optional<LivingEntity> optional = livingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if (!optional.isPresent()) {
            return false;
        }
        double e = livingEntity.distanceToSqr(optional.get().position());
        double f = livingEntity.distanceToSqr(livingEntity2.position());
        return f > e + d * d;
    }

    public static boolean canSee(LivingEntity livingEntity, LivingEntity livingEntity2) {
        Brain<List<LivingEntity>> brain = livingEntity.getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.VISIBLE_LIVING_ENTITIES)) {
            return false;
        }
        return brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get().contains(livingEntity2);
    }

    public static LivingEntity getNearestTarget(LivingEntity livingEntity, Optional<LivingEntity> optional, LivingEntity livingEntity2) {
        if (!optional.isPresent()) {
            return livingEntity2;
        }
        return BehaviorUtils.getTargetNearestMe(livingEntity, optional.get(), livingEntity2);
    }

    public static LivingEntity getTargetNearestMe(LivingEntity livingEntity, LivingEntity livingEntity2, LivingEntity livingEntity3) {
        Vec3 vec3 = livingEntity2.position();
        Vec3 vec32 = livingEntity3.position();
        return livingEntity.distanceToSqr(vec3) < livingEntity.distanceToSqr(vec32) ? livingEntity2 : livingEntity3;
    }

    public static Optional<LivingEntity> getLivingEntityFromUUIDMemory(LivingEntity livingEntity, MemoryModuleType<SerializableUUID> memoryModuleType) {
        Optional<SerializableUUID> optional = livingEntity.getBrain().getMemory(memoryModuleType);
        return optional.map(SerializableUUID::value).map(uUID -> (LivingEntity)((ServerLevel)livingEntity.level).getEntity((UUID)uUID));
    }
}

