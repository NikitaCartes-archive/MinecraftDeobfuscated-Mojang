/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PiglinSpecificSensor
extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, new MemoryModuleType[]{MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_REPELLENT});
    }

    @Override
    protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity2) {
        Brain<?> brain = livingEntity2.getBrain();
        brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, PiglinSpecificSensor.findNearestRepellent(serverLevel, livingEntity2));
        Optional<Object> optional = Optional.empty();
        Optional<Object> optional2 = Optional.empty();
        Optional<Object> optional3 = Optional.empty();
        Optional<Object> optional4 = Optional.empty();
        Optional<Object> optional5 = Optional.empty();
        Optional<Object> optional6 = Optional.empty();
        Optional<Object> optional7 = Optional.empty();
        int i = 0;
        ArrayList<AbstractPiglin> list = Lists.newArrayList();
        ArrayList<AbstractPiglin> list2 = Lists.newArrayList();
        NearestVisibleLivingEntities nearestVisibleLivingEntities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
        for (LivingEntity livingEntity22 : nearestVisibleLivingEntities.findAll(livingEntity -> true)) {
            LivingEntity livingEntity3 = livingEntity22;
            if (livingEntity3 instanceof Hoglin) {
                Hoglin hoglin = (Hoglin)livingEntity3;
                if (hoglin.isBaby() && optional3.isEmpty()) {
                    optional3 = Optional.of(hoglin);
                    continue;
                }
                if (!hoglin.isAdult()) continue;
                ++i;
                if (!optional2.isEmpty() || !hoglin.canBeHunted()) continue;
                optional2 = Optional.of(hoglin);
                continue;
            }
            livingEntity3 = livingEntity22;
            if (livingEntity3 instanceof PiglinBrute) {
                PiglinBrute piglinBrute = (PiglinBrute)livingEntity3;
                list.add(piglinBrute);
                continue;
            }
            livingEntity3 = livingEntity22;
            if (livingEntity3 instanceof Piglin) {
                Piglin piglin = (Piglin)livingEntity3;
                if (piglin.isBaby() && optional4.isEmpty()) {
                    optional4 = Optional.of(piglin);
                    continue;
                }
                if (!piglin.isAdult()) continue;
                list.add(piglin);
                continue;
            }
            livingEntity3 = livingEntity22;
            if (livingEntity3 instanceof Player) {
                Player player = (Player)livingEntity3;
                if (optional6.isEmpty() && !PiglinAi.isWearingGold(player) && livingEntity2.canAttack(livingEntity22)) {
                    optional6 = Optional.of(player);
                }
                if (!optional7.isEmpty() || player.isSpectator() || !PiglinAi.isPlayerHoldingLovedItem(player)) continue;
                optional7 = Optional.of(player);
                continue;
            }
            if (optional.isEmpty() && (livingEntity22 instanceof WitherSkeleton || livingEntity22 instanceof WitherBoss)) {
                optional = Optional.of((Mob)livingEntity22);
                continue;
            }
            if (!optional5.isEmpty() || !PiglinAi.isZombified(livingEntity22.getType())) continue;
            optional5 = Optional.of(livingEntity22);
        }
        List list3 = brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(ImmutableList.of());
        for (LivingEntity livingEntity3 : list3) {
            AbstractPiglin abstractPiglin;
            LivingEntity livingEntity4 = livingEntity3;
            if (!(livingEntity4 instanceof AbstractPiglin) || !(abstractPiglin = (AbstractPiglin)livingEntity4).isAdult()) continue;
            list2.add(abstractPiglin);
        }
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optional);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, optional2);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, optional3);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, optional5);
        brain.setMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, optional6);
        brain.setMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, optional7);
        brain.setMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS, list2);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, list);
        brain.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, list.size());
        brain.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, i);
    }

    private static Optional<BlockPos> findNearestRepellent(ServerLevel serverLevel, LivingEntity livingEntity) {
        return BlockPos.findClosestMatch(livingEntity.blockPosition(), 8, 4, blockPos -> PiglinSpecificSensor.isValidRepellent(serverLevel, blockPos));
    }

    private static boolean isValidRepellent(ServerLevel serverLevel, BlockPos blockPos) {
        BlockState blockState = serverLevel.getBlockState(blockPos);
        boolean bl = blockState.is(BlockTags.PIGLIN_REPELLENTS);
        if (bl && blockState.is(Blocks.SOUL_CAMPFIRE)) {
            return CampfireBlock.isLitCampfire(blockState);
        }
        return bl;
    }
}

