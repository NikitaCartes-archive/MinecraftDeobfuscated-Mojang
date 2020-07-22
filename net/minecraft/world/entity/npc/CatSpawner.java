/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.npc;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.phys.AABB;

public class CatSpawner
implements CustomSpawner {
    private int nextTick;

    @Override
    public int tick(ServerLevel serverLevel, boolean bl, boolean bl2) {
        if (!bl2 || !serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return 0;
        }
        --this.nextTick;
        if (this.nextTick > 0) {
            return 0;
        }
        this.nextTick = 1200;
        ServerPlayer player = serverLevel.getRandomPlayer();
        if (player == null) {
            return 0;
        }
        Random random = serverLevel.random;
        int i = (8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
        int j = (8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
        BlockPos blockPos = player.blockPosition().offset(i, 0, j);
        if (!serverLevel.hasChunksAt(blockPos.getX() - 10, blockPos.getY() - 10, blockPos.getZ() - 10, blockPos.getX() + 10, blockPos.getY() + 10, blockPos.getZ() + 10)) {
            return 0;
        }
        if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, serverLevel, blockPos, EntityType.CAT)) {
            if (serverLevel.isCloseToVillage(blockPos, 2)) {
                return this.spawnInVillage(serverLevel, blockPos);
            }
            if (serverLevel.structureFeatureManager().getStructureAt(blockPos, true, StructureFeature.SWAMP_HUT).isValid()) {
                return this.spawnInHut(serverLevel, blockPos);
            }
        }
        return 0;
    }

    private int spawnInVillage(ServerLevel serverLevel, BlockPos blockPos) {
        List<Cat> list;
        int i = 48;
        if (serverLevel.getPoiManager().getCountInRange(PoiType.HOME.getPredicate(), blockPos, 48, PoiManager.Occupancy.IS_OCCUPIED) > 4L && (list = serverLevel.getEntitiesOfClass(Cat.class, new AABB(blockPos).inflate(48.0, 8.0, 48.0))).size() < 5) {
            return this.spawnCat(blockPos, serverLevel);
        }
        return 0;
    }

    private int spawnInHut(ServerLevel serverLevel, BlockPos blockPos) {
        int i = 16;
        List<Cat> list = serverLevel.getEntitiesOfClass(Cat.class, new AABB(blockPos).inflate(16.0, 8.0, 16.0));
        if (list.size() < 1) {
            return this.spawnCat(blockPos, serverLevel);
        }
        return 0;
    }

    private int spawnCat(BlockPos blockPos, ServerLevel serverLevel) {
        Cat cat = EntityType.CAT.create(serverLevel);
        if (cat == null) {
            return 0;
        }
        cat.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(blockPos), MobSpawnType.NATURAL, null, null);
        cat.moveTo(blockPos, 0.0f, 0.0f);
        serverLevel.addFreshEntityWithPassengers(cat);
        return 1;
    }
}

