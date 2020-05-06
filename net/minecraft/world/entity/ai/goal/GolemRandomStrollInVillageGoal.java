/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.goal;

import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class GolemRandomStrollInVillageGoal
extends RandomStrollGoal {
    public GolemRandomStrollInVillageGoal(PathfinderMob pathfinderMob, double d) {
        super(pathfinderMob, d, 240, false);
    }

    @Override
    @Nullable
    protected Vec3 getPosition() {
        Vec3 vec3;
        float f = this.mob.level.random.nextFloat();
        if (this.mob.level.random.nextFloat() < 0.3f) {
            return this.getPositionTowardsAnywhere();
        }
        if (f < 0.7f) {
            vec3 = this.getPositionTowardsVillagerWhoWantsGolem();
            if (vec3 == null) {
                vec3 = this.getPositionTowardsPoi();
            }
        } else {
            vec3 = this.getPositionTowardsPoi();
            if (vec3 == null) {
                vec3 = this.getPositionTowardsVillagerWhoWantsGolem();
            }
        }
        return vec3 == null ? this.getPositionTowardsAnywhere() : vec3;
    }

    @Nullable
    private Vec3 getPositionTowardsAnywhere() {
        return RandomPos.getLandPos(this.mob, 10, 7);
    }

    @Nullable
    private Vec3 getPositionTowardsVillagerWhoWantsGolem() {
        ServerLevel serverLevel = (ServerLevel)this.mob.level;
        List<Villager> list = serverLevel.getEntities(EntityType.VILLAGER, this.mob.getBoundingBox().inflate(32.0), this::doesVillagerWantGolem);
        if (list.isEmpty()) {
            return null;
        }
        Villager villager = list.get(this.mob.level.random.nextInt(list.size()));
        Vec3 vec3 = villager.position();
        return RandomPos.getLandPosTowards(this.mob, 10, 7, vec3);
    }

    @Nullable
    private Vec3 getPositionTowardsPoi() {
        SectionPos sectionPos = this.getRandomVillageSection();
        if (sectionPos == null) {
            return null;
        }
        BlockPos blockPos = this.getRandomPoiWithinSection(sectionPos);
        if (blockPos == null) {
            return null;
        }
        return RandomPos.getLandPosTowards(this.mob, 10, 7, Vec3.atBottomCenterOf(blockPos));
    }

    @Nullable
    private SectionPos getRandomVillageSection() {
        ServerLevel serverLevel = (ServerLevel)this.mob.level;
        List list = SectionPos.cube(SectionPos.of(this.mob), 2).filter(sectionPos -> serverLevel.sectionsToVillage((SectionPos)sectionPos) == 0).collect(Collectors.toList());
        if (list.isEmpty()) {
            return null;
        }
        return (SectionPos)list.get(serverLevel.random.nextInt(list.size()));
    }

    @Nullable
    private BlockPos getRandomPoiWithinSection(SectionPos sectionPos) {
        ServerLevel serverLevel = (ServerLevel)this.mob.level;
        PoiManager poiManager = serverLevel.getPoiManager();
        List list = poiManager.getInRange(poiType -> true, sectionPos.center(), 8, PoiManager.Occupancy.IS_OCCUPIED).map(PoiRecord::getPos).collect(Collectors.toList());
        if (list.isEmpty()) {
            return null;
        }
        return (BlockPos)list.get(serverLevel.random.nextInt(list.size()));
    }

    private boolean doesVillagerWantGolem(Villager villager) {
        return villager.wantsToSpawnGolem(this.mob.level.getGameTime());
    }
}

