/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.npc;

import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;
import org.jetbrains.annotations.Nullable;

public class WanderingTraderSpawner {
    private final Random random = new Random();
    private final ServerLevel level;
    private int tickDelay;
    private int spawnDelay;
    private int spawnChance;

    public WanderingTraderSpawner(ServerLevel serverLevel) {
        this.level = serverLevel;
        this.tickDelay = 1200;
        LevelData levelData = serverLevel.getLevelData();
        this.spawnDelay = levelData.getWanderingTraderSpawnDelay();
        this.spawnChance = levelData.getWanderingTraderSpawnChance();
        if (this.spawnDelay == 0 && this.spawnChance == 0) {
            this.spawnDelay = 24000;
            levelData.setWanderingTraderSpawnDelay(this.spawnDelay);
            this.spawnChance = 25;
            levelData.setWanderingTraderSpawnChance(this.spawnChance);
        }
    }

    public void tick() {
        if (!this.level.getGameRules().getBoolean(GameRules.RULE_DO_TRADER_SPAWNING)) {
            return;
        }
        if (--this.tickDelay > 0) {
            return;
        }
        this.tickDelay = 1200;
        LevelData levelData = this.level.getLevelData();
        this.spawnDelay -= 1200;
        levelData.setWanderingTraderSpawnDelay(this.spawnDelay);
        if (this.spawnDelay > 0) {
            return;
        }
        this.spawnDelay = 24000;
        if (!this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return;
        }
        int i = this.spawnChance;
        this.spawnChance = Mth.clamp(this.spawnChance + 25, 25, 75);
        levelData.setWanderingTraderSpawnChance(this.spawnChance);
        if (this.random.nextInt(100) > i) {
            return;
        }
        if (this.spawn()) {
            this.spawnChance = 25;
        }
    }

    private boolean spawn() {
        ServerPlayer player = this.level.getRandomPlayer();
        if (player == null) {
            return true;
        }
        if (this.random.nextInt(10) != 0) {
            return false;
        }
        BlockPos blockPos2 = player.blockPosition();
        int i = 48;
        PoiManager poiManager = this.level.getPoiManager();
        Optional<BlockPos> optional = poiManager.find(PoiType.MEETING.getPredicate(), blockPos -> true, blockPos2, 48, PoiManager.Occupancy.ANY);
        BlockPos blockPos22 = optional.orElse(blockPos2);
        BlockPos blockPos3 = this.findSpawnPositionNear(blockPos22, 48);
        if (blockPos3 != null && this.hasEnoughSpace(blockPos3)) {
            if (this.level.getBiome(blockPos3) == Biomes.THE_VOID) {
                return false;
            }
            WanderingTrader wanderingTrader = EntityType.WANDERING_TRADER.spawn(this.level, null, null, null, blockPos3, MobSpawnType.EVENT, false, false);
            if (wanderingTrader != null) {
                for (int j = 0; j < 2; ++j) {
                    this.tryToSpawnLlamaFor(wanderingTrader, 4);
                }
                this.level.getLevelData().setWanderingTraderId(wanderingTrader.getUUID());
                wanderingTrader.setDespawnDelay(48000);
                wanderingTrader.setWanderTarget(blockPos22);
                wanderingTrader.restrictTo(blockPos22, 16);
                return true;
            }
        }
        return false;
    }

    private void tryToSpawnLlamaFor(WanderingTrader wanderingTrader, int i) {
        BlockPos blockPos = this.findSpawnPositionNear(wanderingTrader.blockPosition(), i);
        if (blockPos == null) {
            return;
        }
        TraderLlama traderLlama = EntityType.TRADER_LLAMA.spawn(this.level, null, null, null, blockPos, MobSpawnType.EVENT, false, false);
        if (traderLlama == null) {
            return;
        }
        traderLlama.setLeashedTo(wanderingTrader, true);
    }

    @Nullable
    private BlockPos findSpawnPositionNear(BlockPos blockPos, int i) {
        BlockPos blockPos2 = null;
        for (int j = 0; j < 10; ++j) {
            int l;
            int m;
            int k = blockPos.getX() + this.random.nextInt(i * 2) - i;
            BlockPos blockPos3 = new BlockPos(k, m = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, k, l = blockPos.getZ() + this.random.nextInt(i * 2) - i), l);
            if (!NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, this.level, blockPos3, EntityType.WANDERING_TRADER)) continue;
            blockPos2 = blockPos3;
            break;
        }
        return blockPos2;
    }

    private boolean hasEnoughSpace(BlockPos blockPos) {
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos, blockPos.offset(1, 2, 1))) {
            if (this.level.getBlockState(blockPos2).getCollisionShape(this.level, blockPos2).isEmpty()) continue;
            return false;
        }
        return true;
    }
}

